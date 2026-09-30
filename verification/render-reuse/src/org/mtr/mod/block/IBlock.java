package org.mtr.mod.block;
import org.mtr.mapping.holder.*;
import top.mcmtr.verification.Trace;
public interface IBlock {
    static Direction getStatePropertySafe(World world, BlockPos pos, Object property) {
        Trace.add("facing:" + pos); return world.facing;
    }
    static ActionResult checkHoldingBrush(World world, PlayerEntity player, Runnable action) {
        action.run(); return ActionResult.SUCCESS;
    }
}
