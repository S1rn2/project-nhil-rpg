package net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A thrown disc. It flies straight with no gravity, ricochets off blocks (each bounce extends the
 * outward flight), damages every mob it passes through, then homes back to its owner.
 *
 * <p>Performance: all logic runs server side only, one entity query and at most four block lookups
 * per tick per disc, and blocks in unloaded chunks are treated as walls so a disc can never force
 * a chunk load.
 */
public class ThrowingDiscEntity extends Entity implements FlyingItemEntity {
    private static final TrackedData<Integer> TIER_ID =
            DataTracker.registerData(ThrowingDiscEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final int OUT_TICKS = 12;
    private static final int BOUNCE_BONUS_TICKS = 5;
    private static final int MAX_BOUNCES = 6;
    private static final double RETURN_PULL = 0.3;
    private static final double RETURN_DRAG = 0.88;
    private static final double RETURN_MAX_SPEED = 1.7;
    private static final double CATCH_RADIUS_SQ = 1.5 * 1.5;
    private static final int MIN_CATCH_TICKS = 5;
    private static final int MAX_FLIGHT_TICKS = 400;
    private static final int MAX_HITS_PER_TARGET = 3;
    private static final int HIT_GAP_TICKS = 6;

    private ItemStack stack = ItemStack.EMPTY;
    private UUID ownerId;
    private Vec3d motion = Vec3d.ZERO;
    private boolean returning;
    private int outTicksLeft = OUT_TICKS;
    private int bounces;
    private int flightTicks;
    /** Per target: {hit count, tick of last hit}. Not saved; a reload simply resets the limits. */
    private final Map<UUID, int[]> hits = new HashMap<>();

    private ItemStack displayStack;
    private DiscTier displayTier;

    public ThrowingDiscEntity(EntityType<? extends ThrowingDiscEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    /** Server side: start a fresh throw. */
    public void launch(PlayerEntity owner, ItemStack thrown, DiscTier tier) {
        this.ownerId = owner.getUuid();
        this.stack = thrown;
        this.dataTracker.set(TIER_ID, tier.ordinal());
        Vec3d look = owner.getRotationVec(1.0F).normalize();
        this.motion = look.multiply(tier.speed * 0.8);
        this.outTicksLeft = OUT_TICKS;
        Vec3d start = new Vec3d(owner.getX(), owner.getEyeY() - 0.15, owner.getZ()).add(look.multiply(0.6));
        this.setPosition(start.x, start.y, start.z);
        this.setVelocity(this.motion);
    }

    public DiscTier getTier() {
        return DiscTier.byIndex(this.dataTracker.get(TIER_ID));
    }

    @Override
    public ItemStack getStack() {
        if (!stack.isEmpty()) {
            return stack;
        }
        // Client side (and fallback): show a plain stack of this disc's tier, cached.
        DiscTier tier = getTier();
        if (displayStack == null || displayTier != tier) {
            displayStack = new ItemStack(tier.item);
            displayTier = tier;
        }
        return displayStack;
    }

    @Override
    public void tick() {
        super.tick();
        World world = getEntityWorld();
        if (world.isClient()) {
            // Cheap extrapolation between server position updates.
            Vec3d v = getVelocity();
            setPosition(getX() + v.x, getY() + v.y, getZ() + v.z);
            return;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        flightTicks++;
        if (flightTicks > MAX_FLIGHT_TICKS) {
            dropAndRemove(serverWorld);
            return;
        }

        PlayerEntity owner = ownerId == null ? null : serverWorld.getPlayerByUuid(ownerId);
        if (returning) {
            if (owner == null || !owner.isAlive()) {
                dropAndRemove(serverWorld);
                return;
            }
            steerHome(owner);
        } else if (outTicksLeft-- <= 0) {
            returning = true;
        }

        if (!returning) {
            bounceOffBlocks(serverWorld);
        }

        Vec3d next = getEntityPos().add(motion);
        setPosition(next.x, next.y, next.z);
        setVelocity(motion);

        hitMobs(serverWorld, owner);

        if (returning && owner != null && flightTicks > MIN_CATCH_TICKS
                && getEntityPos().squaredDistanceTo(owner.getEntityPos().add(0, owner.getHeight() * 0.5, 0))
                        < CATCH_RADIUS_SQ) {
            catchBy(owner);
        }
    }

    private void steerHome(PlayerEntity owner) {
        Vec3d target = owner.getEntityPos().add(0, owner.getHeight() * 0.6, 0);
        Vec3d toOwner = target.subtract(getEntityPos());
        double dist = toOwner.length();
        if (dist > 1.0E-4) {
            motion = motion.multiply(RETURN_DRAG).add(toOwner.multiply(RETURN_PULL / dist));
        }
        double speed = motion.length();
        if (speed > RETURN_MAX_SPEED) {
            motion = motion.multiply(RETURN_MAX_SPEED / speed);
        }
    }

    /** Tests each axis separately so the disc reflects off the face it actually hit. */
    private void bounceOffBlocks(ServerWorld world) {
        Vec3d p = getEntityPos();
        boolean hx = blocked(world, p.x + motion.x, p.y, p.z);
        boolean hy = blocked(world, p.x, p.y + motion.y, p.z);
        boolean hz = blocked(world, p.x, p.y, p.z + motion.z);
        if (!hx && !hy && !hz && blocked(world, p.x + motion.x, p.y + motion.y, p.z + motion.z)) {
            hx = true;
            hy = true;
            hz = true;
        }
        if (!hx && !hy && !hz) {
            return;
        }
        motion = new Vec3d(hx ? -motion.x : motion.x, hy ? -motion.y : motion.y, hz ? -motion.z : motion.z);
        bounces++;
        outTicksLeft += BOUNCE_BONUS_TICKS;
        if (bounces >= MAX_BOUNCES) {
            returning = true;
        }
        world.playSound(null, getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS, 0.5F, getTier().pitch);
    }

    private static boolean blocked(ServerWorld world, double x, double y, double z) {
        BlockPos pos = BlockPos.ofFloored(x, y, z);
        if (!world.isChunkLoaded(pos)) {
            return true;
        }
        BlockState state = world.getBlockState(pos);
        return !state.getCollisionShape(world, pos).isEmpty();
    }

    private void hitMobs(ServerWorld world, PlayerEntity owner) {
        Box box = getBoundingBox().expand(0.35);
        List<LivingEntity> found = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e.isAlive() && !e.isSpectator() && !e.getUuid().equals(ownerId));
        if (found.isEmpty()) {
            return;
        }
        DiscTier tier = getTier();
        for (LivingEntity target : found) {
            int[] record = hits.computeIfAbsent(target.getUuid(), k -> new int[] {0, -HIT_GAP_TICKS});
            if (record[0] >= MAX_HITS_PER_TARGET || flightTicks - record[1] < HIT_GAP_TICKS) {
                continue;
            }
            record[0]++;
            record[1] = flightTicks;
            // The owner is the attacker, so Nhil's kill-experience logic credits the thrower.
            target.damage(world, world.getDamageSources().thrown(this, owner), tier.damage);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    SoundCategory.PLAYERS, 0.7F, tier.pitch);
        }
    }

    /** Returns the disc to its owner, spending one point of durability per completed throw. */
    private void catchBy(PlayerEntity owner) {
        ItemStack back = stack.copy();
        if (!back.isEmpty()) {
            if (back.isDamageable()) {
                int worn = back.getDamage() + 1;
                if (worn >= back.getMaxDamage()) {
                    discard();
                    return;
                }
                back.setDamage(worn);
            }
            owner.giveItemStack(back);
        }
        discard();
    }

    private void dropAndRemove(ServerWorld world) {
        if (!stack.isEmpty()) {
            world.spawnEntity(new ItemEntity(world, getX(), getY(), getZ(), stack.copy()));
        }
        discard();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TIER_ID, 0);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.dataTracker.set(TIER_ID, view.getInt("tier", 0));
        this.returning = view.getBoolean("returning", false);
        this.outTicksLeft = view.getInt("outTicksLeft", OUT_TICKS);
        this.bounces = view.getInt("bounces", 0);
        this.flightTicks = view.getInt("flightTicks", 0);
        this.motion = new Vec3d(view.getDouble("mx", 0), view.getDouble("my", 0), view.getDouble("mz", 0));
        this.stack = view.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.ownerId = view.getOptionalString("owner").map(ThrowingDiscEntity::parseUuid).orElse(null);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("tier", getTier().ordinal());
        view.putBoolean("returning", returning);
        view.putInt("outTicksLeft", outTicksLeft);
        view.putInt("bounces", bounces);
        view.putInt("flightTicks", flightTicks);
        view.putDouble("mx", motion.x);
        view.putDouble("my", motion.y);
        view.putDouble("mz", motion.z);
        if (!stack.isEmpty()) {
            view.put("stack", ItemStack.CODEC, stack);
        }
        if (ownerId != null) {
            view.putString("owner", ownerId.toString());
        }
    }

    private static UUID parseUuid(String text) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
