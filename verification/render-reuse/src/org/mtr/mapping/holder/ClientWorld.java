package org.mtr.mapping.holder;
import top.mcmtr.verification.Trace;
public final class ClientWorld {
    public int reads;
    public int generation;
    public int failAt = -1;
    public int getLightLevel(LightType type, BlockPos pos) {
        Trace.add("light:" + type + ":" + pos);
        if (reads++ == failAt) { throw new IllegalStateException("light failure"); }
        return Math.floorMod(generation + reads + pos.getX() + pos.getY() + pos.getZ() + type.ordinal(), 16);
    }
}
