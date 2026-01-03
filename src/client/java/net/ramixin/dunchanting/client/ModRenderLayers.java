package net.ramixin.dunchanting.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.client.mixins.RenderTypeAccessor;

public class ModRenderLayers {

    public static final Identifier GILDED_ITEM_GLINT_TEXTURE = Dunchanting.id("textures/misc/gilded_enchanted_glint_item.png");
    public static final Identifier GILDED_ARMOR_ENTITY_GLINT_TEXTURE = Dunchanting.id("textures/misc/gilded_enchanted_glint_armor.png");

    public static final RenderType GILDED_ARMOR_ENTITY_GLINT = create(
            "armor_entity_glint",
            RenderSetup.builder(RenderPipelines.GLINT)
                    .withTexture("Sampler0", GILDED_ARMOR_ENTITY_GLINT_TEXTURE)
                    .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );
    public static final RenderType GILDED_GLINT_TRANSLUCENT = create(
            "glint_translucent",
            RenderSetup.builder(RenderPipelines.GLINT)
                    .withTexture("Sampler0", GILDED_ITEM_GLINT_TEXTURE)
                    .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );
    public static final RenderType GILDED_GLINT = create(
            "glint",
            RenderSetup.builder(RenderPipelines.GLINT)
                    .withTexture("Sampler0", GILDED_ITEM_GLINT_TEXTURE)
                    .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                    .createRenderSetup()
    );
    public static final RenderType GILDED_ENTITY_GLINT = create(
            "entity_glint",
            RenderSetup.builder(RenderPipelines.GLINT)
                    .withTexture("Sampler0", GILDED_ITEM_GLINT_TEXTURE)
                    .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                    .createRenderSetup()
    );

    private static RenderType create(String name, RenderSetup renderSetup) {
        return RenderTypeAccessor.callCreate(name, renderSetup);
    }

}
