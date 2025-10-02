package net.ramixin.dunchanting.client;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.ramixin.dunchanting.Dunchanting;

import static net.minecraft.client.render.RenderPhase.*;

public class ModRenderLayers {

    public static final Identifier GILDED_ITEM_GLINT_TEXTURE = Dunchanting.id("textures/misc/generated/gilded_enchanted_glint_item.png");
    public static final Identifier GILDED_ARMOR_ENTITY_GLINT_TEXTURE = Dunchanting.id("textures/misc/generated/gilded_enchanted_glint_armor.png");


    public static final RenderLayer GILDED_ARMOR_ENTITY_GLINT = RenderLayer.of(
            "gilded_armor_entity_glint",
            1536,
            RenderPipelines.GLINT,
            RenderLayer.MultiPhaseParameters.builder()
                    .texture(new RenderPhase.Texture(GILDED_ARMOR_ENTITY_GLINT_TEXTURE, false))
                    .texturing(ARMOR_ENTITY_GLINT_TEXTURING)
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .build(false)
    );
    public static final RenderLayer GILDED_GLINT_TRANSLUCENT = RenderLayer.of(
            "gilded_glint_translucent",
            1536,
            RenderPipelines.GLINT,
            RenderLayer.MultiPhaseParameters.builder()
                    .texture(new RenderPhase.Texture(GILDED_ITEM_GLINT_TEXTURE, false))
                    .texturing(GLINT_TEXTURING)
                    .target(ITEM_ENTITY_TARGET)
                    .build(false)
    );
    public static final RenderLayer GILDED_GLINT = RenderLayer.of(
            "gilded_glint",
            1536,
            RenderPipelines.GLINT,
            RenderLayer.MultiPhaseParameters.builder()
                    .texture(new RenderPhase.Texture(GILDED_ITEM_GLINT_TEXTURE, false))
                    .texturing(GLINT_TEXTURING)
                    .build(false)
    );
    public static final RenderLayer GILDED_ENTITY_GLINT = RenderLayer.of(
            "gilded_entity_glint",
            1536,
            RenderPipelines.GLINT,
            RenderLayer.MultiPhaseParameters.builder()
                    .texture(new RenderPhase.Texture(GILDED_ITEM_GLINT_TEXTURE, false))
                    .texturing(ENTITY_GLINT_TEXTURING)
                    .build(false)
    );

}
