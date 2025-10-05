package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.gui.DrawContext;
import net.ramixin.dunchanting.client.util.DrawContentDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(DrawContext.class)
public class DrawContentMixin implements DrawContentDuck {

    @Shadow private @Nullable Runnable tooltipDrawer;
    @Unique
    private final List<Consumer<DrawContext>> tooltipBatches = new ArrayList<>();

    @Override
    public void dunchanting$enableTooltipBatching() {
        Runnable original = this.tooltipDrawer;
        this.tooltipDrawer = () -> {
            if (original != null) original.run();
            for (Consumer<DrawContext> runnable : this.tooltipBatches)
                runnable.accept((DrawContext)(Object)this);
        };
    }

    @Override
    public void dunchanting$addTooltipToBatch(Consumer<DrawContext> runnable) {
        tooltipBatches.add(runnable);
    }
}
