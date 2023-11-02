package uk.co.celant.playtimeschedule;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.PermissionAPI;
import uk.co.celant.playtimeschedule.capabilities.IPlaytimeCapability;
import uk.co.celant.playtimeschedule.capabilities.PlaytimeCapability;
import uk.co.celant.playtimeschedule.capabilities.PlaytimeCapabilityProvider;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;

@Mod.EventBusSubscriber(modid = PlaytimeSchedule.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events {
    private int tick;

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlaytimeCapability.class);
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        event.addCapability(PlaytimeCapabilityProvider.IDENTIFIER, new PlaytimeCapabilityProvider());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        IPlaytimeCapability playtime = player.getCapability(PlaytimeSchedule.PLAYTIME).orElse(new PlaytimeCapability());
        String remaining = playtime.getPlaytimeLeftString();
        player.sendSystemMessage(Component.literal(remaining));
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) throws Exception {
        Player player = event.getEntity();
        Player oldPlayer = event.getOriginal();
        oldPlayer.reviveCaps();
        IPlaytimeCapability playtime = player.getCapability(PlaytimeSchedule.PLAYTIME).orElseThrow(Exception::new);
        IPlaytimeCapability oldPlaytime = oldPlayer.getCapability(PlaytimeSchedule.PLAYTIME).orElseThrow(Exception::new);
        oldPlayer.invalidateCaps();
        playtime.copy(oldPlaytime);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (!e.side.isServer() || e.phase != TickEvent.Phase.START || e.type != TickEvent.Type.SERVER)
            return;

        tick++;
        if (tick % 20 != 0)
            return;

        Calendar c = Calendar.getInstance();
        List<Schedule> scheduleList = PlaytimeSchedule.CONFIG.freePlaySchedules.get(c.get(Calendar.DAY_OF_WEEK));
        e.getServer().getPlayerList().getPlayers().forEach((serverPlayer -> {
            if (!serverPlayer.isAlive()) return;

            IPlaytimeCapability playtime = serverPlayer.getCapability(PlaytimeSchedule.PLAYTIME).orElse(new PlaytimeCapability());

            if (playtime.dayHasChanged()) {
                playtime.resetPlaytime();
                serverPlayer.sendSystemMessage(
                        Component.literal(ChatFormatting.GREEN + "Your daily playtime has been reset.")
                );
            }

            boolean scheduleMatches = scheduleList.stream().anyMatch(Schedule::isBetween);

            boolean save = true;
            if (playtime.getInFreePlay()) {
                if (playerAlwaysFreePlay(serverPlayer) || scheduleMatches) {
                    save = false;
                } else {
                    serverPlayer.sendSystemMessage(
                            Component.literal(ChatFormatting.GREEN + "You are no longer in free play.")
                    );
                    playtime.setInFreePlay(false);
                }
            } else {
                if (playerAlwaysFreePlay(serverPlayer) || scheduleMatches) {
                    serverPlayer.sendSystemMessage(
                            Component.literal(ChatFormatting.GREEN + "You are now in free play, enjoy!")
                    );
                    playtime.setInFreePlay(true);
                    save = false;
                }
            }

            playtime.incrementPlaytime(save);
            if (!save) return;

            switch (playtime.getPlaytimeLeft()) {
                case 20 * 60:
                    serverPlayer.sendSystemMessage(
                            Component.literal(ChatFormatting.GREEN + "You have 20 minutes of playtime left")
                    );
                    break;
                case 10 * 60:
                    serverPlayer.sendSystemMessage(
                            Component.literal(ChatFormatting.GREEN + "You have 10 minutes of playtime left")
                    );
                    break;
                case 5 * 60:
                    serverPlayer.sendSystemMessage(
                            Component.literal(ChatFormatting.GREEN + "You have 5 minutes of playtime left")
                    );
                    break;
            }

            if (playtime.getPlaytimeLeft() == 0) {
                kickPlayer(serverPlayer);
            }
        }));
    }

    private boolean playerAlwaysFreePlay(ServerPlayer player) {
        if (PlaytimeSchedule.CONFIG.freePlayCreativeBypass.get() && player.isCreative())
            return true;
        if (PlaytimeSchedule.CONFIG.freePlayOpBypass.get() &&
                PermissionAPI.getPermission(player, Permissions.BYPASS_FREEPLAY))
            return true;
        return false;
    }

    private long millisUntilMidnight() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return (c.getTimeInMillis()-System.currentTimeMillis());
    }

    private void kickPlayer(ServerPlayer player) {
        if (player == null) return;

        long until = millisUntilMidnight();
        String seconds = new DecimalFormat("00").format(Duration.ofMillis(until).toSecondsPart());
        String minutes = new DecimalFormat("00").format(Duration.ofMillis(until).toMinutesPart());
        String hours = new DecimalFormat("00").format(Duration.ofMillis(until).toHoursPart());

        player.connection.disconnect(Component.literal(
                ChatFormatting.RED + "You've reached your daily playtime limit!"
                        + "\n\n"
                        + ChatFormatting.WHITE + "You will be able to join again in:"
                        + "\n"
                        + ChatFormatting.BOLD
                        + ChatFormatting.RED + hours + ChatFormatting.WHITE + " hours, "
                        + ChatFormatting.RED + minutes + ChatFormatting.WHITE + " minutes and "
                        + ChatFormatting.RED + seconds + ChatFormatting.WHITE + " seconds"
        ));
    }
}
