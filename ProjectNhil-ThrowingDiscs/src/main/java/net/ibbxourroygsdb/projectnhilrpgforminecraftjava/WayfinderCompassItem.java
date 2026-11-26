package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

/**
 * The Wayfinder Compass: reveals the nearest ruin or abandoned site.
 */
public class WayfinderCompassItem extends Item {
    /** Search radius in chunks. */
    private static final int SEARCH_RADIUS_CHUNKS = 60;
    /** Cooldown in ticks between uses. */
    private static final int COOLDOWN_TICKS = 200;

    private static final List<RegistryKey<Structure>> RUIN_SITES = List.of(
            StructureKeys.RUINED_PORTAL,
            StructureKeys.RUINED_PORTAL_DESERT,
            StructureKeys.RUINED_PORTAL_JUNGLE,
            StructureKeys.RUINED_PORTAL_SWAMP,
            StructureKeys.RUINED_PORTAL_MOUNTAIN,
            StructureKeys.TRAIL_RUINS,
            StructureKeys.DESERT_PYRAMID,
            StructureKeys.JUNGLE_PYRAMID,
            StructureKeys.IGLOO,
            StructureKeys.SWAMP_HUT,
            StructureKeys.PILLAGER_OUTPOST,
            StructureKeys.MINESHAFT,
            StructureKeys.STRONGHOLD,
            StructureKeys.ANCIENT_CITY,
            StructureKeys.TRIAL_CHAMBERS,
            StructureKeys.SHIPWRECK,
            StructureKeys.BURIED_TREASURE,
            StructureKeys.MANSION,
            StructureKeys.MONUMENT,
            StructureKeys.OCEAN_RUIN_COLD,
            StructureKeys.OCEAN_RUIN_WARM);

    public WayfinderCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient() || !(user instanceof ServerPlayerEntity player)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld serverWorld = player.getEntityWorld();
        if (player.getItemCooldownManager().isCoolingDown(player.getStackInHand(hand))) {
            return ActionResult.FAIL;
        }
        Pair<BlockPos, RegistryEntry<Structure>> found = locate(serverWorld, player);
        if (found == null) {
            player.sendMessage(Text.literal("The needle spins. No ruins within reach.")
                    .formatted(Formatting.GRAY), true);
            return ActionResult.FAIL;
        }

        BlockPos target = found.getFirst();
        Identifier id = found.getSecond().getKey()
                .map(RegistryKey::getValue)
                .orElse(Identifier.ofVanilla("ruins"));
        String pretty = prettify(id.getPath());
        String direction = directionFrom(player, target);
        int distance = (int) Math.round(Math.sqrt(target.getSquaredDistance(player.getX(), player.getY(), player.getZ())));

        player.sendMessage(Text.literal("Wayfinder: " + pretty)
                .formatted(Formatting.GOLD)
                .append(Text.literal(" (" + distance + " blocks " + direction + ")").formatted(Formatting.GRAY)), false);
        player.sendMessage(Text.literal("Recorded a new landmark.").formatted(Formatting.YELLOW), true);

        player.getItemCooldownManager().set(player.getStackInHand(hand), COOLDOWN_TICKS);
        NhilProgress.grantExperience(player, 4, "landmark");
        NhilProgress.registerDiscovery(player, id.toString());
        return ActionResult.SUCCESS;
    }

    private static Pair<BlockPos, RegistryEntry<Structure>> locate(ServerWorld world, PlayerEntity player) {
        try {
            Registry<Structure> registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
            List<RegistryEntry<Structure>> entries = new ArrayList<>();
            for (RegistryKey<Structure> key : RUIN_SITES) {
                registry.getEntry(key.getValue()).ifPresent(entries::add);
            }
            if (entries.isEmpty()) {
                return null;
            }
            RegistryEntryList<Structure> candidates = RegistryEntryList.of(entries);
            return world.getChunkManager().getChunkGenerator().locateStructure(
                    world, candidates, player.getBlockPos(), SEARCH_RADIUS_CHUNKS, true);
        } catch (RuntimeException exception) {
            PGPlugin.LOGGER.warn("Wayfinder search failed", exception);
            return null;
        }
    }

    private static String directionFrom(PlayerEntity player, BlockPos target) {
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? "east" : "west";
        }
        return dz > 0 ? "south" : "north";
    }

    static String prettify(String path) {
        String spaced = path.replace('_', ' ');
        if (spaced.isEmpty()) {
            return "ruins";
        }
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
