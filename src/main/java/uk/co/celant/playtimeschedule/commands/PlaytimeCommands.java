package uk.co.celant.playtimeschedule.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import uk.co.celant.playtimeschedule.Permissions;
import uk.co.celant.playtimeschedule.PlaytimeSchedule;
import uk.co.celant.playtimeschedule.capabilities.IPlaytimeCapability;
import uk.co.celant.playtimeschedule.capabilities.PlaytimeCapability;

import java.text.DecimalFormat;
import java.time.Duration;

@Mod.EventBusSubscriber(modid = PlaytimeSchedule.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlaytimeCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        CommandPlaytime.register(dispatcher);
    }

    public static class CommandPlaytime {
        private static final String CMD_PREFIX = "playtime";

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal(CMD_PREFIX)
                            .then(commandPlaytimeReset())
                            .then(commandPlaytimeLeft())
            );
        }

        private static LiteralArgumentBuilder<CommandSourceStack> commandPlaytimeReset() {
            return Commands.literal("reset")
                    .requires(Permissions.COMMAND_RESET.and(cs -> cs.getEntity() instanceof ServerPlayer))
                    .then(
                            Commands.argument("targetPlayer", EntityArgument.players())
                                    .executes(CommandPlaytime::resetPlaytime)
                    );
        }

        private static LiteralArgumentBuilder<CommandSourceStack> commandPlaytimeLeft() {
            return Commands.literal("left")
                    .executes(CommandPlaytime::getPlaytime);
        }

        private static int getPlaytime(CommandContext<CommandSourceStack> ctx) {
            Player player = ctx.getSource().getPlayer();
            if (player == null) return 0;

            IPlaytimeCapability playtime = player.getCapability(PlaytimeSchedule.PLAYTIME).orElse(new PlaytimeCapability());
            String remaining = playtime.getPlaytimeLeftString();
            player.sendSystemMessage(Component.literal(remaining));
            return 1;
        }

        private static int resetPlaytime(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "targetPlayer");
            IPlaytimeCapability playtime = targetPlayer.getCapability(PlaytimeSchedule.PLAYTIME).orElse(new PlaytimeCapability());
            playtime.resetPlaytime();
            ctx.getSource().sendSystemMessage(Component.literal("Resetting playtime"));
            return 1;
        }
    }
}
