package uk.co.celant.playtimeschedule;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.celant.playtimeschedule.config.ConfigBase;

import java.util.function.Consumer;

public class CommonRegistry {
    public static <T extends ConfigBase> T registerConfig(ModConfig.Type type, Class<T> configClass, boolean registerListener) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        T config;
        try {
            config = configClass.getConstructor(ForgeConfigSpec.Builder.class).newInstance(builder);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        ForgeConfigSpec spec = builder.build();
        ModLoadingContext.get().registerConfig(type, spec);
        config.setConfigSpec(spec);
        if (registerListener) {
            Consumer<ModConfigEvent> consumer = evt -> {
                if (evt.getConfig().getType() == type) {
                    config.onReload(evt);
                }
            };
            FMLJavaModLoadingContext.get().getModEventBus().addListener(consumer);
        }
        return config;
    }

    public static <T extends ConfigBase> T registerConfig(ModConfig.Type type, Class<T> configClass) {
        return registerConfig(type, configClass, false);
    }
}
