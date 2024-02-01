package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.block.AbstractCrudeTorchBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class CrudeTorchBlockEntity extends BlockEntity implements BlockEntityProvider {
    private long extinguishTime;
    private static final String LIT_NBT = "hcs_torch_last_lit";
    public static final long MAX_BURNING_LENGTH = 24000 * 2;

    public CrudeTorchBlockEntity(BlockPos pos, BlockState state) {
        super(Reg.CRUDE_TORCH_BLOCK_ENTITY, pos, state);
        this.igniteSync();
    }

    public boolean shouldExtinguish() {
        if (this.extinguishTime == 0L || this.world == null) return false;
        return this.world.getTime() - this.extinguishTime > MAX_BURNING_LENGTH;
    }

    public void igniteSync() {
        this.extinguishTime = applyNullable(this.getWorld(), World::getTime, 0L) + MAX_BURNING_LENGTH;
    }

    public void extinguishSync() {
        this.extinguishTime = 0L;
    }

    @SuppressWarnings("unused")
    public static void tick(@NotNull World world, BlockPos pos, BlockState state, BlockEntity entity) {
        if (world.getBlockEntity(pos) instanceof CrudeTorchBlockEntity torch) {
            if (torch.shouldExtinguish()) AbstractCrudeTorchBlock.extinguish(world, state, pos);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { // This method usually won't be called
        return new CrudeTorchBlockEntity(pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(LIT_NBT, NbtElement.LONG_TYPE)) this.extinguishTime = nbt.getLong(LIT_NBT);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong(LIT_NBT, this.extinguishTime);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    //TODO check nbt.putLong(LIT_NBT,this.lastLitTime);
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong(LIT_NBT, this.extinguishTime);
        return nbt;
    }
}