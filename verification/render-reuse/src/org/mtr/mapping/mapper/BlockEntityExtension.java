package org.mtr.mapping.mapper;
import org.mtr.mapping.holder.*;
import top.mcmtr.verification.Trace;
public abstract class BlockEntityExtension {
    public World world;
    private final BlockPos pos;
    public BlockEntityExtension(BlockEntityType<?> type, BlockPos pos, BlockState state) { this.pos = pos; }
    public World getWorld2() { Trace.add("entity.world"); return world; }
    public BlockPos getPos2() { Trace.add("entity.pos"); return pos; }
    public void markDirty2() { Trace.add("entity.dirty"); }
    public abstract void readCompoundTag(CompoundTag tag);
    public abstract void writeCompoundTag(CompoundTag tag);
    public double getRenderDistance2() { return 0; }
}
