package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {

    @Invoker
    static RenderType callCreate(String string, RenderSetup renderSetup) {
        throw new AssertionError();
    }
}
