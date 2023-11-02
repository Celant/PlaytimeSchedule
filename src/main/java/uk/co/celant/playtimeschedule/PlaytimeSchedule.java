package uk.co.celant.playtimeschedule;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.server.permission.PermissionAPI;
import org.slf4j.Logger;
import uk.co.celant.playtimeschedule.capabilities.IPlaytimeCapability;
import uk.co.celant.playtimeschedule.capabilities.PlaytimeCapability;
import uk.co.celant.playtimeschedule.capabilities.PlaytimeCapabilityProvider;
import uk.co.celant.playtimeschedule.config.PlaytimeScheduleConfig;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PlaytimeSchedule.MODID)
public class PlaytimeSchedule
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "playtimeschedule";
    public static final Capability<IPlaytimeCapability> PLAYTIME = CapabilityManager.get(new CapabilityToken<>(){});
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Events EVENTS = new Events();

    public static PlaytimeScheduleConfig CONFIG;

    public PlaytimeSchedule()
    {
        CONFIG = CommonRegistry.registerConfig(ModConfig.Type.COMMON, PlaytimeScheduleConfig.class, true);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(EVENTS);
        MinecraftForge.EVENT_BUS.addListener(Permissions::registerPermissionNodes);
    }
}
