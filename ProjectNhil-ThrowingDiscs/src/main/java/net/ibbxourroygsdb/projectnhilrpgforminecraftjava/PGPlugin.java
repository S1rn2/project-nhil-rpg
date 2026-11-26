package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs.ThrowingDiscsContent;

/**
 * Shared entry point. Runs on both dedicated servers and in single player.
 */
public final class PGPlugin implements ModInitializer {
    public static final String MOD_ID = "projectnhilrpgforminecraftjava";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Project Nhil RPG for Minecraft Java");

        // Touching the class registers the persistent attachment type during init.
        NhilProgress.bootstrap();

        NhilContent.initialize();
        ThrowingDiscsContent.initialize();
        NhilGameplay.register();
        NhilElites.register();
        NhilCommands.register();
        NhilLoot.register();
    }
}
