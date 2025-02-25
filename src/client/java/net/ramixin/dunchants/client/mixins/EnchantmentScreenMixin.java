package net.ramixin.dunchants.client.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.DungeonEnchants;
import net.ramixin.dunchants.DungeonEnchantsUtils;
import net.ramixin.dunchants.ModItemComponents;
import net.ramixin.dunchants.client.DungeonEnchantsClient;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Debug(export = true)
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {

    @Shadow private ItemStack stack;

    @Unique private final int[] animationProgresses = new int[]{0,0,0,0,0,0,0,0,0};

    @Unique private final boolean[] isHovering = new boolean[9];

    @Unique
    private static final Identifier[] selectionIcons = new Identifier[]{
            DungeonEnchants.id("container/enchanting_table/selection_small"),
            DungeonEnchants.id("container/enchanting_table/selection_animation2"),
            DungeonEnchants.id("container/enchanting_table/selection_animation3"),
            DungeonEnchants.id("container/enchanting_table/selection_animation4"),
            DungeonEnchants.id("container/enchanting_table/selection_animation5"),
            DungeonEnchants.id("container/enchanting_table/selection_animation6"),
            DungeonEnchants.id("container/enchanting_table/selection_animation7"),
            DungeonEnchants.id("container/enchanting_table/selection_animation8"),
            DungeonEnchants.id("container/enchanting_table/selection_animation9"),
            DungeonEnchants.id("container/enchanting_table/selection_animation10"),
            DungeonEnchants.id("container/enchanting_table/selection_animation11"),
            DungeonEnchants.id("container/enchanting_table/selection_animation12"),
            DungeonEnchants.id("container/enchanting_table/selection_animation13"),
            DungeonEnchants.id("container/enchanting_table/selection_animation14"),
            DungeonEnchants.id("container/enchanting_table/selection_large")
    };

    @Unique
    private static final Identifier[] roman_numerals = new Identifier[]{
            DungeonEnchants.id("container/enchanting_table/one_roman_numeral"),
            DungeonEnchants.id("container/enchanting_table/two_roman_numeral"),
            DungeonEnchants.id("container/enchanting_table/three_roman_numeral")
    };

    @Unique
    private static final Identifier locked_enchantment = DungeonEnchants.id("container/enchanting_table/locked_small");

    @Unique
    private static final Identifier locked_enchantment_big = DungeonEnchants.id("container/enchanting_table/locked");

    @Unique
    private List<List<String>> cachedOptions = null;

    @Unique
    private static final SpriteIdentifier missingno_icon = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.ofVanilla("missingno"));


    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeBackgroundHeight(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.backgroundHeight = 173;
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void preventRendering(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
        if(this.client == null || this.client.world == null) return;
        int relX = (this.width - this.backgroundWidth) / 2;
        int relY = (this.height - this.backgroundHeight) / 2;
        if(stack.get(ModItemComponents.ENCHANTMENT_OPTIONS) != cachedOptions)
            if(DungeonEnchantsUtils.containsImproperComponent(stack, MinecraftClient.getInstance().world)) return;
        cachedOptions = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        List<List<String>> enchantmentOptions = this.stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(enchantmentOptions == null) return;
        for(int i = 0; i < 3; i++) {
            List<String> enchants = enchantmentOptions.get(i);
            if(DungeonEnchantsUtils.isOptionEnchanted(stack, this.client.world, i, cachedOptions)) {
                context.drawGuiTexture(selectionIcons[14], relX - 1 + 57 * i, relY + 19, 64, 64);
                String enchant = enchants.getFirst();
                Optional<RegistryEntry.Reference<Enchantment>> enchantment = this.client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(enchant));
                if(enchantment.isEmpty()) continue;
                SpriteIdentifier spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.of(enchant).withPrefixedPath("large/"));
                if(spriteId.getSprite() == missingno_icon.getSprite()) spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.ofVanilla("large/unknown"));
                context.drawSprite(relX - 1 + 57 * i, relY + 19, 0, 64, 64, spriteId.getSprite());
                if(!isHovering[3 * i]) continue;
                context.drawTooltip(this.textRenderer, List.of(enchantment.orElseThrow().value().description()), mouseX, mouseY);
            }
            else if(enchants.getFirst().equals("locked")) {
                context.drawGuiTexture(locked_enchantment_big, relX - 1 + 57 * i, relY + 19, 64, 64);
            } else {
                context.drawGuiTexture(roman_numerals[i], relX + 23 + 57 * i, relY + 29, 16, 16);
                for(int l = 0; l < 3; l++) {
                    String enchant = enchants.get(l);
                    int x = (int) (relX + (-21 * Math.pow(l, 2) + 49 * l - 15)) + 57 * i;
                    int y = (l == 2 ? 34 : 19) + relY;
                    context.drawGuiTexture(enchant.equals("locked") ? locked_enchantment : selectionIcons[animationProgresses[3 * i + l] / 2], x, y, 64, 64);
                    if(enchant.equals("locked")) continue;
                    Optional<RegistryEntry.Reference<Enchantment>> enchantment = this.client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(enchant));
                    SpriteIdentifier spriteId = getEnchantmentIcon(enchant, 0);
                    context.drawSprite(x, y, 0, 64, 64, spriteId.getSprite());
                    if(isHovering[3 * i + l]) context.drawTooltip(this.textRenderer, List.of(enchantment.orElseThrow().value().description()), mouseX, mouseY);
                }
            }
        }
        for(int i = 0; i < 9; i++) if(animationProgresses[i] > 0) {
            if(enchantmentOptions.get(i/3).getFirst().equals("locked")) continue;
            if(enchantmentOptions.get(i/3).size() == 1) continue;

            String enchant = enchantmentOptions.get(i / 3).get(i % 3);
            if(enchant.equals("locked")) continue;
            int x = (int) (relX + (-21 * Math.pow(i % 3, 2) + 49 * (i % 3) - 15)) + 57 * (i / 3);
            int y = (i % 3 == 2 ? 34 : 19) + relY;
            context.drawGuiTexture(selectionIcons[animationProgresses[i] / 2], x, y, 64, 64);
            SpriteIdentifier spriteId = getEnchantmentIcon(enchant, animationProgresses[i] / 2);
            context.drawSprite(x, y, 0, 64, 64, spriteId.getSprite());
        }
    }

    @Override
    public void mouseMoved(double x, double y) {
        if(cachedOptions == null) return;
        int relX = (this.width - this.backgroundWidth) / 2;
        int relY = (this.height - this.backgroundHeight) / 2;
        for(int i = 0; i < 3; i++) {
            if(cachedOptions.get(i).size() == 1) {
                int slotX = relX - 1 + 57 * i;
                int slotY = relY + 19;
                isHovering[3 * i] = Math.abs(x - slotX - 32) + Math.abs(y - slotY - 32) <= 24;
                isHovering[3 * i + 1] = false;
                isHovering[3 * i + 2] = false;
                continue;
            }
            for (int l = 0; l < 3; l++) {
                int slotX = (int) (relX + (-21 * Math.pow(l, 2) + 49 * l - 15)) + 57 * i;
                int slotY = (l == 2 ? 34 : 19) + relY;
                isHovering[3 * i + l] = Math.abs(x - slotX - 32) + Math.abs(y - slotY - 32) <= 12;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void updateAnimationsOnRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        for(int i = 0; i < 9; i++) {
            if(animationProgresses[i] > 0 && !isHovering[i]) animationProgresses[i] = Math.max(0, animationProgresses[i] - (int) delta - 1);
            else if(animationProgresses[i] < 24 && isHovering[i]) animationProgresses[i] = Math.min(24, animationProgresses[i] + (int) delta + 1);
        }
    }

    @Unique
    private static Identifier getEnchantmentIconId(String id, int index) {
        StringBuilder builder = new StringBuilder();
        if(index == 0) builder.append("small/");
        else builder.append("generated/");
        builder.append(id);
        if(index != 0) builder.append("/").append(index);
        return Identifier.of(builder.toString());
    }

    @Unique
    private SpriteIdentifier getEnchantmentIcon(String id, int index) {
        if(this.client == null) throw new IllegalStateException("Client is null though in use");
        if(this.client.world == null) throw new IllegalStateException("Client world is null though in use");
        Identifier enchantId = Identifier.of(id);
        Optional<RegistryEntry.Reference<Enchantment>> enchantment = this.client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(enchantId);
        if(enchantment.isEmpty()) return new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("unknown", index));
        SpriteIdentifier spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId(enchantId.getPath(), index));
        if(spriteId.getSprite() == missingno_icon.getSprite()) return new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("unknown", index));
        return spriteId;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void applyEnchantmentOnClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.mouseClicked(mouseX, mouseY, button));
        if(this.client == null || this.client.interactionManager == null) return;
        if(DungeonEnchantsUtils.containsImproperComponent(stack, this.client.world)) return;
        for(int i = 0; i < 9; i++) if(isHovering[i]) {
            this.handler.onButtonClick(this.client.player, i);
            this.client.interactionManager.clickButton(this.handler.syncId, i);
            return;
        }
    }
}
