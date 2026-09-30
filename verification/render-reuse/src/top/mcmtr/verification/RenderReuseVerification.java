package top.mcmtr.verification;

import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.BlockEntityRenderer;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.render.MainRenderer;
import top.mcmtr.mod.blocks.BlockCustomTextBase.BlockCustomTextEntity;
import top.mcmtr.mod.blocks.BlockCustomTextBase.BlockCustomTextEntity.MessageParts;
import top.mcmtr.mod.render.BaselineRenderCustomText;
import top.mcmtr.mod.render.RenderCustomText;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntConsumer;

/** Executes production entry points with recording Minecraft/MTR boundaries. */
public final class RenderReuseVerification {
    private static int assertions;
    private static int renderComparisons;
    private static int lightComparisons;
    private static final Vector3d OFFSET = new Vector3d(-91.125, 61.875, 1_000_000.0625);

    public static void main(String[] args) throws Exception {
        renderTraceEquivalence();
        scheduledReadTiming();
        cacheIdentityAndOwnership();
        nbtAndEditInvalidation();
        worldAndEntityLifecycles();
        lightQueryEquivalence();
        System.out.println("PASS: " + renderComparisons + " complete scheduled-render trace comparisons, "
                + lightComparisons + " production light-query comparisons, " + assertions + " assertions");
    }

    private static void renderTraceEquivalence() throws Exception {
        String[] texts = {null, "", "plain", "|", "||", "|||", "a|", "a||", "|a", "|a|", "a||b||",
                "A|ignored|B", "서울|Seoul", "東京|とうきょう", "🚆|Station 🚉", "line\nbreak|small", "\\|x", "a\u0000b|c"};
        BlockPos[] positions = {new BlockPos(1, 2, 3), new BlockPos(-300, -64, 700),
                new BlockPos(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)};
        for (Direction facing : Direction.values()) {
            for (boolean rotate90 : new boolean[]{false, true}) {
                for (BlockPos pos : positions) {
                    for (String text : texts) {
                        compare(new Scenario(new String[]{text, "second|small", null}, 3, facing, rotate90, pos, 0));
                    }
                }
            }
        }
        // Empty/default rows, a zero-row entity, and renderer/entity row-count mismatch.
        compare(new Scenario(null, 0, Direction.NORTH, false, positions[0], 0));
        compare(new Scenario(new String[]{"only"}, 4, Direction.WEST, true, positions[1], 0));
        Random random = new Random(20260930L);
        String[] alphabet = {"a", "|", "|", " ", "한", "日", "🚉", "\n", "\\"};
        for (int sample = 0; sample < 500; sample++) {
            StringBuilder text = new StringBuilder();
            int length = random.nextInt(45);
            for (int i = 0; i < length; i++) text.append(alphabet[random.nextInt(alphabet.length)]);
            compare(new Scenario(new String[]{text.toString(), "secondary", ""}, 3,
                    Direction.values()[random.nextInt(4)], random.nextBoolean(), positions[sample % 3], 0));
        }
    }

