package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"deprecation", "unused"})
public class CrudeTorchBlock extends TorchBlock {
    // Extinguished crude torch block
    public CrudeTorchBlock(AbstractBlock.Settings settings) {
        super(settings, ParticleTypes.FLAME);
    }

    public static boolean onLit(@NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        //TODO fire bow lit
        //TODO use lava or fire to lit while holding crude torches
        final boolean isFireCharge = item == Items.FIRE_CHARGE, isFlintAndSteel = item == Items.FLINT_AND_STEEL;
        final boolean isTorchOrCandle = item == Reg.BURNING_CRUDE_TORCH_ITEM || (item instanceof BlockItem blockItem && (blockItem.getBlock() instanceof CandleBlock || (blockItem.getBlock() instanceof TorchBlock && item != Items.REDSTONE_TORCH && item != Reg.CRUDE_TORCH_ITEM)));
        if (isFlintAndSteel || isFireCharge || isTorchOrCandle) {
            if (!player.isCreative()) {
                if (isFlintAndSteel) stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                else if (isFireCharge) stack.decrement(1);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            return true;
        }
        return false;
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (onLit(world, pos, player, hand)) {
            world.setBlockState(pos, state.getBlock().getDefaultState());
            if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                torch.onLit(player);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public Item asItem() {
        return Reg.CRUDE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.CRUDE_TORCH_BLOCK;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Just do nothing
    }
}
