package net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs;

import net.minecraft.item.Item;

/** Stats for each throwing disc tier. Stat values come from the original Bedrock addon; emerald and obsidian are new. */
public enum DiscTier {
    COPPER("copper", 4.0F, 1.3F, 1.0F, 250),
    IRON("iron", 5.0F, 1.3F, 1.0F, 250),
    GOLDEN("golden", 3.5F, 1.55F, 1.15F, 100),
    DIAMOND("diamond", 6.0F, 1.35F, 1.1F, 1561),
    NETHERITE("netherite", 7.0F, 1.35F, 0.85F, 2031),
    EMERALD("emerald", 5.5F, 1.4F, 1.2F, 600),
    OBSIDIAN("obsidian", 8.0F, 1.25F, 0.75F, 1200);

    private static final DiscTier[] VALUES = values();

    public final String id;
    public final float damage;
    public final float speed;
    public final float pitch;
    public final int durability;
    /** Filled in by {@link ThrowingDiscsContent#initialize()}. */
    public Item item;

    DiscTier(String id, float damage, float speed, float pitch, int durability) {
        this.id = id;
        this.damage = damage;
        this.speed = speed;
        this.pitch = pitch;
        this.durability = durability;
    }

    /** Safe lookup: out-of-range values fall back to copper instead of throwing. */
    public static DiscTier byIndex(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : COPPER;
    }
}
