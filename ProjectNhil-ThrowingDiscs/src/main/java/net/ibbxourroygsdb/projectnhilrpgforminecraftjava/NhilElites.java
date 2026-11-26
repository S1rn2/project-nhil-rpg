package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Marks a small share of hostile mobs as Nhil elites.
 *
 * <p>The marker is written when a mob enters the world and read again on death, so the
 * variant and its reward always agree. Everything happens on entity load and death,
 * never in a per-tick scan.
 */
public final class NhilElites {
    private NhilElites() {
    }

    /** Chance for a freshly loaded monster to become an elite. */
    private static final float ELITE_CHANCE = 0.06F;
    /** Command tag used as the marker, persisted with the entity and visible through /tag. */
    public static final String MARKER = "nhil_elite";

    private static final Identifier HEALTH_ID = Identifier.of(PGPlugin.MOD_ID, "elite_vitality");
    private static final Identifier DAMAGE_ID = Identifier.of(PGPlugin.MOD_ID, "elite_might");

    public static void register() {
        // Producer: decide the variant once, when the mob enters the world.
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof LivingEntity living) || !(entity instanceof Monster)) {
                return;
            }
            if (living.getCommandTags().contains(MARKER)) {
                // Already an elite, for example after a chunk reload. Keep boons, never heal.
                applyBoons(living, false);
                living.setGlowing(true);
                return;
            }
            if (living.getRandom().nextFloat() >= ELITE_CHANCE) {
                return;
            }
            living.addCommandTag(MARKER);
            applyBoons(living, true);
            living.setCustomName(Text.literal("Nhil Elite ").formatted(Formatting.LIGHT_PURPLE)
                    .append(living.getType().getName()));
            living.setCustomNameVisible(false);
            living.setGlowing(true);
        });

        // Consumer: pay out extra progression for the marked variant.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof Monster) || !entity.getCommandTags().contains(MARKER)) {
                return;
            }
            ServerPlayerEntity killer = killerOf(source);
            if (killer == null) {
                return;
            }
            NhilProgress.grantExperience(killer, 8, "elite");
            killer.sendMessage(Text.literal("Elite slain. Its rune power is yours.")
                    .formatted(Formatting.LIGHT_PURPLE), true);
            if (killer.getRandom().nextFloat() < 0.5F) {
                ItemStack tonic = new ItemStack(NhilContent.RUNE_TONIC);
                if (!killer.getInventory().insertStack(tonic)) {
                    killer.dropItem(tonic, false);
                }
            }
        });
    }

    /** Bonus health and attack power for an elite. Safe to call more than once. */
    private static void applyBoons(LivingEntity entity, boolean heal) {
        if (!(entity.getEntityWorld() instanceof ServerWorld)) {
            return;
        }
        EntityAttributeInstance health = entity.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_ID) == null) {
            health.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_ID, 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (heal) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
        EntityAttributeInstance damage = entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (damage != null && damage.getModifier(DAMAGE_ID) == null) {
            damage.addPersistentModifier(new EntityAttributeModifier(
                    DAMAGE_ID, 0.35, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private static ServerPlayerEntity killerOf(DamageSource source) {
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            return player;
        }
        return null;
    }
}
