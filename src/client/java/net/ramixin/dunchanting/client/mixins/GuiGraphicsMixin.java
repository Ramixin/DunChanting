package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.ramixin.dunchanting.client.util.DrawContentDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements DrawContentDuck {

    @Shadow private @Nullable Runnable deferredTooltip;
    @Unique
    private final List<Consumer<GuiGraphics>> tooltipBatches = new ArrayList<>();

    @Override
    public void dunchanting$enableTooltipBatching() {
        Runnable original = this.deferredTooltip;
        this.deferredTooltip = () -> {
            if (original != null) original.run();
            for (Consumer<GuiGraphics> runnable : this.tooltipBatches)
                runnable.accept((GuiGraphics)(Object)this);
        };
    }

    @Override
    public void dunchanting$addTooltipToBatch(Consumer<GuiGraphics> runnable) {
        tooltipBatches.add(runnable);
    }
}
