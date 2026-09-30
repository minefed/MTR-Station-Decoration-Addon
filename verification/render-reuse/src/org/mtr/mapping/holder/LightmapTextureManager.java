package org.mtr.mapping.holder;
import top.mcmtr.verification.Trace;
public final class LightmapTextureManager {
    public static int pack(int block, int sky) { Trace.add("pack:" + block + ":" + sky); return block << 4 | sky << 20; }
}
