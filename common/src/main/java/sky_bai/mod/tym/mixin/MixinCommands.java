package sky_bai.mod.tym.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sky_bai.mod.tym.command.TYM_Command;

@Mixin(Commands.class)
public class MixinCommands {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(
            method = "<init>(Lnet/minecraft/commands/Commands$CommandSelection;)V",
            at = @At("RETURN")
    )
    public void inject_$init$(Commands.CommandSelection commandSelection, CallbackInfo ci) {
        TYM_Command.register(this.dispatcher);
    }
}
