package org.mtr.mapping.holder;
/** Query-boundary fixture only. Real bundled and resolved enum contracts are verified separately. */
public enum LightType {
    SKY, BLOCK;
    public static int helperCalls;
    public static LightType getBlockMapped() { helperCalls++; return BLOCK; }
    public static LightType getSkyMapped() { helperCalls++; return SKY; }
}
