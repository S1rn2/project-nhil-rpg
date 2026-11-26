package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.List;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

/**
 * Adds ruin-themed loot to vanilla abandoned structures, so exploring pays off.
 */
public final class NhilLoot {
    private NhilLoot() {
    }

    /** Chest tables that should sometimes contain a rune shard. */
    private static final List<String> RUIN_CHESTS = List.of(
            "chests/ruined_portal",
            "chests/desert_pyramid",
            "chests/jungle_temple",
            "chests/igloo_chest",
            "chests/shipwreck_treasure",
            "chests/simple_dungeon",
            "chests/abandoned_mineshaft",
            "chests/pillager_outpost",
            "chests/ancient_city");

    private static final List<String> TREASURE_CHESTS = List.of(
            "chests/buried_treasure",
            "chests/ancient_city",
            "chests/trial_chambers/reward_ominous_unique",
            "chests/stronghold_library");

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            Identifier id = key.getValue();
            if (!"minecraft".equals(id.getNamespace())) {
                return;
            }
            String path = id.getPath();
            if (RUIN_CHESTS.contains(path)) {
                addPool(tableBuilder, NhilContent.RUNE_SHARD, 0.35F, 1, 3);
                addPool(tableBuilder, NhilContent.RUNE_TONIC, 0.20F, 1, 2);
            }
            if (TREASURE_CHESTS.contains(path)) {
                addPool(tableBuilder, NhilContent.ANCIENT_RELIC, 0.25F, 1, 1);
            }
        });
    }

    private static void addPool(LootTable.Builder builder,
                                net.minecraft.item.Item item,
                                float chance,
                                int min,
                                int max) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(min))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(item));
        if (max > min) {
            pool.bonusRolls(ConstantLootNumberProvider.create(max - min));
        }
        builder.pool(pool);
    }
}
