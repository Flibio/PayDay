package io.github.hsyyid.payday.commands;

import io.github.hsyyid.payday.PayDay;
import io.github.hsyyid.payday.utils.Utils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class ReloadCommand implements CommandExecutor {
    public static final String description = "Reloads the PayDay config";

    public static CommandSpec getCommandSpec() {
        return CommandSpec.builder()
                .description(Text.of(description))
                .permission("payday.reload")
                .executor(new ReloadCommand())
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PayDay.reload();
        Utils.warnIfMissingAfkService(src);
        src.sendMessage(Text.of("Reloaded config!"));
        return CommandResult.success();
    }
}
