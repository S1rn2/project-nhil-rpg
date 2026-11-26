package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Persistent adventuring progress for a single player.
 *
 * <p>Stored through a Fabric data attachment so it survives relogging, server restarts and death.
 */
public final class NhilProgress {
    /** Experience needed to advance from level 1 to level 2. */
    public static final int XP_PER_LEVEL = 15;
    /** Extra experience each further level demands. */
    public static final int XP_STEP = 2;
    /** Highest level a wanderer can reach. */
    public static final int MAX_LEVEL = 20;

    /** Adventuring ranks, from the first steps to the endgame. */
    private static final String[][] RANKS = {
            {"Wanderer", "1"},
            {"Delver", "3"},
            {"Ruin Seeker", "6"},
            {"Relic Hunter", "10"},
            {"Nhil Warden", "15"},
            {"Nhil Ascendant", "20"},
    };
    public static final Identifier HEALTH_MODIFIER_ID =
            Identifier.of(PGPlugin.MOD_ID, "level_vitality");

    public static final Codec<NhilProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(NhilProgress::level),
            Codec.INT.fieldOf("experience").forGetter(NhilProgress::experience),
            Codec.INT.fieldOf("relics").forGetter(NhilProgress::relics),
            Codec.STRING.listOf().fieldOf("discoveries").forGetter(NhilProgress::discoveries)
    ).apply(instance, NhilProgress::new));

    public static final AttachmentType<NhilProgress> ATTACHMENT = AttachmentRegistry
            .<NhilProgress>builder()
            .persistent(CODEC)
            .copyOnDeath()
            .syncWith(net.minecraft.network.codec.PacketCodecs.registryCodec(CODEC),
                    net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.all())
            .buildAndRegister(Identifier.of(PGPlugin.MOD_ID, "progress"));

    private final int level;
    private final int experience;
    private final int relics;
    private final List<String> discoveries;

    public NhilProgress(int level, int experience, int relics, List<String> discoveries) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
        this.experience = Math.max(0, experience);
        this.relics = Math.max(0, relics);
        this.discoveries = List.copyOf(discoveries);
    }

    public static NhilProgress fresh() {
        return new NhilProgress(1, 0, 0, List.of());
    }

    /** Forces this class to load so the attachment type is registered before the registries freeze. */
    public static void bootstrap() {
        if (ATTACHMENT == null) {
            throw new IllegalStateException("attachment type failed to register");
        }
    }

    public int level() {
        return this.level;
    }

    public int experience() {
        return this.experience;
    }

    public int relics() {
        return this.relics;
    }

    public List<String> discoveries() {
        return this.discoveries;
    }

    /** Experience still needed before the next level. */
    public int experienceToNextLevel() {
        return xpForLevel(this.level);
    }

    /** Experience required to advance out of the given level. */
    public static int xpForLevel(int level) {
        if (level >= MAX_LEVEL) {
            return 0;
        }
        return XP_PER_LEVEL + (Math.max(1, level) - 1) * XP_STEP;
    }

    /** Adventuring title that matches the current level. */
    public String rank() {
        return rankForLevel(this.level);
    }

    /** Adventuring title for any level, from Wanderer to Nhil Ascendant. */
    public static String rankForLevel(int level) {
        String title = RANKS[0][0];
        for (String[] rank : RANKS) {
            if (level >= Integer.parseInt(rank[1])) {
                title = rank[0];
            }
        }
        return title;
    }

    /** Every rank and the level that unlocks it, in ascending order. */
    public static List<String> rankNames() {
        List<String> names = new ArrayList<>();
        for (String[] rank : RANKS) {
            names.add(rank[0] + " (level " + rank[1] + ")");
        }
        return names;
    }

    public NhilProgress withExperience(int amount) {
        return new NhilProgress(this.level, amount, this.relics, this.discoveries);
    }

    public NhilProgress withLevel(int value) {
        return new NhilProgress(value, this.experience, this.relics, this.discoveries);
    }

    public NhilProgress withRelics(int value) {
        return new NhilProgress(this.level, this.experience, value, this.discoveries);
    }

    public NhilProgress withDiscoveries(List<String> values) {
        return new NhilProgress(this.level, this.experience, this.relics, values);
    }

    public boolean knows(String discovery) {
        return this.discoveries.contains(discovery);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    public static NhilProgress of(ServerPlayerEntity player) {
        return player.getAttachedOrCreate(ATTACHMENT, NhilProgress::fresh);
    }

    public static void store(ServerPlayerEntity player, NhilProgress progress) {
        player.setAttached(ATTACHMENT, progress);
        syncHealth(player, progress.level());
    }

    /**
     * Grants experience, handling any number of level ups, and reports them to the player.
     */
    public static void grantExperience(ServerPlayerEntity player, int amount, String reason) {
        if (amount <= 0) {
            return;
        }
        NhilProgress progress = of(player);
        int total = progress.experience() + amount;
        int level = progress.level();
        String oldRank = rankForLevel(level);
        while (level < MAX_LEVEL && total >= xpForLevel(level)) {
            total -= xpForLevel(level);
            level++;
        }
        if (level >= MAX_LEVEL) {
            total = 0;
        }
        NhilProgress updated = new NhilProgress(level, total, progress.relics(), progress.discoveries());
        store(player, updated);

        if (level > progress.level()) {
            String newRank = rankForLevel(level);
            player.sendMessage(Text.literal("Nhil level " + level + " reached")
                    .formatted(Formatting.GOLD)
                    .append(Text.literal(" (" + reason + ")").formatted(Formatting.GRAY)), false);
            player.sendMessage(Text.literal("Your vitality grows. Maximum health increased.")
                    .formatted(Formatting.YELLOW), true);
            if (!newRank.equals(oldRank)) {
                player.sendMessage(Text.literal("Rank earned: " + newRank)
                        .formatted(Formatting.AQUA), false);
            }
        } else if (amount >= 5) {
            player.sendMessage(Text.literal("+" + amount + " Nhil experience")
                    .formatted(Formatting.GRAY), true);
        }
    }

    /** Applies the permanent max health bonus that scales with level. */
    public static void syncHealth(ServerPlayerEntity player, int level) {
        EntityAttributeInstance health = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        health.removeModifier(HEALTH_MODIFIER_ID);
        if (level > 1) {
            health.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_ID, level - 1, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void registerRelic(ServerPlayerEntity player, int amount) {
        NhilProgress progress = of(player);
        store(player, progress.withRelics(progress.relics() + amount));
    }

    public static boolean registerDiscovery(ServerPlayerEntity player, String discovery) {
        NhilProgress progress = of(player);
        if (progress.knows(discovery)) {
            return false;
        }
        List<String> known = new ArrayList<>(progress.discoveries());
        known.add(discovery);
        store(player, progress.withDiscoveries(known));
        return true;
    }
}
