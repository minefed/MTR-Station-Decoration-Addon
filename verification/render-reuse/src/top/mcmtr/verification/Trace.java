package top.mcmtr.verification;
import java.util.ArrayList;
import java.util.List;
public final class Trace {
    public static final List<String> EVENTS = new ArrayList<>();
    public static void add(String event) { EVENTS.add(event); }
    public static void doubles(String operation, double... values) {
        StringBuilder event = new StringBuilder(operation);
        for (double value : values) { event.append(':').append(Long.toHexString(Double.doubleToRawLongBits(value))); }
        add(event.toString());
    }
    public static void floats(String operation, float... values) {
        StringBuilder event = new StringBuilder(operation);
        for (float value : values) { event.append(':').append(Integer.toHexString(Float.floatToRawIntBits(value))); }
        add(event.toString());
    }
}
