/*
 *    Copyright 2025 ideal-state
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package team.idealstate.sugar.next.command;

import static team.idealstate.sugar.next.function.Functional.pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import lombok.*;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.next.command.annotation.CommandArgument;
import team.idealstate.sugar.next.command.annotation.CommandHandler;
import team.idealstate.sugar.next.command.exception.CommandArgumentConversionException;
import team.idealstate.sugar.next.command.exception.CommandException;
import team.idealstate.sugar.next.databind.Pair;
import team.idealstate.sugar.next.function.Lazy;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class SimpleCommandLine implements CommandLine {

    public static final int ROOT_DEPTH = -1;
    public static final String ARGUMENTS_DELIMITER = " ";
    public static final String PERMISSION_DELIMITER = ".";
    private final int depth;

    @NotNull
    private final String name;

    private String description = "";

    private final boolean isArgument;

    private SimpleCommandHelpTree helpTree;

    @NotNull
    private final List<String> permission;

    private final boolean open;

    @NotNull
    private final Class<?> argumentType;

    private final CommandArgument.Converter<?> converter;
    private final CommandArgument.Completer completer;
    private final Deque<SimpleCommandLine> children = new ArrayDeque<>();
    private CommandExecutor executor;

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes", "ExtractMethodRecommender"})
    public static SimpleCommandLine of(@NotNull String name, @NotNull Object command) {
        CommandLine.validateName(name);
        Validation.notNull(command, "command must not be null.");
        // lazyRoot 让根节点能被下面的不全闭包引用（闭包先定义，root 后创建）
        final AtomicReference<SimpleCommandLine> lazyRoot = new AtomicReference<>();
        // 根级补全器：把所有可见子节点的补全结果拼起来
        CommandArgument.Completer completer = (context, argument) -> {
            SimpleCommandLine commandLine = lazyRoot.get();
            Deque<SimpleCommandLine> children = new ArrayDeque<>(commandLine.children);
            if (children.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> ret = new ArrayList<>(children.size());
            CommandSender sender = context.getSender();
            for (SimpleCommandLine child : children) {
                if (!validate(sender, child.permission, child.isOpen())) {
                    continue;
                }
                CommandArgument.Completer childCompleter = child.completer;
                if (childCompleter != null) {
                    ret.addAll(childCompleter.complete(context, argument));
                }
            }
            return ret;
        };
        // 构造根节点：深度 -1，权限=根名，开放命令，参数类型占位为 String
        SimpleCommandLine root = new SimpleCommandLine(
                ROOT_DEPTH, name,false, Collections.singletonList(name), true, String.class, null, completer);
        lazyRoot.set(root);
        Class<?> commandType = command.getClass();
        Method[] methods = commandType.getMethods();
        if (methods.length == 0) {
            return root;
        }
        // 先按参数个数升序排序，减少歧义时的差异
        methods = Arrays.stream(methods)
                .sorted(Comparator.comparingInt(Method::getParameterCount))
                .toArray(Method[]::new);
        for (Method method : methods) {
            // 只接受实例方法且返回值必须是 CommandResult
            if (Modifier.isStatic(method.getModifiers()) || !CommandResult.class.equals(method.getReturnType())) {
                continue;
            }
            CommandHandler commandHandler = method.getDeclaredAnnotation(CommandHandler.class);
            if (commandHandler == null) {
                continue;
            }
            String value = commandHandler.value();
            String description = commandHandler.description();
            String methodName = method.getName();
            if (value.isEmpty()) {
                value = methodName;
            }
            // 拆分路径，变量用 {var} 表示，变量必须出现在字面量之后
            String[] arguments = value.split(ARGUMENTS_DELIMITER);
            int variableCount = 0;
            for (int i = 0; i < arguments.length; i++) {
                String argument = arguments[i];
                int last = argument.length() - 1;
                if (argument.charAt(0) == '{' && argument.charAt(last) == '}') {
                    argument = argument.substring(1, last);
                    arguments[i] = argument;
                    variableCount++;
                } else {
                    if (variableCount > 0) {
                        throw new IllegalArgumentException("literal argument must before to variable argument.");
                    }
                }
                CommandLine.validateName(argument, "argument");
            }
            String[] permission = commandHandler.permission();
            if (permission.length == 0) {
                // 权限默认：根名 + 路径各段
                String[] temp = new String[arguments.length + 1];
                temp[0] = name;
                System.arraycopy(arguments, 0, temp, 1, arguments.length);
                permission = temp;
            }
            Parameter[] parameters = method.getParameters();
            // 收集所有变量参数的元数据；CommandContext 不算变量
            Map<String, Pair<Parameter, CommandArgument>> commandArguments = new HashMap<>(parameters.length);
            for (Parameter parameter : parameters) {
                CommandArgument commandArgument = parameter.getDeclaredAnnotation(CommandArgument.class);
                Class<?> parameterType = parameter.getType();
                if (commandArgument == null) {
                    if (CommandContext.class.equals(parameterType)) {
                        continue;
                    }
                    throw new IllegalArgumentException(
                            "parameter must be a CommandContext or annotated with" + " @CommandArgument.");
                }
                value = commandArgument.value();
                if (value.isEmpty()) {
                    value = parameter.getName();
                }
                commandArguments.put(value, Pair.of(parameter, commandArgument));
            }
            if (commandArguments.size() != variableCount) {
                // 变量段数量必须等于带 @CommandArgument 的参数数量
                throw new IllegalArgumentException("parameter size must be same as variable size.");
            }
            SimpleCommandLine parent = root;
            int variableStart = arguments.length - variableCount;
            // 逐段下钻构建节点链（parent 始终指向当前层）
            for (int i = 0; i < arguments.length; i++) {
                String childName = arguments[i];
                CommandArgument.Converter<?> converter = null;
                completer = null;
                boolean isVariable = i >= variableStart;
                Class<?> parameterType = null;
                if (isVariable) {
                    // 变量段：绑定对应参数并准备转换器/补全器
                    Pair<Parameter, CommandArgument> pair = commandArguments.get(childName);
                    Parameter parameter;
                    if (pair == null || (parameter = pair.getFirst()) == null) {
                        throw new IllegalArgumentException(
                                String.format("%s(...): parameter '%s' cannot be found.", methodName, childName));
                    }
                    parameterType = parameter.getType();
                    CommandArgument commandArgument = pair.getSecond();
                    if (commandArgument == null) {
                        throw new IllegalArgumentException(String.format(
                                "%s(...): variable parameter '%s' must be annotated with" + " @CommandArgument.",
                                methodName, childName));
                    }
                    try {
                        Class<? extends CommandArgument.Converter> converterClass = commandArgument.converterType();
                        if (!CommandArgument.Converter.class.equals(converterClass)) {
                            Constructor<? extends CommandArgument.Converter> constructor =
                                    converterClass.getConstructor();
                            converter = constructor.newInstance();
                        } else {
                            // 方法名转换器：不可 static，返回类型必须是 ConverterResult
                            String converterMethodName = commandArgument.converter();
                            if (!converterMethodName.isEmpty()) {
                                Method converterMethod = commandType.getMethod(
                                        converterMethodName, CommandContext.class, String.class, boolean.class);
                                if (Modifier.isStatic(converterMethod.getModifiers())) {
                                    throw new IllegalArgumentException(String.format(
                                            "%s(...): converter method '%s' must not be" + " static.",
                                            methodName, converterMethodName));
                                }
                                if (!CommandArgument.ConverterResult.class.isAssignableFrom(
                                        converterMethod.getReturnType())) {
                                    throw new IllegalArgumentException(String.format(
                                            "%s(...): converter method '%s' return type"
                                                    + " must be assignable to ConverterResult.",
                                            methodName, converterMethodName));
                                }
                                converterMethod.setAccessible(true);
                                converter = new SimpleCommandArgumentConverter(parameterType, command, converterMethod);
                            }
                            // 默认转换器：接受任意字符串直接原样返回
                            if (converter == null) {
                                converter = new CommandArgument.AbstractConverter<String>(String.class) {
                                    @NotNull
                                    @Override
                                    protected CommandArgument.ConverterResult<String> doConvert(
                                            @NotNull CommandContext context, @NotNull String argument)
                                            throws CommandArgumentConversionException {
                                        return CommandArgument.ConverterResult.success(argument);
                                    }

                                    @Override
                                    protected boolean canBeConvert(
                                            @NotNull CommandContext context, @NotNull String argument) {
                                        return true;
                                    }
                                };
                            }
                        }
                        Class<? extends CommandArgument.Completer> completerClass = commandArgument.completerType();
                        if (!CommandArgument.Completer.class.equals(completerClass)) {
                            Constructor<? extends CommandArgument.Completer> constructor =
                                    completerClass.getConstructor();
                            completer = constructor.newInstance();
                        } else {
                            // 方法名补全器：不可 static，返回类型必须是 List
                            String completerMethodName = commandArgument.completer();
                            if (!completerMethodName.isEmpty()) {
                                Method completerMethod =
                                        commandType.getMethod(completerMethodName, CommandContext.class, String.class);
                                if (Modifier.isStatic(completerMethod.getModifiers())) {
                                    throw new IllegalArgumentException(String.format(
                                            "%s(...): completer method '%s' must not be" + " static.",
                                            methodName, completerMethodName));
                                }
                                if (!List.class.isAssignableFrom(completerMethod.getReturnType())) {
                                    throw new IllegalArgumentException(String.format(
                                            "%s(...): completer method '%s' return type"
                                                    + " must be assignable to List.",
                                            methodName, completerMethodName));
                                }
                                completerMethod.setAccessible(true);
                                completer = new SimpleCommandArgumentCompleter(command, completerMethod);
                            }
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new CommandException(e);
                    }
                }
                if (isVariable) {
                    if (parameterType == null) {
                        throw new IllegalArgumentException(String.format(
                                "%s(...): variable parameter '%s' must have a type.", methodName, childName));
                    }
                } else {
                    // 字面量段：不全用前缀匹配
                    List<String> list = Collections.singletonList(childName);
                    completer = (context, argument) -> {
                        if (argument.isEmpty() || childName.toLowerCase().startsWith(argument.toLowerCase())) {
                            return list;
                        }
                        return Collections.emptyList();
                    };
                }
                // 将子节点追加到当前父节点并推进 parent
                parent = parent.addChild(
                        childName,
                        isVariable,
                        Arrays.asList(permission),
                        commandHandler.open(),
                        parameterType == null ? String.class : parameterType,
                        converter,
                        completer);
            }
            // 末节点绑定执行器（直接反射调用目标方法）
            method.setAccessible(true);
            parent.setDescription(description);
            parent.executor = new SimpleCommandExecutor(command, method);
        }
        return root;
    }

    public static String permissionOf(@NotNull List<String> permissionNodes) {
        Validation.notNull(permissionNodes, "permissionNodes must not be null.");
        if (permissionNodes.isEmpty()) {
            return "";
        }
        for (String permissionNode : permissionNodes) {
            CommandLine.validateName(permissionNode, "permissionNode");
        }
        if (permissionNodes.size() == 1) {
            return permissionNodes.get(0);
        }
        return String.join(PERMISSION_DELIMITER, permissionNodes);
    }

    private static int getLastChildDepth(@NotNull SimpleCommandLine parent) {
        int depth = parent.depth;
        for (SimpleCommandLine child : parent.children) {
            int childDepth = getLastChildDepth(child);
            if (childDepth > depth) {
                depth = childDepth;
            }
        }
        return depth;
    }

    @NotNull
    // 从当前节点出发匹配后续参数，返回“最高分”的命中路径
    private static Pair<Double, List<SimpleCommandLine>> accept(
            @NotNull SimpleCommandLine parent,
            @NotNull CommandContext context,
            int current,
            @NotNull String... arguments) {
        // next：即将匹配的参数下标
        int next = current + 1;
        if (next >= arguments.length) {
            return pair(0.D, Collections.emptyList());
        }
        // 遍历当前节点的子节点（拷贝一份，避免迭代时修改）
        Deque<SimpleCommandLine> children = new ArrayDeque<>(parent.children);
        if (children.isEmpty()) {
            return pair(0.D, Collections.emptyList());
        }
        String argument = arguments[next];
        // key: score，value: 命中的节点链
        Map<Double, List<SimpleCommandLine>> accepted = new HashMap<>(children.size());
        for (SimpleCommandLine child : children) {
            // 必须 depth 精确命中，且 child.accept 通过才继续
            if (child.depth != next || !child.accept(context, argument)) {
                continue;
            }
            List<SimpleCommandLine> acceptedChildren = new ArrayList<>();
            List<SimpleCommandLine> nextAcceptedChildren = Collections.singletonList(child);
            do {
                // 将当前这一层命中的节点追加到链尾
                acceptedChildren.addAll(nextAcceptedChildren);
                // 递归尝试更深层的命中
                nextAcceptedChildren = accept(
                                acceptedChildren.get(acceptedChildren.size() - 1), context, next, arguments)
                        .getSecond();
            } while (!nextAcceptedChildren.isEmpty());
            int hit = acceptedChildren.size();
            // 未命中深度：末尾节点到底的距离（惩罚项）
            int unhit = getLastChildDepth(acceptedChildren.get(hit - 1)) - hit + 1;
            // 评分 = 命中率 - 未命中深度
            double score = hit * 1.0D / arguments.length - unhit;
            Log.debug(() -> String.format("score: %s / %s - %s = %s", hit, arguments.length, unhit, score));
            if (!accepted.containsKey(score)) {
                // 同分时保留先出现的（后面的直接丢弃）
                accepted.put(score, acceptedChildren);
            }
        }
        if (accepted.isEmpty()) {
            return pair(0.D, Collections.emptyList());
        }
        // 取最高分的命中链
        Double key = accepted.keySet().stream().max(Double::compare).get();
        return pair(key, accepted.get(key));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validate(@NotNull CommandContext context, @NotNull String... arguments) {
        Validation.notNull(context, "context must not be null.");
        Validation.notNull(arguments, "arguments must not be null.");
        for (String argument : arguments) {
            Validation.notNull(argument, "argument must not be null.");
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validate(
            @NotNull CommandSender sender, @NotNull List<String> permissionNodes, boolean open) {
        Validation.notNull(sender, "sender must not be null.");
        Validation.notNull(permissionNodes, "permissionNodes must not be null.");
        if (open || sender.isAdministrator()) {
            return true;
        }
        String permission = permissionOf(permissionNodes);
        if (permission.isEmpty()) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    @NotNull
    private SimpleCommandLine addChild(
            @NotNull String name,
            boolean isArgument,
            @NotNull List<String> permission,
            boolean open,
            @NotNull Class<?> argumentType,
            CommandArgument.Converter<?> converter,
            CommandArgument.Completer completer) {
        Validation.notNull(name, "name must not be null.");
        Validation.notNull(permission, "permission must not be null.");
        Validation.notNull(argumentType, "argumentType must not be null.");
        SimpleCommandLine child =
                new SimpleCommandLine(depth + 1, name, isArgument, permission, open, argumentType, converter, completer);
        children.add(child);
        return child;
    }

    private CommandArgument.Completer getCompleter(@NotNull CommandContext context) {
        CommandArgument.Completer completer = this.completer;
        return completer == null ? context.getCompleter(argumentType) : completer;
    }

    @SuppressWarnings({"unchecked"})
    private <T> CommandArgument.Converter<T> getConverter(@NotNull CommandContext context) {
        CommandArgument.Converter<?> converter = this.converter;
        return converter == null
                ? (CommandArgument.Converter<T>) context.getConverter(argumentType)
                : (CommandArgument.Converter<T>) converter;
    }

    private boolean accept(@NotNull CommandContext context, @NotNull String argument) {
        return converter == null
                ? getName().equalsIgnoreCase(argument)
                : getConverter(context).convert(context, argument, false).isSuccess();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String[] getPermission() {
        return permission.toArray(new String[0]);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public @NotNull CommandResult execute(@NotNull CommandContext context, @NotNull String... arguments)
            throws CommandException {
        if (!validate(context, arguments)) {
            return CommandResult.failure();
        }
        if (arguments.length == 0) {
            if (helpTree == null) {
                helpTree = new SimpleCommandHelpTree();
            }
            String message = helpTree.lazyHelpMessage.get();
            for (String line : message.split("\n")) {
                context.getSender().sendMessage(line);
            }
            return CommandResult.failure();
        }
        Pair<Double, List<SimpleCommandLine>> accept = accept(this, context, depth, arguments);
        List<SimpleCommandLine> acceptedChildren = accept.getSecond();
        if (acceptedChildren.isEmpty()) {
            Log.debug(() -> String.format(
                    "Command '%s' with arguments '%s' not found.",
                    getName(), String.join(ARGUMENTS_DELIMITER, arguments)));
            return CommandResult.failure();
        }
        SimpleCommandLine accepted = acceptedChildren.get(acceptedChildren.size() - 1);
        Validation.notNull(accepted, "Accepted must not be null.");
        Double score = accept.getFirst();
        Log.debug(() -> String.format(
                "(%s) Command '%s' with arguments '%s' is found. %s",
                score, getName(), String.join(ARGUMENTS_DELIMITER, arguments), accepted));
        CommandExecutor executor = accepted.executor;
        if (executor == null) {
            StringJoiner joiner = new StringJoiner(ARGUMENTS_DELIMITER);
            joiner.add(getName());
            int loc = accepted.getDepth() + 1;
            for (int i = 0; i < loc; i++) {
                if (i >= arguments.length) {
                    break;
                }
                joiner.add(arguments[i]);
            }
            return CommandResult.failure(String.format("Invalid Command '%s'.", joiner));
        }
        if (!validate(context.getSender(), accepted.permission, accepted.open)) {
            return CommandResult.failure(String.format(
                    "You don't have permission '%s' to execute this command.", permissionOf(accepted.permission)));
        }
        for (SimpleCommandLine acceptedChild : acceptedChildren) {
            CommandArgument.Converter<?> converter = acceptedChild.getConverter(context);
            if (converter == null) {
                continue;
            }
            String argument1 = arguments[acceptedChild.depth];
            CommandArgument.ConverterResult<?> converted = converter.convert(context, argument1, true);
            if (!converted.isSuccess()) {
                throw new CommandException(String.format("Invalid argument: %s", argument1));
            }
            Object argument = converted.getResult();
            context.put(acceptedChild.getName(), argument);
        }
        int depth = accepted.depth;
        return executor.execute(context, depth, arguments);
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandContext context, @NotNull String... arguments)
            throws CommandException {
        if (!validate(context, arguments)) {
            return Collections.emptyList();
        }
        Pair<Double, List<SimpleCommandLine>> accept = accept(this, context, depth, arguments);
        List<SimpleCommandLine> acceptedChildren = accept.getSecond();
        List<String> completed;
        if (acceptedChildren.isEmpty()) {
            if (arguments.length > 1 || completer == null) {
                return Collections.emptyList();
            }
            completed = completer.complete(context, arguments[0]);
        } else {
            int size = acceptedChildren.size();
            SimpleCommandLine accepted = acceptedChildren.get(size - 1);
            if (accepted == null) {
                return Collections.emptyList();
            }
            CommandArgument.Completer completer;
            int depth;
            if (size == arguments.length) {
                completer = accepted.getCompleter(context);
                depth = accepted.depth;
            } else {
                completer = (context0, argument) -> {
                    Deque<SimpleCommandLine> children = new ArrayDeque<>(accepted.children);
                    if (children.isEmpty()) {
                        return Collections.emptyList();
                    }
                    List<String> ret = new ArrayList<>(children.size());
                    CommandSender sender = context0.getSender();
                    for (SimpleCommandLine child : children) {
                        if (!validate(sender, child.permission, child.isOpen())) {
                            continue;
                        }
                        CommandArgument.Completer childCompleter = child.completer;
                        if (childCompleter != null) {
                            ret.addAll(childCompleter.complete(context0, argument));
                        }
                    }
                    return ret;
                };
                depth = accepted.depth + 1;
            }
            if (completer == null) {
                return Collections.emptyList();
            }
            if (!validate(context.getSender(), accepted.permission, accepted.open)) {
                return Collections.emptyList();
            }
            if (arguments.length - 1 != depth) {
                return Collections.emptyList();
            }
            completed = completer.complete(context, arguments[depth]);
        }
        if (completed.isEmpty()) {
            return completed;
        }
        Set<String> set = new LinkedHashSet<>(completed);
        return new ArrayList<>(set);
    }

    class SimpleCommandHelpTree {

        private final String rootName = SimpleCommandLine.this.getName();

        private static final String TREE_ROOT_INDENT = "        ";
        private static final String TREE_BRANCH_LAST = "§8└── ";
        private static final String TREE_BRANCH_MIDDLE = "§8├── ";
        private static final String TREE_CHILD_INDENT_LAST = "    ";
        private static final String TREE_CHILD_INDENT_MIDDLE = "§8│   ";
        private final Comparator<ArgumentPoint> treeAsciiComparator = Comparator.comparing(ArgumentPoint::getDisplayName);

        private final Map<String, ArgumentPoint> rootArgumentMap = new LinkedHashMap<>();

        private final Lazy<String> lazyHelpMessage = Lazy.of(this::buildMessageTree);

        SimpleCommandHelpTree() {
            for (SimpleCommandLine child : SimpleCommandLine.this.children) {
                mergeNode(rootArgumentMap, child);
            }
        }

        private void mergeNode(@NotNull Map<String, ArgumentPoint> nodeMap, @NotNull SimpleCommandLine commandLine) {
            String key = keyOf(commandLine.getName(), commandLine.isArgument());
            ArgumentPoint point = nodeMap.computeIfAbsent(
                    key, s -> new ArgumentPoint(commandLine.getName(), commandLine.isArgument(), commandLine.getDescription(), commandLine.getExecutor() != null));
            for (SimpleCommandLine child : commandLine.children) {
                mergeNode(point.getChildren(), child);
            }
        }

        private String buildMessageTree() {
            StringBuilder helpBuilder = new StringBuilder("§6Usage: /").append(rootName).append("\n");
            List<ArgumentPoint> rootNodes = new ArrayList<>(rootArgumentMap.values());
            rootNodes.sort(treeAsciiComparator);
            for (int i = 0; i < rootNodes.size(); i++) {
                buildTreeRecursively(rootNodes.get(i), helpBuilder, TREE_ROOT_INDENT, i == rootNodes.size() - 1);
            }
            return helpBuilder.toString();
        }

        private void buildTreeRecursively(
                @NotNull ArgumentPoint node,
                @NotNull StringBuilder helpBuilder,
                @NotNull String prefix,
                boolean isLast) {
            helpBuilder.append(prefix).append(isLast ? TREE_BRANCH_LAST : TREE_BRANCH_MIDDLE);
            ArgumentPoint current = node;
            helpBuilder.append(current.getDisplayName());
            while (current.getChildren().size() == 1 && !current.isTerminal()) {
                current = current.getChildren().values().iterator().next();
                helpBuilder.append(" ").append(current.getDisplayName());
            }
            if (!current.getDescription().isEmpty()) {
                helpBuilder.append("§7").append(" - ").append(current.getDescription()).append("§r");
            }
            helpBuilder.append("\n");
            List<ArgumentPoint> children = new ArrayList<>(current.getChildren().values());
            children.sort(treeAsciiComparator);
            String childPrefix = prefix + (isLast ? TREE_CHILD_INDENT_LAST : TREE_CHILD_INDENT_MIDDLE);
            for (int i = 0; i < children.size(); i++) {
                buildTreeRecursively(children.get(i), helpBuilder, childPrefix, i == children.size() - 1);
            }
        }

        private String keyOf(@NotNull String name, boolean argument) {
            return argument ? "ARG:" + name : "LIT:" + name;
        }

        @Getter
        @RequiredArgsConstructor
        @ToString
        class ArgumentPoint {

            private final String name;

            private final boolean argument;

            private final String description;

            private final boolean terminal;

            private final Map<String, ArgumentPoint> children = new LinkedHashMap<>();

            public String getDisplayName() {
                return argument ? String.format("§b<%s>", name) : "§e" + name;
            }
        }
    }
}
