package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.WorldEvents;

import java.util.Objects;


public class AttackBlockEvent {
    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            ItemStack mainHandStack = player.getMainHandStack();
            Item mainHand = mainHandStack.getItem();
            if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
                if (blockEntity instanceof ChestBlockEntity) {
                    ChestBlock chestBlock = (ChestBlock) state.getBlock();
                    if (!Objects.requireNonNull(ChestBlock.getInventory(chestBlock, state, world, pos, true)).isEmpty()) {//break without empty
                        EntityHelper.msgById(player, "hcs.tip.cant_break_chest");
                        Reg.LOGGER.info("Don't worry when the mismatch warning comes out. It is just a normal result after prevent player from attacking a nonempty chest.");
                        return ActionResult.SUCCESS;
                    }
                }

                if ((state.isToolRequired() && !state.isOf(Blocks.COBWEB)) || block == Blocks.BEDROCK) {
                    if (mainHand == Reg.ROCK) {
                        // Add sound and particles
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                        mainHandStack.decrement(1);
                        if (Math.random() < (0.5 + player.experienceLevel / 40.0)) {
                            EntityHelper.dropItem(player, Reg.SHARP_ROCK);
                            EntityHelper.msgById(player, "hcs.tip.chip_succeed");
                        } else EntityHelper.msgById(player, "hcs.tip.chip_failed");
                    } else if (mainHand == Items.FLINT) {
                        mainHandStack.decrement(1);
                        if (Math.random() < (0.25 + player.experienceLevel / 40.0)) {
                            EntityHelper.dropItem(player, Reg.SHARP_FLINT);
                            EntityHelper.msgById(player, "hcs.tip.chip_succeed");
                        } else EntityHelper.msgById(player, "hcs.tip.chip_failed");
                    } else if (mainHand == Items.BONE) {
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                        mainHandStack.decrement(1);
                        EntityHelper.msgById(player, "hcs.tip.chip_succeed");
                        EntityHelper.dropItem(player, Reg.SHARP_BROKEN_BONE);
                    }
                }

                if (RotHelper.canRot(mainHand)) {
                    String name = mainHandStack.getItem().getName().toString();
                    if (RotHelper.getFresh(world, mainHandStack) <= 0.0001F && RotHelper.getPackageType(name) == 1) {
                        if (name.contains("stew") || name.contains("salad") || name.contains("soup")) {
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BOWL, mainHandStack.getCount()));
                            EntityHelper.dropItem(player, Reg.ROT);
                        }
                        if (name.contains("bucket"))
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BUCKET, mainHandStack.getCount()));
                        if (name.contains("bottle") || name.contains("juice"))
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, mainHandStack.getCount()));
                    }
                }

            }
            return ActionResult.PASS;
        });
    }
}
