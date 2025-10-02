package net.ramixin.dunchanting.client;

import net.minecraft.util.Identifier;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.mixson.debug.DebugMode;
import net.ramixin.mixson.inline.EventContext;
import net.ramixin.mixson.inline.Mixson;
import net.ramixin.mixson.inline.MixsonCodecs;
import net.ramixin.mixson.util.MixsonUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

import static net.ramixin.dunchanting.util.ModUtils.manhattanDistance;
import static net.ramixin.dunchanting.util.ModUtils.toBufferedImage;

@SuppressWarnings("unused")
public class ModMixsonClient {

    public static void onInitialize() {

        Mixson.setDebugMode(DebugMode.EXPORT);

        //registerGlintTransformer("textures/misc/enchanted_glint_item", "textures/misc/generated/gilded_enchanted_glint_item");
        registerGlintTransformer("textures/misc/enchanted_glint_armor", "textures/misc/generated/gilded_enchanted_glint_armor");

        Mixson.registerEvent(
                MixsonCodecs.PNG,
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().startsWith("textures/enchantment/large/"),
                "FindAllLargeEnchantmentIcons",
                context -> {

                    Identifier large_id = MixsonUtil.removeExtension(context.getResourceId());
                    Identifier small_id = Identifier.of(large_id.getNamespace(), large_id.getPath().replace("/large/", "/small/"));
                    context.registerRuntimeEvent(
                            Mixson.DEFAULT_PRIORITY,
                            id -> id.equals(small_id),
                            "generateTransitionTextures_"+small_id,
                            context2 -> generateInBetweenTextures(context2, context.getFile()),
                            true
                    );

                },
                false
        );

        Mixson.registerEvent(
                MixsonCodecs.PNG,
                Mixson.DEFAULT_PRIORITY + 100,
                id -> id.getPath().startsWith("textures/enchantment/generated/"),
                "GrayscaleAllGeneratedEnchantmentIcons",
                context -> {
                    Identifier resourceId = context.getResourceId();
                    Identifier newId = Identifier.of(resourceId.getNamespace(), resourceId.getPath().replace("/generated/", "/generated/grayscale/"));
                    BufferedImage grayscaleImage = grayscaleImage(context.getFile());
                    context.createResource(newId, grayscaleImage);
                },
                false
        );

        Mixson.registerEvent(
                MixsonCodecs.PNG,
                Mixson.DEFAULT_PRIORITY + 100,
                id -> id.getPath().startsWith("textures/enchantment/small/"),
                "GrayscaleAllSmallEnchantmentIcons",
                context -> {
                    Identifier resourceId = context.getResourceId();
                    Identifier newId = Identifier.of(resourceId.getNamespace(), resourceId.getPath().replace("/small/", "/generated/grayscale/small/"));
                    BufferedImage grayscaleImage = grayscaleImage(context.getFile());
                    context.createResource(newId, grayscaleImage);
                },
                false
        );

        Mixson.registerEvent(
                MixsonCodecs.PNG,
                Mixson.DEFAULT_PRIORITY + 100,
                id -> id.getPath().startsWith("textures/enchantment/large/"),
                "GrayscaleLargeSmallEnchantmentIcons",
                context -> {
                    Identifier resourceId = context.getResourceId();
                    Identifier newId = Identifier.of(resourceId.getNamespace(), resourceId.getPath().replace("/large/", "/generated/grayscale/large/"));
                    BufferedImage grayscaleImage = grayscaleImage(context.getFile());
                    context.createResource(newId, grayscaleImage);
                },
                false
        );
    }

    private static void registerGlintTransformer(String path, String transformedPath) {
        Color shiftColor = new Color(0xFFA601);
        Mixson.registerEvent(
                MixsonCodecs.PNG,
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().contains(path),
                "GenerateGlint_"+path,
                context -> {
                    Dunchanting.LOGGER.info("generating glint texture for {}", context.getResourceId());
                    BufferedImage image = context.getFile();
                    BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    for(int i = 0; i < image.getWidth(); i++)
                        for(int j = 0; j < image.getHeight(); j++) {
                            double gray = (grayscalePixel(image.getRGB(i, j)) & 0xFF) / 255d;
                            Color newColor = new Color(
                                    (int) (shiftColor.getRed() * gray),
                                    (int) (shiftColor.getGreen() * gray),
                                    (int) (shiftColor.getBlue() * gray)
                            );
                            newImage.setRGB(i, j, newColor.getRGB());
                        }
                    context.createResource(Dunchanting.id(transformedPath).withSuffixedPath(".png"), newImage);
                    context.setDebugExport(newImage);
                },
                false
        );
    }

