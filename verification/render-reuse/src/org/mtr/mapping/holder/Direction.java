package org.mtr.mapping.holder;
public enum Direction {
    NORTH(0, -1, 180), SOUTH(0, 1, 0), WEST(-1, 0, 90), EAST(1, 0, 270);
    public final int x, z;
    private final float rotation;
    Direction(int x, int z, float rotation) { this.x = x; this.z = z; this.rotation = rotation; }
    public float asRotation() { return rotation; }
    public Direction getOpposite() { return values()[ordinal() ^ 1]; }
}
