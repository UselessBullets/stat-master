package useless.statmaster.mixins;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiMainMenu.class, remap = false)
public class GuiMainMenuMixin extends GuiScreen {
	@Inject(method = "init()V", at = @At("TAIL"))
	private void addButton(CallbackInfo ci){
		int i = this.height / 4 + 48;
		controlList.add(new GuiButton(689, this.width / 2 - 100 - 23, i + 72 + 12, 20, 20, "S"));
	}
	@Inject(method = "buttonPressed(Lnet/minecraft/client/gui/GuiButton;)V", at = @At("HEAD"))
	private void pressButton(GuiButton button, CallbackInfo ci){
		if (button.id == 689){
			mc.displayGuiScreen(new GuiStats(this, mc.statsCounter));
		}
	}
}
