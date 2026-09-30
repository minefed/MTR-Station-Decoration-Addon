package top.mcmtr.mod;
import org.mtr.mapping.holder.ServerPlayerEntity;
public final class Init {
    public static final Registry REGISTRY = new Registry();
    public static final class Registry { public void sendPacketToClient(ServerPlayerEntity player, Object packet) {} }
}
