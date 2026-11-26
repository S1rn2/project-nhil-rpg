package net.ibbxourroygsdb.projectnhilrpgforminecraftjava.discs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ThrowingDiscItem extends Item {
    private final DiscTier tier;

    public ThrowingDiscItem(DiscTier tier, Item.Settings settings) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        ItemStack held = user.getStackInHand(hand);
        ThrowingDiscEntity disc = new ThrowingDiscEntity(ThrowingDiscsContent.DISC_ENTITY, world);
        disc.launch(user, held.copyWithCount(1), tier);
        // The disc item rides inside the entity and comes back to the thrower when caught.
        held.decrement(1);
        world.spawnEntity(disc);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 0.6F, tier.pitch);
        return ActionResult.SUCCESS;
    }
}
