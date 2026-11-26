package net.ibbxourroygsdb.projectnhilrpgforminecraftjava;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Rune Altar: spend a rune shard to convert it into Nhil experience.
 * <p>All work happens on the server only, so multiplayer clients stay in sync.
 */
public class NhilAltarBlock extends Block {
    /** Experience granted for each shard offered. */
    public static final int XP_PER_SHARD = 10;

    public NhilAltarBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                         PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.SUCCESS;
        }
        if (!stack.isOf(NhilContent.RUNE_SHARD)) {
            serverPlayer.sendMessage(Text.literal("The altar hums. Offer a rune shard to awaken it.")
                    .formatted(Formatting.GRAY), true);
            return ActionResult.FAIL;
        }

        stack.decrementUnlessCreative(1, serverPlayer);
        NhilProgress.grantExperience(serverPlayer, XP_PER_SHARD, "altar");

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.BLOCKS, 1.0F, 1.2F);
            serverWorld.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5,
                    pos.getY() + 1.1, pos.getZ() + 0.5, 14, 0.4, 0.3, 0.4, 0.02);
        }
        serverPlayer.sendMessage(Text.literal("The rune shard dissolves into " + XP_PER_SHARD + " experience.")
                .formatted(Formatting.AQUA), true);
        return ActionResult.SUCCESS;
    }
}
