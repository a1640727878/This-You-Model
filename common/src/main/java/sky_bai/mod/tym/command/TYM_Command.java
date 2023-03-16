package sky_bai.mod.tym.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import sky_bai.mod.tym.manager.GlTFModelManager;
import sky_bai.mod.tym.manager.PlayerModelManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TYM_Command {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("tym");
        Set<GlTFModelManager.ModelData> data = GlTFModelManager.getManager().getGlTF();
        List<String> modelName = new ArrayList<>(data.size());
        data.forEach(d -> modelName.add(d.name));
        modelName.add(GlTFModelManager.getManager().getDefaultModelData().name);

        LiteralArgumentBuilder<CommandSourceStack> set_model = Commands.literal("set_model");

        for (String name : modelName) {
            set_model.then(Commands.literal(name).executes((context) -> {
                CommandSourceStack stack = context.getSource();
                if (stack.getEntity() != null && stack.getEntity() instanceof Player player)
                    PlayerModelManager.getManager().set(player.getStringUUID(), name);
                return 0;
            }));
        }

        LiteralArgumentBuilder<CommandSourceStack> rule = Commands.literal("rule");

        rule.then(Commands.literal("me_model")
                .then(Commands.literal("true").executes(context -> setOpen(context, true)))
                .then(Commands.literal("false").executes(context -> setOpen(context, false))));

        command.then(set_model).then(rule);
        dispatcher.register(command);
    }


    private static int setOpen(CommandContext<CommandSourceStack> context, boolean b) {
        CommandSourceStack stack = context.getSource();
        if (stack.getEntity() != null && stack.getEntity() instanceof Player player)
            PlayerModelManager.getManager().setOpen(player, b);
        return 0;
    }

}
