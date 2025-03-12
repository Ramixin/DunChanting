package net.ramixin.dunchants.client;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.DungeonEnchants;
import net.ramixin.util.ModUtils;
import net.ramixin.mixson.inline.EventContext;
import net.ramixin.mixson.inline.Mixson;
import net.ramixin.mixson.inline.MixsonCodec;
import net.ramixin.mixson.util.MixsonUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static net.ramixin.util.ModUtils.manhattanDistance;
import static net.ramixin.util.ModUtils.toBufferedImage;

@SuppressWarnings("unused")
public class ModMixsonClient {

    public static final MixsonCodec<BufferedImage> BUFFERED_IMAGE_PNG_MIXSON_CODEC = MixsonCodec.create("png",
            resource -> ImageIO.read(resource.getInputStream()),
            (r, elem) -> new Resource(r.getPack(), () -> new ByteArrayInputStream(ModUtils.bufferedImageToStream(elem).toByteArray()), r::getMetadata),
            ModUtils::bufferedImageToStream
    );

    public static void onInitialize() {
        Mixson.registerEvent(
                BUFFERED_IMAGE_PNG_MIXSON_CODEC,
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().startsWith("textures/enchantment/large/"),
                "FindAllLargeEnchantmentIcons",
                context -> {

                    Identifier large_id = MixsonUtil.removeExtension(context.getResourceId());
                    Identifier small_id = Identifier.of(large_id.getNamespace(), large_id.getPath().replace("large", "small"));
                    context.registerRuntimeEvent(
                            Mixson.DEFAULT_PRIORITY,
                            id -> id.equals(small_id),
                            "generateTransitionTextures",
                            context2 -> generateInBetweenTextures(context2, context.getFile()),
                            true
                    );

                },
                false
        );
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
                    int cornerCount = getCornerCount(x, y, i);
                    if (cornerCount != 0) {
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
                    int cornerCount = getCornerCount(x, y, i);
                    if (cornerCount != 0) finalImg.setRGB(offset + x, offset + y, 0x00000000);
                    else finalImg.setRGB(offset + x, offset + y, scaled.getRGB(x, y));
                }
            Identifier identifier = Identifier.of(context.getResourceId().toString().replace("large", "generated").replace(".png", "")).withSuffixedPath("/" + ((i - 16) / 2) + ".png");
            context.createResource(identifier, finalImg);
            DungeonEnchants.LOGGER.info("created resource: {}", identifier);
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
                    int cornerCount = getCornerCount(x, y, i);
                    if (cornerCount != 0) finalImg.setRGB(offset + x, offset + y, 0x00000000);
                    else finalImg.setRGB(offset + x, offset + y, scaled.getRGB(x, y));
                }
            Identifier identifier = Identifier.of(context.getResourceId().toString().replace("large", "generated").replace(".png", "")).withSuffixedPath("/" + ((i - 16) / 2) + ".png");
            context.createResource(identifier, finalImg);
            DungeonEnchants.LOGGER.info("created resource: {}", identifier);
        }
    }

    private static int getCornerCount(int x, int y, int i) {
        int cornerCount = 0;
        if (manhattanDistance(x, y, 0, 0) <= i / 2d - 1) cornerCount++;
        if (manhattanDistance(x, y, i - 1, 0) <= i / 2d - 1) cornerCount++;
        if (manhattanDistance(x, y, i - 1, i - 1) <= i / 2d - 1) cornerCount++;
        if (manhattanDistance(x, y, 0, i - 1) <= i / 2d - 1) cornerCount++;
        return cornerCount;
    }
}
