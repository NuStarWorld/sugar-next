package team.idealstate.sugar.next.test;

import team.idealstate.sugar.next.command.CommandContext;
import team.idealstate.sugar.next.command.CommandResult;
import team.idealstate.sugar.next.command.annotation.CommandArgument;
import team.idealstate.sugar.next.command.annotation.CommandHandler;
import team.idealstate.sugar.validate.annotation.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author NuStar<br>
 * @since 2026/1/27 21:28<br>
 */
public class ExampleCommand {

    private final List<String> configs = Arrays.asList("aaa", "bbb");

    @CommandHandler
    public CommandResult reload() {
        return CommandResult.success("重载成功");
    }

    @NotNull
    public List<String> getConfigs(@NotNull CommandContext context, @NotNull String argument) {
        if (argument.isEmpty()) {
            return configs;
        }
        return configs.stream().filter(s -> s.toLowerCase().startsWith(argument.toLowerCase())).collect(Collectors.toList());
    }

    @CommandHandler("fuck abc {name}")
    public CommandResult fuck(CommandContext context, @CommandArgument("name") String target) {
        return CommandResult.success("fuck: " + target + " " + context);
    }

    @CommandHandler("fuck abc {name} {name1}")
    public CommandResult fuck1(CommandContext context, @CommandArgument("name") String target, @CommandArgument("name1") String target1) {
        return CommandResult.success("fuck: " + target + " " + context);
    }

    @CommandHandler("fuck bbb abc {name} {name1}")
    public CommandResult fuckAbc(CommandContext context, @CommandArgument("name") String target, @CommandArgument("name1") String target1) {
        return CommandResult.success("fuck: " + target + " " + context);
    }

    @CommandHandler("fuck bbb ccc {name} {name1}")
    public CommandResult fuckAbc1(CommandContext context, @CommandArgument("name") String target, @CommandArgument("name1") String target1) {
        return CommandResult.success("fuck: " + target + " " + context);
    }

    @CommandHandler("fuck test {abc}")
    public CommandResult test(CommandContext context, @CommandArgument("abc") String target) {
        return CommandResult.success("abc: " + target + " " + context);
    }

    @CommandHandler("bookDick test {abc}")
    public CommandResult book(CommandContext context, @CommandArgument("abc") String target) {
        return CommandResult.success("abc: " + target + " " + context);
    }

    @CommandHandler("bookDick abc {abc}")
    public CommandResult bookabc(CommandContext context, @CommandArgument("abc") String target) {
        return CommandResult.success("abc: " + target + " " + context);
    }

    @CommandHandler("reload {id}")
    public CommandResult reload(@CommandArgument(completer = "getConfigs") String id) {
        return CommandResult.success("重载成功: " + id);
    }
}
