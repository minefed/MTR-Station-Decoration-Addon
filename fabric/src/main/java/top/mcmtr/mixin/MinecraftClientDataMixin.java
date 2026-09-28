package top.mcmtr.mixin;

import org.mtr.mod.client.MinecraftClientData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.mcmtr.mod.InitClient;

@Mixin(MinecraftClientData.class)
public abstract class MinecraftClientDataMixin {
    @Inject(method = "sync", at = @At(value = "RETURN"), remap = false)
    private void clearStationCache(CallbackInfo ci) {
        InitClient.clearStationCache();
    }
}
