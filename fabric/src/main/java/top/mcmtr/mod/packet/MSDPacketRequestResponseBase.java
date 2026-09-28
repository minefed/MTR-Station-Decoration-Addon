package top.mcmtr.mod.packet;

import org.mtr.core.serializer.JsonReader;
import org.mtr.core.serializer.SerializedDataBase;
import org.mtr.core.tool.Utilities;
import org.mtr.libraries.com.google.gson.JsonObject;
import org.mtr.mapping.holder.MinecraftServer;
import org.mtr.mapping.holder.ServerPlayerEntity;
import org.mtr.mapping.holder.ServerWorld;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.MinecraftServerHelper;
import org.mtr.mapping.registry.PacketHandler;
import org.mtr.mapping.tool.PacketBufferReceiver;
import org.mtr.mapping.tool.PacketBufferSender;
import top.mcmtr.mod.Init;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MSDPacketRequestResponseBase extends PacketHandler {
    private final String content;

    public MSDPacketRequestResponseBase(PacketBufferReceiver packetBufferReceiver) {
        this.content = packetBufferReceiver.readString();
    }

    public MSDPacketRequestResponseBase(String content) {
        this.content = content;
    }

    @Override
    public void write(PacketBufferSender packetBufferSender) {
        packetBufferSender.writeString(content);
    }

    @Override
    public void runServer(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity) {
        runServerOutbound(serverPlayerEntity.getServerWorld(), serverPlayerEntity);
    }

    @Override
    public final void runClient() {
        runClientInbound(new JsonReader(Utilities.parseJson(content)));
    }

    public final void runServerOutbound(ServerWorld serverWorld, @Nullable ServerPlayerEntity serverPlayerEntity) {
        Init.sendMessageC2S(getKey(), serverWorld.getServer(), new World(serverWorld.data), getDataInstance(new JsonReader(Utilities.parseJson(content))), responseType() == MSDPacketRequestResponseBase.ResponseType.NONE ? null : responseData -> {
            final JsonObject responseJson = Utilities.getJsonObjectFromData(responseData);
            if (responseType() == MSDPacketRequestResponseBase.ResponseType.PLAYER) {
                if (serverPlayerEntity != null) {
                    Init.REGISTRY.sendPacketToClient(serverPlayerEntity, getInstance(responseJson.toString()));
                }
            } else {
                final String responseString = responseJson.toString();
                MinecraftServerHelper.iteratePlayers(serverWorld, serverPlayerEntityNew -> Init.REGISTRY.sendPacketToClient(serverPlayerEntityNew, getInstance(responseString)));
            }
            runServerInbound(serverWorld, responseJson);
        }, SerializedDataBase.class);
    }

    protected abstract void runServerInbound(ServerWorld serverWorld, JsonObject jsonObject);

    protected abstract void runClientInbound(JsonReader jsonReader);

    protected abstract MSDPacketRequestResponseBase getInstance(String content);

    protected abstract SerializedDataBase getDataInstance(JsonReader jsonReader);

    @Nonnull
    protected abstract String getKey();

    protected abstract ResponseType responseType();

    protected enum ResponseType {
        NONE,
        PLAYER,
        ALL;

        ResponseType() {
        }
    }
}