    private static void scheduledReadTiming() throws Exception {
        // Update after enqueue, on the first back-face row read, and on an intermediate row read.
        for (int mode : new int[]{1, 2, 3}) {
            compare(new Scenario(new String[]{"old|small", "row two", "row three"}, 3,
                    Direction.EAST, true, new BlockPos(4, 5, 6), mode));
        }
        Entity entity = entity(new String[]{"old"}, new BlockPos(1, 2, 3));
        reset();
        renderer(true, 1, false).render(entity, 0, null, 4, 5);
        check(entity.reads == 0, "No message reads before the scheduled render callback");
        check(cache(entity)[0] == null, "No eager split while enqueueing");
        entity.setMessages(new String[]{"|"});
        check(cache(entity)[0] == null, "Editing delimiter-only text does not split or throw early");
        expect(ArrayIndexOutOfBoundsException.class, () -> MainRenderer.flush(OFFSET));
        check(entity.reads == 1, "Delimiter-only exception occurs at the original first row read");
        check(Trace.EVENTS.stream().noneMatch(e -> e.equals("push")), "No graphics calls before the original delimiter-only exception");
        MainRenderer.QUEUE.clear();
        reset();
        renderer(true, 1, false).render(entity, 0, null, 4, 5);
        MessageParts empty = cache(entity)[0];
        expect(ArrayIndexOutOfBoundsException.class, () -> MainRenderer.flush(OFFSET));
        check(cache(entity)[0] == empty, "A cached empty split still throws at render time");
        MainRenderer.QUEUE.clear();
        entity.setMessages(new String[]{"valid"});
        entity.getMessageParts(0);
        MessageParts valid = cache(entity)[0];
        CompoundTag delimiterTag = new CompoundTag();
        delimiterTag.putString("msd_custom_message0", "|||");
        reset();
        entity.readCompoundTag(delimiterTag);
        check(cache(entity)[0] == valid, "Delimiter-only NBT text does not split or throw while loading");
        renderer(true, 1, false).render(entity, 0, null, 0, 0);
        expect(ArrayIndexOutOfBoundsException.class, () -> MainRenderer.flush(OFFSET));
        check(cache(entity)[0].size() == 0, "NBT delimiter-only split preserves the original empty array");
        MainRenderer.QUEUE.clear();
    }

