package net.ibbxourroygsdb.projectnhilrpgforminecraftjava.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.ibbxourroygsdb.projectnhilrpgforminecraftjava.NhilProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Client side only: a small level readout next to the hotbar.
 */
public final class NhilClient implements ClientModInitializer {
    private static final Identifier HUD_ID =
            Identifier.of("projectnhilrpgforminecraftjava", "progress_hud");

    @Override
    public void onInitializeClient() {
        // Make sure the attachment type this HUD reads is registered on the client too.
        NhilProgress.bootstrap();
        HudElementRegistry.addLast(HUD_ID, (context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden || client.currentScreen != null) {
            return;
        }
        NhilProgress progress = player.getAttached(NhilProgress.ATTACHMENT);
        if (progress == null) {
            return;
        }

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int x = width / 2 - 91;
        int y = height - 46;

        Text label = Text.literal("Lv " + progress.level())
                .formatted(Formatting.GOLD);
        context.drawTextWithShadow(client.textRenderer, label, x, y, 0xFFFFFF);

        // Slim experience bar so the next level is always readable at a glance.
        int needed = progress.experienceToNextLevel();
        if (needed > 0) {
            int barWidth = 60;
            int filled = Math.min(barWidth, (progress.experience() * barWidth) / needed);
            context.fill(x, y + 10, x + barWidth, y + 12, 0x44000000);
            if (filled > 0) {
                context.fill(x, y + 10, x + filled, y + 12, 0xFFAA00AA);
            }
        } else {
            context.drawTextWithShadow(client.textRenderer,
                    Text.literal("Max").formatted(Formatting.LIGHT_PURPLE), x + 20, y, 0xFFFFFF);
        }

        if (progress.relics() > 0) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.literal("Relics " + progress.relics()).formatted(Formatting.AQUA),
                    x, y - 10, 0xFFFFFF);
        }
    }
}
