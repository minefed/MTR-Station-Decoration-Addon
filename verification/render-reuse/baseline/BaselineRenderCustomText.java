// Frozen renderer from 91cdd24f6fbf94a1b12f5432ce93d7168246e769, class renamed only.
package top.mcmtr.mod.render;

import org.mtr.core.tool.Utilities;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.Vector3d;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.BlockEntityRenderer;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.data.IGui;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.QueuedRenderLayer;
import top.mcmtr.mod.blocks.BlockCustomTextBase;
import top.mcmtr.mod.config.Config;

public class BaselineRenderCustomText<T extends BlockCustomTextBase.BlockCustomTextEntity> extends BlockEntityRenderer<T> implements IGui, Utilities {
    private final int maxArrivals;
    private final float scale;
    private final float fRowScale;
    private final float sRowScale;
    private final float rowSpacing;
    private final float startX;
    private final float startY;
    private final float startZ;
    private final float maxHeight;
    private final float maxWidth;
    private final boolean rotate90;
    private final int[] colors;

    /**
     * (总行高x0.8) / (行数x缩放倍数) = 文字高度
     */
    public BaselineRenderCustomText(Argument argument, int maxArrivals, float startX, float startY, float startZ, float maxHeight, float maxWidth, boolean rotate90, float textPadding, float fRowTextPadding, float sRowTextPadding, float rowSpacing, int... colors) {
        super(argument);
        this.maxArrivals = maxArrivals;
        this.scale = 160 * maxArrivals / maxHeight * textPadding;
        this.fRowScale = 160 * maxArrivals / maxHeight * fRowTextPadding;
        this.sRowScale = 160 * maxArrivals / maxHeight * sRowTextPadding;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
        this.rotate90 = rotate90;
        this.rowSpacing = rowSpacing;
        this.colors = new int[maxArrivals];
        System.arraycopy(colors, 0, this.colors, 0, Math.min(colors.length, maxArrivals));
        if (colors.length < maxArrivals) {
            for (int i = colors.length; i < maxArrivals; i++) {
                this.colors[i] = colors[colors.length - 1];
            }
        }
    }

    @Override
    public void render(T entity, float tickDelta, GraphicsHolder ignored, int light, int overlay) {
        final World world = entity.getWorld2();
        if (world == null) {
            return;
        }
        final BlockPos blockPos = entity.getPos2();
        final Direction facing = IBlock.getStatePropertySafe(world, blockPos, BlockCustomTextBase.FACING);
        MainRenderer.scheduleRender(QueuedRenderLayer.TEXT, ((graphicsHolder, offset) -> {
            render(entity, graphicsHolder, blockPos, offset, facing, false);
            render(entity, graphicsHolder, blockPos.offset(facing), offset, facing.getOpposite(), true);
        }));
    }

    private void render(T entity, GraphicsHolder graphicsHolder, BlockPos blockPos, Vector3d offset, Direction facing, boolean rightAlign) {
        for (int i = 0; i < maxArrivals; i++) {
            String[] text = entity.getMessage(i).split("\\|");
            final String bigText = text[0];
            String smallText = null;
            float finalBigScale = scale;
            float finalSmallScale = 0;
            if (text.length > 1) {
                smallText = text[text.length - 1];
                finalBigScale = fRowScale;
                finalSmallScale = sRowScale;
            }
            graphicsHolder.push();
            graphicsHolder.translate(blockPos.getX() - offset.getXMapped() + 0.5, blockPos.getY() - offset.getYMapped(), blockPos.getZ() - offset.getZMapped() + 0.5);
            graphicsHolder.rotateYDegrees((rotate90 ? 90 : 0) - facing.asRotation());
            graphicsHolder.rotateZDegrees(180);
            graphicsHolder.translate(rightAlign ? (13 - startX) / 16 : (startX - 8) / 16, -startY / 16 + i * maxHeight / maxArrivals / 16, (startZ - 8) / 16 - SMALL_OFFSET * 2);
            renderText(graphicsHolder, bigText, colors[i], maxWidth * finalBigScale / 16, 0, finalBigScale, rightAlign);
            if (smallText != null) {
                renderText(graphicsHolder, smallText, colors[i], maxWidth * finalSmallScale / 16, 8 / finalBigScale + rowSpacing, finalSmallScale, rightAlign);
            }
            graphicsHolder.pop();
        }
    }

    private static void renderText(GraphicsHolder graphicsHolder, String text, int color, float maxWidth, double y, float scale, boolean rightAlign) {
        graphicsHolder.push();
        graphicsHolder.translate(0, y, 0);
        graphicsHolder.scale(1 / scale, 1 / scale, 1 / scale);
        final int textWidth = GraphicsHolder.getTextWidth(text);
        if (textWidth > maxWidth) {
            graphicsHolder.scale(maxWidth / textWidth, 1, 1);
        }
        graphicsHolder.drawText(text, rightAlign ? Math.max(0, (int) maxWidth - textWidth) : 0, 0, color | ARGB_BLACK, false, GraphicsHolder.getDefaultLight());
        graphicsHolder.pop();
    }

    @Override
    public int getRenderDistance2() {
        return Config.getCustomTextSignMaxViewDistance();
    }
}