package useless.statmaster.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.NetClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import useless.statmaster.IHostName;

@Mixin(value = NetClientHandler.class, remap = false)
public class NetClientHandlerMixin implements IHostName {
	@Unique public String hostName;
	@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V", at = @At("TAIL"))
	private void retrieveHostName(Minecraft minecraft, String host, int port, CallbackInfo ci){
		hostName = host;
	}

	@Override
	public String stat_master$getHostName() {
		return hostName;
	}
}
