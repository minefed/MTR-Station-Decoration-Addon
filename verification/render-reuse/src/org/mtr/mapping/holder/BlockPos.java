package org.mtr.mapping.holder;
public final class BlockPos {
    private final int x, y, z;
    public BlockPos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public BlockPos offset(Direction direction) { return new BlockPos(x + direction.x, y, z + direction.z); }
    public String toString() { return x + "," + y + "," + z; }
}
