package useless.statmaster.mixins;

import net.minecraft.core.world.save.SaveHandlerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SaveHandlerBase.class, remap = false)
public interface SaveHandlerBaseAccessor {
	@Accessor
	String getWorldDirName();
}
