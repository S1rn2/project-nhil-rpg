package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * The Nhil Blade, a sword that can release a short rune wave.
 *
 * <p>The wave is server side only, covers a small radius and respects a cooldown,
 * so it stays cheap in multiplayer and never scans the world.
 */
public class NhilBladeItem extends Item {
    /** Reach of the rune wave, in blocks. */
    private static final double WAVE_RADIUS = 4.0D;
    /** Damage dealt to each creature caught in the wave. */
    private static final float WAVE_DAMAGE = 5.0F;
    /** Cooldown between waves, in ticks. */
    private static final int COOLDOWN_TICKS = 240;

    public NhilBladeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient() || !(user instanceof ServerPlayerEntity player)) {
            return ActionResult.SUCCESS;
        }
        ItemStack stack = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.PASS;
        }

        int level = NhilProgress.of(player).level();
        Box area = player.getBoundingBox().expand(WAVE_RADIUS);
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, area,
                candidate -> candidate != player && candidate.isAlive() && !candidate.isSpectator());

        int struck = 0;
        int cap = Math.min(targets.size(), 6);
        for (int index = 0; index < cap; index++) {
            LivingEntity target = targets.get(index);
            if (target.damage(serverWorld, serverWorld.getDamageSources().playerAttack(player),
                    WAVE_DAMAGE + level * 0.2F)) {
                struck++;
            }
        }

        serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 0.9F, 1.4F);
        serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0,
                player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

        if (struck > 0) {
            player.sendMessage(Text.literal("Rune wave strikes " + struck + " for " + (int) WAVE_DAMAGE + ".")
                    .formatted(Formatting.LIGHT_PURPLE), true);
        } else {
            player.sendMessage(Text.literal("The rune wave finds nothing to cut.")
                    .formatted(Formatting.GRAY), true);
        }
        return ActionResult.SUCCESS;
    }
}
