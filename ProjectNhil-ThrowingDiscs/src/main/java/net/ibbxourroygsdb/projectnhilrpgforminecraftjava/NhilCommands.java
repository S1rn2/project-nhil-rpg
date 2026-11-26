package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * The {@code nhil} command tree.
 *
 * <p>The root literal has no permission gate so the server console can always read status.
 */
public final class NhilCommands {
    private NhilCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nhil")
                .executes(ctx -> status(ctx.getSource()))
                .then(CommandManager.literal("status")
                        .executes(ctx -> status(ctx.getSource())))
                .then(CommandManager.literal("landmarks")
                        .executes(ctx -> landmarks(ctx.getSource())))
                .then(CommandManager.literal("progress")
                        .executes(ctx -> progress(ctx.getSource())))
                .then(CommandManager.literal("ranks")
                        .executes(ctx -> ranks(ctx.getSource())))
                .then(CommandManager.literal("grant")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 500))
                                .executes(ctx -> grant(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "amount")))))
                .then(CommandManager.literal("relic")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .executes(ctx -> relic(ctx.getSource()))));
    }

    private static int status(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Project Nhil RPG")
                .formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("Levels need " + NhilProgress.XP_PER_LEVEL
                + " experience at first, rising by " + NhilProgress.XP_STEP
                + " each level, maximum level " + NhilProgress.MAX_LEVEL).formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("Earn experience by exploring ruins, salvaging ruin blocks, "
                + "fighting monsters and surviving their blows.").formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("Use /nhil landmarks to list the tracked sites.")
                .formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int landmarks(ServerCommandSource source) {
        var names = NhilGameplay.landmarkNames();
        source.sendFeedback(() -> Text.literal("Tracked landmarks (" + names.size() + ")")
                .formatted(Formatting.GOLD), false);
        for (String name : names) {
            source.sendFeedback(() -> Text.literal(" - " + WayfinderCompassItem.prettify(name))
                    .formatted(Formatting.GRAY), false);
        }
        return names.size();
    }

    private static int ranks(ServerCommandSource source) {
        var titles = NhilProgress.rankNames();
        source.sendFeedback(() -> Text.literal("Nhil ranks (" + titles.size() + ")")
                .formatted(Formatting.GOLD), false);
        for (String title : titles) {
            source.sendFeedback(() -> Text.literal(" - " + title).formatted(Formatting.GRAY), false);
        }
        return titles.size();
    }

    private static int progress(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("/nhil progress needs a player. "
                    + "Run it in game instead.").formatted(Formatting.GRAY), false);
            return 1;
        }
        NhilProgress progress = NhilProgress.of(player);
        source.sendFeedback(() -> Text.literal(progress.rank() + "  |  level " + progress.level()
                + "  |  experience " + progress.experience() + "/" + progress.experienceToNextLevel())
                .formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("Relics " + progress.relics()
                + "  |  landmarks " + progress.discoveries().size())
                .formatted(Formatting.GRAY), false);
        return progress.level();
    }

    private static int grant(ServerCommandSource source, int amount) {
        Collection<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();
        if (players.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No players online to receive experience.")
                    .formatted(Formatting.GRAY), false);
            return 1;
        }
        for (ServerPlayerEntity player : players) {
            NhilProgress.grantExperience(player, amount, "reward");
        }
        source.sendFeedback(() -> Text.literal("Granted " + amount + " Nhil experience to "
                + players.size() + " player(s).").formatted(Formatting.GREEN), false);
        return players.size();
    }

    private static int relic(ServerCommandSource source) {
        Collection<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();
        if (players.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No players online to receive a relic.")
                    .formatted(Formatting.GRAY), false);
            return 1;
        }
        for (ServerPlayerEntity player : players) {
            player.giveItemStack(new net.minecraft.item.ItemStack(NhilContent.ANCIENT_RELIC));
            NhilProgress.registerRelic(player, 1);
        }
        source.sendFeedback(() -> Text.literal("Handed out an ancient relic to "
                + players.size() + " player(s).").formatted(Formatting.GREEN), false);
        return players.size();
    }
}
