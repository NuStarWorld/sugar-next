package team.idealstate.sugar.next.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import team.idealstate.sugar.next.command.CommandContext;
import team.idealstate.sugar.next.command.CommandLine;
import team.idealstate.sugar.next.command.CommandResult;
import team.idealstate.sugar.next.command.CommandSender;
import team.idealstate.sugar.validate.annotation.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * @author NuStar<br>
 * @since 2026/1/27 21:28<br>
 */
public class TestCommand {

    @Test
    public void test() {
        TestCommandSender sender = new TestCommandSender(UUID.randomUUID(), true, Collections.emptySet());

        CommandLine commandLine = CommandLine.of(getClass().getSimpleName(), new ExampleCommand());
        execute(commandLine, sender);
    }

    private CommandResult execute(CommandLine commandLine, CommandSender sender, String... command) {
        CommandContext context = CommandContext.of(sender);
        return commandLine.execute(context, command);
    }

    @Data
    @AllArgsConstructor
    public static final class TestCommandSender implements CommandSender {
        @NonNull
        private final UUID uniqueId;
        private boolean administrator;
        @NonNull
        private Set<String> permissions;

        @Override
        public boolean hasPermission(@NotNull String permission) {
            return permissions.contains(permission);
        }

        @Override
        public void sendMessage(@NotNull String message) {
            System.out.println(message);
        }
    }
}
