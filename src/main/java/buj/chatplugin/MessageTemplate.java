package buj.chatplugin;

import buj.chatplugin.operations.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MessageTemplate {
    public MessageTemplate() {}

    private final MessageTemplateOperationList operations = new OperationBlock();

    public static @NotNull MessageTemplate from(@NotNull String script) {
        MessageTemplate template = new MessageTemplate();

        /*
         Behold, the world's worst compiler in the universe.
         Since this language is pretty simple, we can just
         slap lexer and parser together, and hope for the best.
        */

        final MessageTemplateOperationList[] operations = {template.operations};
        final List<MessageTemplateOperationList> lists = new LinkedList<>();
        lists.add(operations[0]);

        Arrays.stream(script.split("\n"))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .map(a -> a.replaceAll(" +", " "))
                .forEach(s -> {
                    String op = s.split(" ")[0];

                    switch (op) {
                        case "display":
                            operations[0].append(new DisplayOperation(s.substring(op.length()).trim()));
                            break;

                        case "end":
                            if (lists.size() == 1) return;
                            lists.remove(lists.size() - 1);
                            operations[0] = lists.get(lists.size() - 1);
                            break;

                            /*
                             Only terrible solutions allowed.
                             By the way, our if statement does not support
                             complex logic, so you kinda have to stack them
                             on top of each other. At least now you can put
                             them inside each other and not get confused by
                             how their weird behaviour
                            */
                        case "if":
                        case "unless":
                        {
                            IfStatement statement = new InvalidIfStatement();
                            if (s.split(" ").length >= 2) {
                                String primary = s.split(" ")[1];

                                switch (primary) {
                                    case "defined":
                                    {
                                        if (s.split(" ").length < 3) break;
                                        List<String> vars = Arrays.stream(s.split(" "))
                                                .skip(2)
                                                .collect(Collectors.toList());

                                        statement = new CheckIfDefined(vars);
                                    }
                                    break;

                                    case "permitted":
                                    {
                                        if (s.split(" ").length < 3) break;
                                        List<String> permissions = Arrays.stream(s.split(" "))
                                                .skip(2)
                                                .collect(Collectors.toList());

                                        statement = new CheckIfPermitted(permissions);
                                    }
                                    break;

                                    case "equal":
                                    {
                                        if (s.split(" ").length < 4) break;

                                        String tag = s.split(" ")[2];
                                        String equality = Arrays.stream(s.split(" "))
                                                .skip(3)
                                                .collect(Collectors.joining(" "));

                                        statement = new CheckIfEquals(tag, equality);
                                    }
                                    break;
                                }
                            }

                            if (op.equals("unless")) statement.reverse();

                            // Now we need to go inside that if statement
                            operations[0].append(statement);
                            operations[0] = statement;
                            lists.add(statement);
                        }
                            break;

                        case "set":
                        {
                            if (s.split(" ").length < 3) break;
                            String tag = s.split(" ")[1];
                            String value = Arrays.stream(s.split(" "))
                                    .skip(2)
                                    .collect(Collectors.joining(" "));

                            operations[0].append(new SetOperation(tag, value));
                        }
                            break;

                        case "replace":
                        {
                            if (s.split(" ").length < 4) break;
                            String str = s.split(" ")[1];
                            String value = s.split(" ")[2];
                            String replacement = Arrays.stream(s.split(" "))
                                    .skip(3)
                                    .collect(Collectors.joining(" "));

                            operations[0].append(new ReplaceOperation(str, value, replacement));
                        }
                        break;

                    }
                });

        return template;
    }

    public @NotNull Component build(
            @NotNull String text,
            @NotNull Player player,
            @NotNull Map<String, String> tags
    ) {
        Map<String, String> allTags = new HashMap<>(tags);
        LinkedList<Component> display = new LinkedList<>();
        OperationContext context = new OperationContext() {
            @Override
            public Player getPlayer() {
                return player;
            }

            @Override
            public Server getServer() {
                return player.getServer();
            }

            @Override
            public Map<String, String> getTags() {
                return allTags;
            }

            @Override
            public List<Component> getDisplay() {
                return display;
            }
        };

        Location location = player.getLocation();

        allTags.put("username", player.getName());
        allTags.put("name", ComponentUtils.flatten(player.teamDisplayName()));
        allTags.put("x", String.valueOf(location.getX()));
        allTags.put("y", String.valueOf(location.getY()));
        allTags.put("z", String.valueOf(location.getX()));
        allTags.put("bx", String.valueOf(location.getBlockX()));
        allTags.put("by", String.valueOf(location.getBlockY()));
        allTags.put("bz", String.valueOf(location.getBlockZ()));
        allTags.put("world", location.getWorld().getName());
        allTags.put("content", text);

        operations.execute(context);

        AtomicInteger i = new AtomicInteger();

        return Component.join(
                JoinConfiguration.builder().build(),
                display.stream().map(c -> {
                    if (i.get() + 1 == display.size()) return c;
                    i.getAndIncrement();
                    return Component.join(
                            JoinConfiguration.builder().build(),
                            c,
                            Component.newline()
                    );
                }).collect(Collectors.toList())
        );
    }
}
