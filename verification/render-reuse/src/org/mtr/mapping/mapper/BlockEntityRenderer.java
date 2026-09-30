package org.mtr.mapping.mapper;
public abstract class BlockEntityRenderer<T> {
    public static final class Argument {}
    protected BlockEntityRenderer(Argument argument) {}
    public abstract void render(T entity, float tickDelta, GraphicsHolder holder, int light, int overlay);
    public int getRenderDistance2() { return 0; }
}
