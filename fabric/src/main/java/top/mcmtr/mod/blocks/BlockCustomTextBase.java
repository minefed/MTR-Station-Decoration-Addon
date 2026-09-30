package top.mcmtr.mod.blocks;

import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.BlockEntityExtension;
import org.mtr.mapping.mapper.BlockWithEntity;
import org.mtr.mapping.mapper.DirectionHelper;
import org.mtr.mod.block.IBlock;
import top.mcmtr.mod.Init;
import top.mcmtr.mod.config.Config;
import top.mcmtr.mod.packet.MSDPacketOpenCustomScreen;

public abstract class BlockCustomTextBase extends BlockChangeModelBase implements DirectionHelper, BlockWithEntity {
    private final int maxArrivals;

    public BlockCustomTextBase(BlockSettings blockSettings, int maxArrivals, int maxModelNum) {
        super(blockSettings, maxModelNum);
        this.maxArrivals = maxArrivals;
    }

    public int getMaxArrivals() {
        return maxArrivals;
    }

    @Override
    public ActionResult onUse2(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return IBlock.checkHoldingBrush(world, player, () -> {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null && blockEntity.data instanceof BlockCustomTextEntity) {
                Init.REGISTRY.sendPacketToClient(ServerPlayerEntity.cast(player), new MSDPacketOpenCustomScreen(pos, maxArrivals));
            }
        });
    }

    public abstract static class BlockCustomTextEntity extends BlockEntityExtension {
        private final int maxArrivals;
        private final String[] messages;
        private final MessageParts[] messageParts;
        private static final String KEY_MESSAGE = "msd_custom_message";

        public BlockCustomTextEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int maxArrivals) {
            super(type, blockPos, blockState);
            this.maxArrivals = maxArrivals;
            this.messages = new String[maxArrivals];
            this.messageParts = new MessageParts[maxArrivals];
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            for (int i = 0; i < maxArrivals; i++) {
                messages[i] = compoundTag.getString(KEY_MESSAGE + i);
            }
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            for (int i = 0; i < maxArrivals; i++) {
                compoundTag.putString(KEY_MESSAGE + i, messages[i] == null ? "" : messages[i]);
            }
        }

        public void setMessages(String[] messages) {
            System.arraycopy(messages, 0, this.messages, 0, Math.min(messages.length, maxArrivals));
            markDirty2();
        }

        public String getMessage(int index) {
            if (index >= 0 && index < maxArrivals) {
                if (messages[index] != null) {
                    return messages[index];
                }
            }
            return "";
        }

        /**
         * Called on the render thread at the point where a row is drawn. Keep the
         * message read here so edits between queued renders or sign faces remain visible.
         */
        public final MessageParts getMessageParts(int index) {
            final String message = getMessage(index);
            if (index < 0 || index >= maxArrivals) {
                return new MessageParts(message);
            }
            MessageParts parts = messageParts[index];
            if (parts == null || !parts.message.equals(message)) {
                parts = new MessageParts(message);
                messageParts[index] = parts;
            }
            return parts;
        }

        /** An immutable view of the exact String.split result; the array never escapes. */
        public static final class MessageParts {
            private final String message;
            private final String[] parts;

            private MessageParts(String message) {
                this.message = message;
                parts = message.split("\\|");
            }

            public int size() {
                return parts.length;
            }

            public String get(int index) {
                return parts[index];
            }
        }

        @Override
        public double getRenderDistance2() {
            return Config.getCustomTextSignMaxViewDistance();
        }
    }
}