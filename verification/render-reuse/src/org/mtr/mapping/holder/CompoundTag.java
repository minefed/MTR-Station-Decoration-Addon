package org.mtr.mapping.holder;
import java.util.HashMap;
import java.util.Map;
import top.mcmtr.verification.Trace;
public final class CompoundTag {
    private final Map<String, String> values = new HashMap<>();
    public String getString(String key) { Trace.add("nbt.read:" + key); return values.getOrDefault(key, ""); }
    public void putString(String key, String value) { Trace.add("nbt.write:" + key + ":" + value); values.put(key, value); }
}
