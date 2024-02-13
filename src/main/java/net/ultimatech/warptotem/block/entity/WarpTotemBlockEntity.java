package net.ultimatech.warptotem.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class WarpTotemBlockEntity extends BlockEntity {

    private int[] lodestonePos = new int[3];

    public WarpTotemBlockEntity(BlockEntityType blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public WarpTotemBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(WTBlockEntities.WARP_TOTEM_BLOCK_ENTITIES, blockPos, blockState);
    }

    public BlockEntityType<WarpTotemBlockEntity> getType() {
        return WTBlockEntities.WARP_TOTEM_BLOCK_ENTITIES;
    }

    // --- NBT Management --- //
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.lodestonePos = nbt.getIntArray("LodestonePos");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putIntArray("LodestonePos", this.lodestonePos);
    }

    public void setLodestonePos(BlockPos blockPos) {
        if (blockPos != null) {
            this.lodestonePos[0] = blockPos.getX();
            this.lodestonePos[1] = blockPos.getY();
            this.lodestonePos[2] = blockPos.getZ();
        }
    }

    public BlockPos getLodestonePos() {
        return new BlockPos(this.lodestonePos[0], this.lodestonePos[1], this.lodestonePos[2]);
    }
}
