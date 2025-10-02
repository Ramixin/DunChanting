package net.ramixin.dunchanting.client.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.texture.AtlasManager;
import net.ramixin.dunchanting.client.DunchantingClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(AtlasManager.class)
public class AtlasManagerMixin {

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"))
    private static List<Object> appendEnchantmentIconsAtlas(List<Object> original) {
        List<Object> mutable = new ArrayList<>(original);
        mutable.add(new AtlasManager.Metadata(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, DunchantingClient.ENCHANTMENT_ICONS_ATLAS_ID, false));
        return mutable;
    }

}
