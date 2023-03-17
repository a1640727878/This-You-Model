package sky_bai.mod.tym.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import sky_bai.mod.tym.manager.GlTFModelManager;
import sky_bai.mod.tym.manager.IOManager;
import sky_bai.mod.tym.manager.NetworkManager;
import sky_bai.mod.tym.manager.PlayerModelManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TYM_Command {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("tym");
        LiteralArgumentBuilder<CommandSourceStack> player_command = Commands.literal("player").requires(arg -> arg.hasPermission(2));
        Set<GlTFModelManager.ModelData> data = GlTFModelManager.getManager().getGlTF();
        List<String> modelName = new ArrayList<>(data.size());
        data.forEach(d -> modelName.add(d.name));
        modelName.add(GlTFModelManager.getManager().getDefaultModelData().name);

        thenPlayer(player_command, modelName);

        command.then(player_command);
        then(command, modelName);
        dispatcher.register(command);
    }


    private static String is(boolean b) {
        return b ? "true" : "false";
    }

    private static void thenPlayerOpen(ArgumentBuilder<CommandSourceStack, ?> builder, boolean b) {
        builder.then(Commands.literal(is(b)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
            for (ServerPlayer player : players)
                PlayerModelManager.getManager().setOpen(player, b);
            return 0;
        }));
    }

    private static void thenOpen(ArgumentBuilder<CommandSourceStack, ?> builder, boolean b) {
        builder.then(Commands.literal(is(b)).executes(context -> {
            if (context.getSource().getEntity() instanceof Player player)
                PlayerModelManager.getManager().setOpen(player, b);
            return 0;
        }));
    }


    private static void thenPlayer(LiteralArgumentBuilder<CommandSourceStack> player_command, List<String> modelName) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> players_builder = Commands.argument("player", EntityArgument.players());

        LiteralArgumentBuilder<CommandSourceStack> set_model = Commands.literal("set_model");

        for (String name : modelName) {
            set_model.then(Commands.literal(name).executes((context) -> {
                Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
                for (ServerPlayer player : players) PlayerModelManager.getManager().set(player, name);
                sendPlayerSetModel(context.getSource().getServer(),name);
                return 0;
            }));
        }
        players_builder.then(set_model);

        LiteralArgumentBuilder<CommandSourceStack> open_model = Commands.literal("open_model");
        thenPlayerOpen(open_model, true);
        thenPlayerOpen(open_model, false);

        players_builder.then(open_model);

        player_command.then(players_builder);
    }

    private static void then(LiteralArgumentBuilder<CommandSourceStack> command, List<String> modelName) {
        LiteralArgumentBuilder<CommandSourceStack> set_model = Commands.literal("set_model");

        for (String name : modelName) {
            set_model.then(Commands.literal(name).executes(context -> {
                CommandSourceStack stack = context.getSource();
                if (stack.getEntity() instanceof Player player) {
                    PlayerModelManager.getManager().set(player, name);
                    sendPlayerSetModel(context.getSource().getServer(),name);
                }
                return 0;
            }));
        }
        command.then(set_model);

        LiteralArgumentBuilder<CommandSourceStack> open_model = Commands.literal("open_model");
        thenOpen(open_model, true);
        thenOpen(open_model, false);

        command.then(open_model);
    }

    private static void sendPlayerSetModel(MinecraftServer server,String model_name){
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkManager.getManager().S2C_SET_PLAYER_MODEL.send(player,
                    IOManager.theStringToByteBuf(player.getStringUUID() + "<->" + model_name));
        }
    }

}
