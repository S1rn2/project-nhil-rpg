package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.List;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * Items and blocks added by the mod.
 *
 * <p>All art is reused from vanilla textures so the content always renders.
 */
public final class NhilContent {
    private NhilContent() {
    }

    // ------------------------------------------------------------------
    // Blocks
    // ------------------------------------------------------------------

    public static final RegistryKey<Block> RUIN_BRICK_KEY = blockKey("ruin_brick");
    public static final Block RUIN_BRICK = BlockAccess.register(RUIN_BRICK_KEY,
            AbstractBlock.Settings.create()
                    .registryKey(RUIN_BRICK_KEY)
                    .strength(1.6F, 6.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE));

    public static final RegistryKey<Block> MOSSY_RUIN_BRICK_KEY = blockKey("mossy_ruin_brick");
    public static final Block MOSSY_RUIN_BRICK = BlockAccess.register(MOSSY_RUIN_BRICK_KEY,
            AbstractBlock.Settings.create()
                    .registryKey(MOSSY_RUIN_BRICK_KEY)
                    .strength(1.8F, 6.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE));

    public static final RegistryKey<Block> DEEP_RUIN_TILE_KEY = blockKey("deep_ruin_tile");
    public static final Block DEEP_RUIN_TILE = BlockAccess.register(DEEP_RUIN_TILE_KEY,
            AbstractBlock.Settings.create()
                    .registryKey(DEEP_RUIN_TILE_KEY)
                    .strength(2.2F, 9.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.DEEPSLATE_TILES));

    public static final RegistryKey<Block> RUNE_LAMP_KEY = blockKey("rune_lamp");
    public static final Block RUNE_LAMP = BlockAccess.register(RUNE_LAMP_KEY,
            AbstractBlock.Settings.create()
                    .registryKey(RUNE_LAMP_KEY)
                    .strength(2.0F, 9.0F)
                    .requiresTool()
                    .luminance(state -> 12)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static final RegistryKey<Block> RUNE_ALTAR_KEY = blockKey("rune_altar");
    public static final Block RUNE_ALTAR = BlockAccess.register(RUNE_ALTAR_KEY,
            AbstractBlock.Settings.create()
                    .registryKey(RUNE_ALTAR_KEY)
                    .strength(2.4F, 9.0F)
                    .requiresTool()
                    .luminance(state -> 7)
                    .sounds(BlockSoundGroup.DEEPSLATE_TILES),
            NhilAltarBlock::new);

    // ------------------------------------------------------------------
    // Items
    // ------------------------------------------------------------------

    public static final Item RUNE_SHARD = simpleItem("rune_shard",
            new Item.Settings().rarity(Rarity.UNCOMMON));

    public static final Item ANCIENT_RELIC = simpleItem("ancient_relic",
            new Item.Settings().rarity(Rarity.RARE).maxCount(16));

    public static final Item WAYFINDER_COMPASS = ItemAccess.register("wayfinder_compass",
            new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(1),
            WayfinderCompassItem::new);

    public static final Item RUNE_TONIC = simpleItem("rune_tonic",
            NhilConsumables.runeTonic(new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(8)));

    public static final Item NHIL_BLADE = ItemAccess.register("nhil_blade",
            new Item.Settings().rarity(Rarity.RARE).maxCount(1)
                    .sword(ToolMaterial.DIAMOND, 3.0F, -2.4F),
            NhilBladeItem::new);

    public static final List<Item> RELICS = List.of(RUNE_SHARD, ANCIENT_RELIC);

    // ------------------------------------------------------------------
    // Registration helpers
    // ------------------------------------------------------------------

    private static RegistryKey<Block> blockKey(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PGPlugin.MOD_ID, name));
    }

    private static RegistryKey<Item> itemKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PGPlugin.MOD_ID, name));
    }

    private static Item simpleItem(String name, Item.Settings settings) {
        RegistryKey<Item> key = itemKey(name);
        return Registry.register(Registries.ITEM, key, new Item(settings.registryKey(key)));
    }

    private static void registerBlockItem(RegistryKey<Block> blockKey, Block block) {
        RegistryKey<Item> key = itemKey(blockKey.getValue().getPath());
        Registry.register(Registries.ITEM, key, new BlockItem(block, new Item.Settings().registryKey(key)));
    }

    /** Tiny indirection so the static field initialisers above stay readable. */
    static final class BlockAccess {
        private BlockAccess() {
        }

        static Block register(RegistryKey<Block> key, AbstractBlock.Settings settings) {
            return Registry.register(Registries.BLOCK, key, new Block(settings));
        }

        static Block register(RegistryKey<Block> key,
                              AbstractBlock.Settings settings,
                              java.util.function.Function<AbstractBlock.Settings, Block> factory) {
            return Registry.register(Registries.BLOCK, key, factory.apply(settings));
        }
    }

    /** Item factory variant that needs a custom class. */
    static final class ItemAccess {
        private ItemAccess() {
        }

        static Item register(String name, Item.Settings settings, java.util.function.Function<Item.Settings, Item> factory) {
            RegistryKey<Item> key = itemKey(name);
            return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
        }
    }

    // ------------------------------------------------------------------
    // Entry point
    // ------------------------------------------------------------------

    public static void initialize() {
        registerBlockItem(RUIN_BRICK_KEY, RUIN_BRICK);
        registerBlockItem(MOSSY_RUIN_BRICK_KEY, MOSSY_RUIN_BRICK);
        registerBlockItem(DEEP_RUIN_TILE_KEY, DEEP_RUIN_TILE);
        registerBlockItem(RUNE_LAMP_KEY, RUNE_LAMP);
        registerBlockItem(RUNE_ALTAR_KEY, RUNE_ALTAR);

        ItemGroupEvents.modifyEntriesEvent(group("building_blocks")).register(entries -> {
            entries.add(RUIN_BRICK);
            entries.add(MOSSY_RUIN_BRICK);
            entries.add(DEEP_RUIN_TILE);
            entries.add(RUNE_LAMP);
            entries.add(RUNE_ALTAR);
        });
        ItemGroupEvents.modifyEntriesEvent(group("ingredients")).register(entries -> {
            entries.add(RUNE_SHARD);
            entries.add(ANCIENT_RELIC);
        });
        ItemGroupEvents.modifyEntriesEvent(group("food_and_drinks"))
                .register(entries -> entries.add(RUNE_TONIC));
        ItemGroupEvents.modifyEntriesEvent(group("tools_and_utilities"))
                .register(entries -> entries.add(WAYFINDER_COMPASS));
        ItemGroupEvents.modifyEntriesEvent(group("combat")).register(entries -> entries.add(NHIL_BLADE));
    }

    /**
     * Vanilla item group keys are private in 1.21.11, so build the well known ids directly.
     */
    private static RegistryKey<ItemGroup> group(String path) {
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.ofVanilla(path));
    }
}
