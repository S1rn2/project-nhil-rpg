package net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registers the throwing discs. Items and the entity keep the original "throwingdiscs" namespace so
 * item ids and recipes stay the same as in the standalone Throwing Discs mod.
 */
public final class ThrowingDiscsContent {
    public static final String NAMESPACE = "throwingdiscs";

    public static EntityType<ThrowingDiscEntity> DISC_ENTITY;

    private ThrowingDiscsContent() {
    }

    public static Identifier id(String path) {
        return Identifier.of(NAMESPACE, path);
    }

    public static void initialize() {
        RegistryKey<EntityType<?>> entityKey = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id("throwing_disc"));
        DISC_ENTITY = Registry.register(Registries.ENTITY_TYPE, entityKey,
                EntityType.Builder.<ThrowingDiscEntity>create(ThrowingDiscEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(64)
                        .trackingTickInterval(1)
                        .build(entityKey));

        for (DiscTier tier : DiscTier.values()) {
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(tier.id + "_throwing_disc"));
            tier.item = Registry.register(Registries.ITEM, key,
                    new ThrowingDiscItem(tier, new Item.Settings().registryKey(key).maxDamage(tier.durability)));
        }

        // Vanilla ItemGroups constants are private in 1.21.11, so build the key from its id (same as NhilContent).
        RegistryKey<ItemGroup> combat = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("combat"));
        ItemGroupEvents.modifyEntriesEvent(combat).register(entries -> {
            for (DiscTier tier : DiscTier.values()) {
                entries.add(tier.item);
            }
        });
    }
}
