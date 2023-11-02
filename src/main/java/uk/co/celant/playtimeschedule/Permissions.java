package uk.co.celant.playtimeschedule;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContextKey;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionNode.PermissionResolver;
import net.minecraftforge.server.permission.nodes.PermissionType;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Permissions {
    private static final List<PermissionNode<?>> NODES_TO_REGISTER = new ArrayList<>();
    private static final PermissionResolver<Boolean> PLAYER_IS_OP = (player, uuid, context) -> player != null && player.hasPermissions(Commands.LEVEL_GAMEMASTERS);

    public static final PermissionNode<Boolean> BYPASS_FREEPLAY = node("bypass_freeplay", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.server.getPlayerList().isOp(player.getGameProfile()));

    public static final CommandPermissionNode COMMAND_RESET = nodeOpCommand("reset");

    private static CommandPermissionNode nodeOpCommand(String nodeName) {
        PermissionNode<Boolean> node = node("command." + nodeName, PermissionTypes.BOOLEAN, PLAYER_IS_OP);
        return new CommandPermissionNode(node, Commands.LEVEL_GAMEMASTERS);
    }

    @SafeVarargs
    private static <T> PermissionNode<T> node(String nodeName, PermissionType<T> type, PermissionNode.PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<T>... dynamics) {
        PermissionNode<T> node = new PermissionNode<>(PlaytimeSchedule.MODID, nodeName, type, defaultResolver, dynamics);
        NODES_TO_REGISTER.add(node);
        return node;
    }

    public static void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(NODES_TO_REGISTER);
    }

    public record CommandPermissionNode(PermissionNode<Boolean> node, int fallbackLevel) implements Predicate<CommandSourceStack> {

        @Override
        public boolean test(CommandSourceStack source) {
            //See https://github.com/MinecraftForge/MinecraftForge/commit/f7eea35cb9b043aae0a3866a9578724aa7560585 for details on why
            // has permission is checked first and the implications
            return source.hasPermission(fallbackLevel) || source.source instanceof ServerPlayer player && PermissionAPI.getPermission(player, node);
        }
    }
}
