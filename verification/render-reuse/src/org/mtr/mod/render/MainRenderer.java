package org.mtr.mod.render;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.mtr.mapping.holder.Vector3d;
import org.mtr.mapping.mapper.GraphicsHolder;
import top.mcmtr.verification.Trace;
public final class MainRenderer {
    public static final List<BiConsumer<GraphicsHolder, Vector3d>> QUEUE = new ArrayList<>();
    public static void scheduleRender(QueuedRenderLayer layer, BiConsumer<GraphicsHolder, Vector3d> callback) {
        Trace.add("schedule:" + layer); QUEUE.add(callback);
    }
    public static void flush(Vector3d offset) {
        for (BiConsumer<GraphicsHolder, Vector3d> callback : new ArrayList<>(QUEUE)) {
            callback.accept(new GraphicsHolder(), offset);
        }
        QUEUE.clear();
    }
}
