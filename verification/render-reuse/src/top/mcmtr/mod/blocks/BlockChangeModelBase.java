package top.mcmtr.mod.blocks;
import org.mtr.mapping.holder.*;
public abstract class BlockChangeModelBase {
    protected BlockChangeModelBase(BlockSettings settings, int count) {}
    public abstract ActionResult onUse2(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit);
}