    private static BufferedImage grayscaleImage(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                newImage.setRGB(x, y, grayscalePixel(image.getRGB(x, y)));
            }
        }
        return newImage;
    }

    private static int grayscalePixel(int color) {
        Color c = new Color(color, true);
        float grayVal = (c.getRed() / 255f) * 0.299f + (c.getGreen() / 255f) * 0.587f + (c.getBlue() / 255f) * 0.114f;
        return new Color(grayVal, grayVal, grayVal, c.getAlpha() / 255f).getRGB();
    }

    private static void generateInBetweenTextures(EventContext<BufferedImage> context, BufferedImage large) {
        BufferedImage croppedLarge = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 40; x++) for (int y = 0; y < 40; y++) croppedLarge.setRGB(x, y, large.getRGB(x + 12, y + 12));
        BufferedImage small = context.getFile();
        BufferedImage croppedSmall = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) croppedSmall.setRGB(x, y, small.getRGB(x + 24, y + 24));

        for (int i = 18; i <= 40; i += 2) {
            BufferedImage newSmallImage = toBufferedImage(croppedSmall.getScaledInstance(i, i, Image.SCALE_SMOOTH));
            BufferedImage newLargeImage = toBufferedImage(croppedLarge.getScaledInstance(i, i, Image.SCALE_SMOOTH));
            BufferedImage finalImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            int offset = (64 - i) / 2;
            double newAlpha = ((i - 16) / 24.0);
            for (int x = 0; x < i; x++)
                for (int y = 0; y < i; y++) {
                    if (isCorner(x, y, i)) {
                        finalImage.setRGB(offset + x, offset + y, 0x00000000);
                        continue;
                    }
                    Color smallPixel = new Color(newSmallImage.getRGB(x, y));
                    Color largePixel = new Color(newLargeImage.getRGB(x, y));

                    Color newColor = new Color(
                            (int) (smallPixel.getRed() * (1 - newAlpha) + largePixel.getRed() * (newAlpha)),
                            (int) (smallPixel.getGreen() * (1 - newAlpha) + largePixel.getGreen() * (newAlpha)),
                            (int) (smallPixel.getBlue() * (1 - newAlpha) + largePixel.getBlue() * (newAlpha))
                    );
                    finalImage.setRGB(offset + x, offset + y, newColor.getRGB());
                }
            Identifier identifier = Identifier.of(context.getResourceId().toString().replace("small", "generated").replace(".png", "")).withSuffixedPath("/" + ((i - 16) / 2) + ".png");
            context.createResource(identifier, finalImage);
        }
    }

    private static void debugLargeTextures(EventContext<BufferedImage> context, String unused) {
        BufferedImage large = context.getFile();
        BufferedImage croppedLarge = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        for(int x = 0; x < 40; x++) for(int y = 0; y < 40; y++) croppedLarge.setRGB(x, y, large.getRGB(x + 12, y + 12));
        for(int i = 18; i <= 40; i += 2) {
            BufferedImage scaled = toBufferedImage(croppedLarge.getScaledInstance(i, i, Image.SCALE_SMOOTH));
            BufferedImage finalImg = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            int offset = (64 - i) / 2;
            for(int x = 0; x < i; x++)
                for(int y = 0; y < i; y++) {
                    if (isCorner(x, y, i)) finalImg.setRGB(offset + x, offset + y, 0x00000000);
                    else finalImg.setRGB(offset + x, offset + y, scaled.getRGB(x, y));
                }
            Identifier identifier = Identifier.of(context.getResourceId().toString().replace("/large/", "/generated/").replace(".png", "")).withSuffixedPath("/" + ((i - 16) / 2) + ".png");
            context.createResource(identifier, finalImg);
            Dunchanting.LOGGER.info("created resource: {}", identifier);
        }
    }

    private static void debugSmallTextures(EventContext<BufferedImage> context, String small_id) {
        BufferedImage small = context.getReference(small_id).retrieve().orElseThrow();
        BufferedImage croppedSmall = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for(int x = 0; x < 16; x++) for(int y = 0; y < 16; y++) croppedSmall.setRGB(x, y, small.getRGB(x + 24, y + 24));
        for(int i = 18; i <= 40; i += 2) {
            BufferedImage scaled = toBufferedImage(croppedSmall.getScaledInstance(i, i, Image.SCALE_SMOOTH));
            BufferedImage finalImg = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            int offset = (64 - i) / 2;
            for(int x = 0; x < i; x++)
                for(int y = 0; y < i; y++) {
                    if (isCorner(x, y, i)) finalImg.setRGB(offset + x, offset + y, 0x00000000);
                    else finalImg.setRGB(offset + x, offset + y, scaled.getRGB(x, y));
                }
            Identifier identifier = Identifier.of(context.getResourceId().toString().replace("large", "generated").replace(".png", "")).withSuffixedPath("/" + ((i - 16) / 2) + ".png");
            context.createResource(identifier, finalImg);
            Dunchanting.LOGGER.info("created resource: {}", identifier);
        }
    }

    private static boolean isCorner(int x, int y, int i) {
        return (manhattanDistance(x, y, i - 1, i - 1) <= i / 2d - 1) ||
                (manhattanDistance(x, y, 0, i - 1) <= i / 2d - 1) ||
                (manhattanDistance(x, y, i - 1, 0) <= i / 2d - 1) ||
                (manhattanDistance(x, y, 0, 0) <= i / 2d - 1);
    }
}
