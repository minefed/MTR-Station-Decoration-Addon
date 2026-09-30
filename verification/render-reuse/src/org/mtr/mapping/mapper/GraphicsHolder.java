package org.mtr.mapping.mapper;
import top.mcmtr.verification.Trace;
public final class GraphicsHolder {
    public static int widthSalt;
    public static Runnable afterDraw;
    public void push() { Trace.add("push"); }
    public void pop() { Trace.add("pop"); }
    public void translate(double x, double y, double z) { Trace.doubles("translate", x, y, z); }
    public void rotateYDegrees(float angle) { Trace.floats("rotateY", angle); }
    public void rotateZDegrees(float angle) { Trace.floats("rotateZ", angle); }
    public void scale(float x, float y, float z) { Trace.floats("scale", x, y, z); }
    public static int getTextWidth(String text) {
        Trace.add("width:" + text);
        return text.codePointCount(0, text.length()) * 7 + widthSalt;
    }
    public static int getDefaultLight() { Trace.add("defaultLight"); return 0xF000F0; }
    public void drawText(String text, int x, int y, int color, boolean shadow, int light) {
        Trace.add("draw:" + text + ":" + x + ":" + y + ":" + color + ":" + shadow + ":" + light);
        if (afterDraw != null) { afterDraw.run(); }
    }
}
