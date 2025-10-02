package net.ramixin.dunchanting;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class DunchantingEarlyLoader implements Runnable {

    @Override
    public void run() {
        Dunchanting.LOGGER.info("Initializing Dunchanting (1/2)");
        MappingResolver mapper = FabricLoader.getInstance().getMappingResolver();
        String itemRenderGlintEnumClass = mapper.mapClassName("intermediary", "net.minecraft.class_10444$class_10445");
        ClassTinkerers.enumBuilder(itemRenderGlintEnumClass).addEnum("STANDARD_GILDED").addEnum("SPECIAL_GILDED").build();
    }
}
