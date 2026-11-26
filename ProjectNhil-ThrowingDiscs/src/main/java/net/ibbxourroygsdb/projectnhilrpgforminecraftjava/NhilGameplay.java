package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.List;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

/**
 * Wires the RPG progression into normal play: exploring, fighting and salvaging.
 */
public final class NhilGameplay {
    private NhilGameplay() {
    }

    /** How often (in ticks) a player's position is checked for a new ruin. */
    private static final int DISCOVERY_INTERVAL = 40;

    /** Ruins and abandoned sites worth recording. */
    private static final List<RegistryKey<Structure>> LANDMARKS = List.of(
            StructureKeys.RUINED_PORTAL,
            StructureKeys.TRAIL_RUINS,
            StructureKeys.DESERT_PYRAMID,
            StructureKeys.JUNGLE_PYRAMID,
            StructureKeys.IGLOO,
            StructureKeys.SWAMP_HUT,
            StructureKeys.PILLAGER_OUTPOST,
            StructureKeys.ANCIENT_CITY,
            StructureKeys.TRIAL_CHAMBERS,
            StructureKeys.SHIPWRECK,
            StructureKeys.BURIED_TREASURE,
            StructureKeys.STRONGHOLD,
            StructureKeys.MINESHAFT);

    private static int tickCounter;

    public static void register() {
        // Load and repair progress when a player joins.
        ServerPlayerEvents.JOIN.register(player -> {
            NhilProgress progress = NhilProgress.of(player);
            NhilProgress.syncHealth(player, progress.level());
            if (progress.level() > 1 || progress.relics() > 0) {
                player.sendMessage(Text.literal(progress.rank() + "  |  level " + progress.level()
                                + "  |  relics " + progress.relics()
                                + "  |  landmarks " + progress.discoveries().size())
                        .formatted(Formatting.GRAY), false);
            }
        });

        // Restore the health bonus after a respawn.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            NhilProgress progress = NhilProgress.of(newPlayer);
            NhilProgress.syncHealth(newPlayer, progress.level());
        });

        // Exploring: cheap positional check, never a cube scan.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter < DISCOVERY_INTERVAL) {
                return;
            }
            tickCounter = 0;
            for (ServerWorld world : server.getWorlds()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    checkLandmark(world, player);
                }
            }
        });

        // Salvaging ruin materials slowly teaches the player.
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }
            if (isRuinBlock(state)) {
                NhilProgress.grantExperience(serverPlayer, 1, "salvage");
            }
        });

        // Fighting: defeating monsters feeds progression and can drop a rune shard.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof Monster)) {
                return;
            }
            ServerPlayerEntity killer = killerOf(source);
            if (killer == null) {
                return;
            }
            NhilProgress.grantExperience(killer, 2, "hunt");
            float roll = entity.getRandom().nextFloat();
            if (roll < 0.2F) {
                ItemStack shard = new ItemStack(NhilContent.RUNE_SHARD);
                if (!killer.getInventory().insertStack(shard)) {
                    killer.dropItem(shard, false);
                }
                killer.sendMessage(Text.literal("A rune shard clatters loose.")
                        .formatted(Formatting.AQUA), true);
            } else if (roll < 0.26F) {
                ItemStack tonic = new ItemStack(NhilContent.RUNE_TONIC);
                if (!killer.getInventory().insertStack(tonic)) {
                    killer.dropItem(tonic, false);
                }
                killer.sendMessage(Text.literal("A rune tonic spills from the corpse.")
                        .formatted(Formatting.GREEN), true);
            }
        });

        // Danger pays: a solid blow from a monster teaches the player something.
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (!(entity instanceof ServerPlayerEntity player) || taken < 4.0F) {
                return;
            }
            if (source.getAttacker() instanceof Monster && player.getRandom().nextFloat() < 0.25F) {
                NhilProgress.grantExperience(player, 1, "endurance");
            }
        });
    }

    private static ServerPlayerEntity killerOf(DamageSource source) {
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            return player;
        }
        return null;
    }

    private static boolean isRuinBlock(BlockState state) {
        return state.isOf(NhilContent.RUIN_BRICK)
                || state.isOf(NhilContent.MOSSY_RUIN_BRICK)
                || state.isOf(NhilContent.DEEP_RUIN_TILE)
                || state.isOf(NhilContent.RUNE_LAMP)
                || state.isOf(NhilContent.RUNE_ALTAR);
    }

    private static void checkLandmark(ServerWorld world, ServerPlayerEntity player) {
        try {
            Registry<Structure> registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
            BlockPos pos = player.getBlockPos();
            for (RegistryKey<Structure> key : LANDMARKS) {
                Structure structure = registry.getOptionalValue(key).orElse(null);
                if (structure == null) {
                    continue;
                }
                StructureStart start = world.getStructureAccessor().getStructureAt(pos, structure);
                if (start == null || !start.hasChildren()) {
                    continue;
                }
                Identifier id = key.getValue();
                if (NhilProgress.registerDiscovery(player, id.toString())) {
                    player.sendMessage(Text.literal("Landmark discovered: " + WayfinderCompassItem.prettify(id.getPath()))
                            .formatted(Formatting.GOLD), false);
                    player.sendMessage(Text.literal("+6 Nhil experience").formatted(Formatting.GRAY), true);
                    NhilProgress.grantExperience(player, 6, "exploration");
                }
                return;
            }
        } catch (RuntimeException exception) {
            PGPlugin.LOGGER.warn("Landmark check failed for {}", player.getName().getString(), exception);
        }
    }

    /** Read-only view used by the status command. */
    public static List<String> landmarkNames() {
        return LANDMARKS.stream().map(key -> key.getValue().getPath()).toList();
    }
}
