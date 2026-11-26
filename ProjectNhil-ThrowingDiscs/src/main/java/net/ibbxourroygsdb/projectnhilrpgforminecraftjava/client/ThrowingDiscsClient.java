package net.ibbxourroygsdb.projectnhilrpgforminecraftjava.client;

import net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs.ThrowingDiscEntity;
import net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs.ThrowingDiscsContent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/** Draws a flying disc as its item sprite. */
public final class ThrowingDiscsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ThrowingDiscsContent.DISC_ENTITY,
                ctx -> new FlyingItemEntityRenderer<ThrowingDiscEntity>(ctx, 0.9F, true));
    }
}
