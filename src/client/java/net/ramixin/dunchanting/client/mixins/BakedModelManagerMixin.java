package net.ramixin.dunchanting.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.model.BakedModelManager;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.client.DunchantingClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;"))
    private static Map<Object, Object> appendEnchantmentIconsAtlas(Object k1, Object v1, Object k2, Object v2, Object k3, Object v3, Object k4, Object v4, Object k5, Object v5, Object k6, Object v6, Object k7, Object v7, Object k8, Object v8, Object k9, Object v9, Operation<Map<Object, Object>> original) {
        Map<Object, Object> map = original.call(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        HashMap<Object, Object> hashedCopy = new HashMap<>(map);
        hashedCopy.put(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, DunchantingClient.ENCHANTMENT_ICONS_ATLAS_ID);
        Dunchanting.LOGGER.info("inserted atlas: {}", DunchantingClient.ENCHANTMENT_ICONS_ATLAS_ID);
        return Map.copyOf(hashedCopy);
    }

}
