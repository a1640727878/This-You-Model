package sky_bai.mod.tym.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
        List<String> model_name = new ArrayList<>(data.size());
        data.forEach(d -> model_name.add(d.name));
        model_name.add(GlTFModelManager.getManager().getDefaultModelData().name);

        LiteralArgumentBuilder<CommandSourceStack> set_model = Commands.literal("set_model");

        for (String name : model_name) {
            set_model.then(Commands.literal(name).executes((context) -> {
                CommandSourceStack stack = context.getSource();
                if (stack.getEntity() != null && stack.getEntity() instanceof Player player)
                    PlayerModelManager.getManager().set(player, name);
                return 0;
            }));
        }

        command.then(set_model);
        dispatcher.register(command);
    }

}
