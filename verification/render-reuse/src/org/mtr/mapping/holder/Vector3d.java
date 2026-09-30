package org.mtr.mapping.holder;
public final class Vector3d {
    private final double x, y, z;
    public Vector3d(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    public double getXMapped() { return x; }
    public double getYMapped() { return y; }
    public double getZMapped() { return z; }
}
