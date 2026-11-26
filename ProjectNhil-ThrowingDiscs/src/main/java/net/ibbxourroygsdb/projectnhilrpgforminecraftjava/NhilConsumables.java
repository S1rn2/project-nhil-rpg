package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import java.util.List;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundEvents;

/**
 * Factory for the Rune Tonic: a drinkable field remedy.
 *
 * <p>Built from vanilla consumable components so it works in every game mode.
 */
final class NhilConsumables {
    private NhilConsumables() {
    }

    /** Two hearts of healing plus short regeneration, at the cost of a shard. */
    static Item.Settings runeTonic(Item.Settings settings) {
        FoodComponent food = new FoodComponent(2, 1.0F, false);
        ConsumableComponent consumable = ConsumableComponents.drink()
                .consumeSeconds(1.4F)
                .useAction(UseAction.DRINK)
                .sound(SoundEvents.ENTITY_GENERIC_DRINK)
                .consumeParticles(true)
                .consumeEffect(new ApplyEffectsConsumeEffect(List.of(
                        new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0),
                        new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0)), 1.0F))
                .build();
        return settings.food(food, consumable);
    }
}