    private static void cacheIdentityAndOwnership() throws Exception {
        Entity first = entity(new String[]{"A||B||", "A||B||", null}, new BlockPos(0, 0, 0));
        MessageParts row0 = first.getMessageParts(0);
        MessageParts row1 = first.getMessageParts(1);
        check(row0 != row1, "Cache entries are per row");
        check(row0 == first.getMessageParts(0), "Stable row reuses its split result");
        first.setMessages(new String[]{new String("A||B||")});
        check(row0 == first.getMessageParts(0), "Equal replacement String reuses the split");
        check(row0.size() == 3 && row0.get(0).equals("A") && row0.get(1).equals("") && row0.get(2).equals("B"),
                "All original split fields, including interior empty fields, are retained");
        first.setMessages(new String[]{"changed|tail"});
        MessageParts changed = first.getMessageParts(0);
        check(changed != row0 && changed.get(0).equals("changed"), "Edit replaces the matching row entry");
        check(row1 == first.getMessageParts(1), "Partial edit retains untouched rows");
        first.setMessages(new String[]{"A||B||"});
        check(first.getMessageParts(0) != row0, "The cache keeps one current value, not a history map");
        Entity second = entity(new String[]{"A||B||"}, new BlockPos(0, 0, 0));
        check(first.getMessageParts(0) != second.getMessageParts(0), "New entity at the same position owns a new cache");
        check(first.getMessageParts(2).size() == 1 && first.getMessageParts(2).get(0).equals(""), "Null/default row remains empty text");
        for (int index : new int[]{-1, 3, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            check(first.getMessageParts(index).size() == 1 && first.getMessageParts(index).get(0).equals(""), "Invalid row keeps getMessage's empty fallback");
        }
        for (String message : new String[]{"", "|", "||", "a|", "|a", "a||b|||", "서울|🚆"}) {
            first.setMessages(new String[]{message});
            String[] expected = message.split("\\|");
            MessageParts actual = first.getMessageParts(0);
            check(actual.size() == expected.length, "Exact String.split length: " + message);
            for (int i = 0; i < expected.length; i++) check(expected[i].equals(actual.get(i)), "Exact String.split element");
        }
        for (Field field : MessageParts.class.getDeclaredFields()) {
            check(Modifier.isPrivate(field.getModifiers()) && Modifier.isFinal(field.getModifiers()), "Split state is private and final");
            check(!field.getName().startsWith("this$"), "Immutable parts do not retain the entity");
        }
        check(Arrays.stream(MessageParts.class.getDeclaredMethods()).noneMatch(m -> m.getReturnType().isArray()), "No method exposes the split array");
        Field cache = BlockCustomTextEntity.class.getDeclaredField("messageParts");
        check(Modifier.isPrivate(cache.getModifiers()) && Modifier.isFinal(cache.getModifiers()) && !Modifier.isStatic(cache.getModifiers()),
                "Cache ownership stays on each entity");
    }

    private static void nbtAndEditInvalidation() throws Exception {
        Entity entity = entity(new String[]{"one|small", "two", null}, new BlockPos(0, 0, 0));
        reset();
        renderFrame(true, entity, 3, false);
        MessageParts old = cache(entity)[0];
        check(old != null, "Production scheduled render populates the entity cache");
        MessageParts[] beforeHit = cache(entity).clone();
        reset();
        renderFrame(true, entity, 3, false);
        check(Arrays.equals(beforeHit, cache(entity)), "All front and back render cache hits reuse existing objects");
        check(count("message:") == 6, "Every row is still read on both faces on a cache hit");
        check(count("width:") == 8 && count("draw:") == 8, "Widths and draws are freshly evaluated on cache hits");
        CompoundTag tag = new CompoundTag();
        tag.putString("msd_custom_message0", "NBT|new");
        tag.putString("msd_custom_message1", "second||last||");
        reset();
        entity.readCompoundTag(tag);
        check(cache(entity)[0] == old, "NBT updates remain split-free until rendering");
        check(Trace.EVENTS.equals(Arrays.asList("nbt.read:msd_custom_message0", "nbt.read:msd_custom_message1", "nbt.read:msd_custom_message2")),
                "NBT read order remains unchanged");
        renderFrame(true, entity, 3, false);
        check(cache(entity)[0] != old && cache(entity)[0].get(0).equals("NBT"), "Render notices the NBT replacement");
        check(entity.getMessage(2).equals(""), "Missing NBT row retains the original empty-string behavior");
        CompoundTag written = new CompoundTag();
        entity.writeCompoundTag(written);
        Entity restored = entity(new String[]{null, null, null}, new BlockPos(0, 0, 0));
        restored.readCompoundTag(written);
        for (int i = 0; i < 3; i++) check(entity.getMessage(i).equals(restored.getMessage(i)), "NBT serialization round trip");
        check(Arrays.stream(cache(restored)).allMatch(p -> p == null), "Newly loaded entity starts without render cache state");
        reset();
        entity.setMessages(new String[]{"edited"});
        check(Trace.EVENTS.equals(Arrays.asList("entity.dirty")), "Edit still marks dirty exactly once and does no eager reads");
        entity.setMessages(new String[]{null});
        check(entity.getMessageParts(0).get(0).equals(""), "A null edit returns to the default row");
        reset();
        renderFrame(true, entity, 3, false);
        List<String> trace = new ArrayList<>(Trace.EVENTS);
        reset();
        renderFrame(false, restoredWithMessages(entity), 3, false);
        check(trace.equals(Trace.EVENTS), "Final edited state has the exact baseline graphics/read trace");
    }

    private static void worldAndEntityLifecycles() throws Exception {
        Entity entity = entity(new String[]{"same|text"}, new BlockPos(1, 2, 3));
        reset();
        renderFrame(true, entity, 1, false);
        MessageParts original = cache(entity)[0];
        entity.world = null;
        reset();
        renderer(true, 1, false).render(entity, 0, null, 0, 0);
        check(Trace.EVENTS.equals(Arrays.asList("entity.world")), "Detached entity still returns before scheduling or reading text");
        check(MainRenderer.QUEUE.isEmpty(), "No queued callback for a missing world");
        entity.world = new World();
        entity.world.facing = Direction.SOUTH;
        reset();
        GraphicsHolder.widthSalt = 1000;
        renderFrame(true, entity, 1, false);
        List<String> candidate = new ArrayList<>(Trace.EVENTS);
        check(cache(entity)[0] == original, "World/font changes can reuse only immutable text splits");
        Entity baseline = entity(new String[]{"same|text"}, new BlockPos(1, 2, 3));
        baseline.world.facing = Direction.SOUTH;
        reset();
        GraphicsHolder.widthSalt = 1000;
        renderFrame(false, baseline, 1, false);
        check(candidate.equals(Trace.EVENTS), "Font-width changes and replacement worlds are freshly observed");
        Entity replacement = entity(new String[]{"same|text"}, new BlockPos(1, 2, 3));
        check(cache(replacement)[0] == null, "Replacement entity starts with no retained cache");
    }

    private static void lightQueryEquivalence() {
        Random random = new Random(1944);
        for (int count : new int[]{0, 1, 2, 31, 1000}) {
            BlockPos[] positions = new BlockPos[count];
            for (int i = 0; i < count; i++) positions[i] = new BlockPos(random.nextInt(), random.nextInt(), random.nextInt());
            for (int generation : new int[]{0, 1, 15, 100}) {
                ClientWorld baseline = new ClientWorld();
                baseline.generation = generation;
                reset();
                LightType.helperCalls = 0;
                int[] expected = BaselineLights.getLights(baseline, positions);
                List<String> trace = new ArrayList<>(Trace.EVENTS);
                check(LightType.helperCalls == count * 2, "Baseline calls both enum helpers per position");
                ClientWorld candidate = new ClientWorld();
                candidate.generation = generation;
                reset();
                LightType.helperCalls = 0;
                int[] actual = CandidateLights.getLights(candidate, positions);
                check(Arrays.equals(expected, actual), "Packed light values are identical");
                check(trace.equals(Trace.EVENTS), "Every block/sky read and pack call stays in original order");
                check(candidate.reads == 2 * count, "No fresh world-light query is skipped");
                check(LightType.helperCalls == 0, "Candidate uses singleton constants directly");
                lightComparisons++;
                candidate.generation++;
                int before = candidate.reads;
                CandidateLights.getLights(candidate, positions);
                check(candidate.reads == before + 2 * count, "Subsequent calls do not cache lighting values");
            }
        }
        BlockPos[] positions = {new BlockPos(1, 2, 3), new BlockPos(1, 2, 3)};
        for (int failure = 0; failure < 4; failure++) {
            ClientWorld baseline = new ClientWorld();
            baseline.failAt = failure;
            reset();
            expect(IllegalStateException.class, () -> BaselineLights.getLights(baseline, positions));
            List<String> trace = new ArrayList<>(Trace.EVENTS);
            ClientWorld candidate = new ClientWorld();
            candidate.failAt = failure;
            reset();
            expect(IllegalStateException.class, () -> CandidateLights.getLights(candidate, positions));
            check(trace.equals(Trace.EVENTS), "World query failures occur after the same read prefix");
            lightComparisons++;
        }
    }

    private static void compare(Scenario scenario) throws Exception {
        List<String> baseline = runScenario(false, scenario);
        List<String> candidate = runScenario(true, scenario);
        if (!baseline.equals(candidate)) {
            int index = 0;
            while (index < Math.min(baseline.size(), candidate.size()) && baseline.get(index).equals(candidate.get(index))) index++;
            throw new AssertionError("Render trace mismatch at " + index + " for " + Arrays.toString(scenario.messages)
                    + "\nbaseline=" + baseline + "\ncandidate=" + candidate);
        }
        assertions++;
        renderComparisons++;
    }

    private static List<String> runScenario(boolean optimized, Scenario scenario) throws Exception {
        Entity entity = entity(scenario.messages, scenario.pos);
        entity.world.facing = scenario.facing;
        reset();
        BlockEntityRenderer<Entity> renderer = renderer(optimized, scenario.rows, scenario.rotate90);
        for (int frame = 0; frame < 3; frame++) {
            entity.reads = 0;
            final int currentFrame = frame;
            entity.beforeRead = ordinal -> {
                if ((scenario.updateMode == 2 && ordinal == scenario.rows) || (scenario.updateMode == 3 && ordinal == 1)) {
                    entity.setMessages(new String[]{"frame " + currentFrame + "|new", "changed row", "tail"});
                }
            };
            GraphicsHolder.widthSalt = frame * 100;
            renderer.render(entity, 0.25F, null, 0xABC, 5);
            check(entity.reads == 0, "Reads remain deferred until callback execution");
            if (scenario.updateMode == 1) entity.setMessages(new String[]{"enqueued edit " + frame + "|new", "", null});
            try {
                MainRenderer.flush(OFFSET);
            } catch (RuntimeException exception) {
                Trace.add("exception:" + exception.getClass().getName() + ":" + exception.getMessage());
                MainRenderer.QUEUE.clear();
            }
        }
        return new ArrayList<>(Trace.EVENTS);
    }

    private static BlockEntityRenderer<Entity> renderer(boolean optimized, int rows, boolean rotate90) {
        BlockEntityRenderer.Argument argument = new BlockEntityRenderer.Argument();
        // Non-power-of-two values exercise the original floating-point evaluation order.
        return optimized
                ? new RenderCustomText<>(argument, rows, 1.35F, 12.65F, 7.9F, 11.7F, 9.25F, rotate90, 0.73F, 0.61F, 0.39F, 0.017F, 0x123456, 0x80FF00)
                : new BaselineRenderCustomText<>(argument, rows, 1.35F, 12.65F, 7.9F, 11.7F, 9.25F, rotate90, 0.73F, 0.61F, 0.39F, 0.017F, 0x123456, 0x80FF00);
    }

    private static Entity entity(String[] messages, BlockPos pos) {
        Entity entity = new Entity(messages == null ? 0 : messages.length, pos);
        entity.world = new World();
        if (messages != null) entity.setMessages(messages);
        return entity;
    }

    private static Entity restoredWithMessages(Entity original) {
        String[] messages = new String[original.rows];
        // Do not add message reads to the trace under comparison.
        for (int i = 0; i < messages.length; i++) messages[i] = original.untracedMessage(i);
        Entity result = entity(messages, new BlockPos(0, 0, 0));
        Trace.EVENTS.clear();
        return result;
    }

    private static void renderFrame(boolean optimized, Entity entity, int rows, boolean rotate90) {
        renderer(optimized, rows, rotate90).render(entity, 0, null, 0, 0);
        MainRenderer.flush(OFFSET);
    }

    private static MessageParts[] cache(Entity entity) throws Exception {
        Field field = BlockCustomTextEntity.class.getDeclaredField("messageParts");
        field.setAccessible(true);
        return (MessageParts[]) field.get(entity);
    }

    private static long count(String prefix) { return Trace.EVENTS.stream().filter(e -> e.startsWith(prefix)).count(); }

    private static void reset() {
        Trace.EVENTS.clear();
        MainRenderer.QUEUE.clear();
        GraphicsHolder.widthSalt = 0;
        GraphicsHolder.afterDraw = null;
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }

    private static void expect(Class<? extends Throwable> expected, Runnable action) {
        try { action.run(); } catch (Throwable actual) {
            check(actual.getClass() == expected, "Expected " + expected + " but got " + actual);
            return;
        }
        throw new AssertionError("Expected " + expected.getName());
    }

    private static final class Entity extends BlockCustomTextEntity {
        final int rows;
        int reads;
        IntConsumer beforeRead = ordinal -> {};
        Entity(int rows, BlockPos pos) { super(new BlockEntityType<>(), pos, new BlockState(), rows); this.rows = rows; }
        @Override public String getMessage(int index) {
            beforeRead.accept(reads++);
            Trace.add("message:" + index);
            return super.getMessage(index);
        }
        String untracedMessage(int index) { return super.getMessage(index); }
    }

    private static final class Scenario {
        final String[] messages;
        final int rows;
        final Direction facing;
        final boolean rotate90;
        final BlockPos pos;
        final int updateMode;
        Scenario(String[] messages, int rows, Direction facing, boolean rotate90, BlockPos pos, int updateMode) {
            this.messages = messages; this.rows = rows; this.facing = facing; this.rotate90 = rotate90; this.pos = pos; this.updateMode = updateMode;
        }
    }
}
