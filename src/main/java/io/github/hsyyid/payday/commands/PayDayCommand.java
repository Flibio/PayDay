package io.github.hsyyid.payday.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class PayDayCommand implements CommandExecutor {
    public static CommandSpec getCommandSpec() {
        return CommandSpec.builder()
                .description(Text.of("PayDay commands"))
                .permission("payday.base")
                .executor(new PayDayCommand())
                .child(ReloadCommand.getCommandSpec(), "reload")
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of("PayDay commands:"));
        src.sendMessage(Text.of("/payday reload"));
        src.sendMessage(Text.of("  - " + ReloadCommand.description));
        return CommandResult.empty();
    }
}
