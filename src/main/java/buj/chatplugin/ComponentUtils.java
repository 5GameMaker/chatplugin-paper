package buj.chatplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ComponentUtils {
    private ComponentUtils() {}

    // Not perfect, but should work
    public static String flatten(Component component) {
        if (length(component) == 0) return "";

        StringBuilder flat = new StringBuilder();

        if (component instanceof TextComponent) {
            @Nullable TextColor textColor = component.color();
            if (textColor != null) {
                flat.append('&');
                flat.append(textColor.asHexString());
            }
            if (component.hasStyling()) {
                if (component.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE) {
                    flat.append("&l");
                }
                if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE) {
                    flat.append("&o");
                }
                if (component.decoration(TextDecoration.OBFUSCATED) == TextDecoration.State.TRUE) {
                    flat.append("&k");
                }
                if (component.decoration(TextDecoration.STRIKETHROUGH) == TextDecoration.State.TRUE) {
                    flat.append("&m");
                }
                if (component.decoration(TextDecoration.UNDERLINED) == TextDecoration.State.TRUE) {
                    flat.append("&n");
                }
            }
            flat.append(((TextComponent) component).content());
        }

        component.children().forEach(c -> flat.append(flatten(c)));

        flat.append("&r");

        return flat.toString();
    }

    private static @NotNull Component buildSingleString(String str, Map<String, String> tags) {
        List<String> parts = Arrays.stream(str.split("\\."))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .collect(Collectors.toList());

        if (parts.size() == 0) return Component.empty();
        if (parts.get(0).isEmpty()) return Component.empty();

        @NotNull Component tag = Component.text(tags.getOrDefault(parts.remove(0), ""));

        if (ComponentUtils.length(tag) == 0) return Component.empty();

        for (String part : parts) {
            switch (part) {
                case "colored":
                    tag = ComponentUtils.build(tag, !parts.contains("no_magic"));
                    break;

                case "clear":
                {
                    AtomicReference<Component> tmp = new AtomicReference<>(tag);
                    List<Component> children = new LinkedList<>();
                    if (tag instanceof TextComponent) tmp.set(tag.style(
                            tmp.get().style()
                                    .color(NamedTextColor.WHITE)
                                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                    ));
                    tag.children().forEach(c -> children.add(c.style(
                            c.style()
                                    .color(NamedTextColor.WHITE)
                                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                    )));

                    tag = tmp.get();
                    tag = tag.children(children);
                }

                case "no_format":
                {
                    AtomicReference<Component> tmp = new AtomicReference<>(tag);
                    List<Component> children = new LinkedList<>();
                    if (tag instanceof TextComponent) tmp.set(tag.style(
                            tmp.get().style()
                                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                    ));
                    tag.children().forEach(c -> children.add(c.style(
                            c.style()
                                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                    )));

                    tag = tmp.get();
                    tag = tag.children(children);
                }
                    break;

                case "no_magic":
                {
                    AtomicReference<Component> tmp = new AtomicReference<>(tag);
                    List<Component> children = new LinkedList<>();
                    if (tag instanceof TextComponent) tmp.set(tag.style(
                            tmp.get().style()
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                    ));
                    tag.children().forEach(c -> children.add(c.style(
                            c.style()
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                    )));

                    tag = tmp.get();
                    tag = tag.children(children);
                }
                break;
            }
        }

        return tag;
    }

    public static String format(String str, Map<String, String> tags) {
        if (str.indexOf('$') == -1) return str;

        StringBuilder output = new StringBuilder();

        int i = 0;
        int o = 0;
        while ((i = str.indexOf("$", i)) != -1) {
            output.append(str, o, i);
            i++;
            if (i >= str.length()) break;
            // Another parser
            if (str.charAt(i) == '(') {
                if (str.indexOf(')', i) == -1) break;

                String tag = str.substring(i + 1, str.indexOf(')', i));
                if (tags.containsKey(tag)) output.append(tags.get(tag));

                i = str.indexOf(')', i) + 1;
            }
            else {
                StringBuilder formatStr = new StringBuilder();

                while (i < str.length() && str.substring(i, i + 1).matches("^[a-zA-Z.0-9_-]$")) {
                    formatStr.append(str.charAt(i++));
                }

                if (tags.containsKey(formatStr.toString())) output.append(tags.get(formatStr.toString()));
            }
            o = i;
        }

        output.append(str.substring(o));

        return output.toString();
    }

    public static Component formatComponent(String str, Map<String, String> tags) {
        if (str.indexOf('$') == -1) return Component.text(str);

        List<Component> output = new LinkedList<>();

        int i = 0;
        int o = 0;
        while ((i = str.indexOf("$", i)) != -1) {
            output.add(ComponentUtils.build(str.substring(o, i)));
            i++;
            if (i >= str.length()) break;
            // Another parser
            if (str.charAt(i) == '(') {
                if (str.indexOf(')', i) == -1) break;

                output.add(buildSingleString(str.substring(i + 1, str.indexOf(')', i)), tags));

                i = str.indexOf(')', i) + 1;
            }
            else {
                StringBuilder formatStr = new StringBuilder();

                while (i < str.length() && str.substring(i, i + 1).matches("^[a-zA-Z.0-9_-]$")) {
                    formatStr.append(str.charAt(i++));
                }

                output.add(buildSingleString(formatStr.toString(), tags));
            }
            o = i;
        }

        output.add(ComponentUtils.build(str.substring(o)));

        return Component.join(
                JoinConfiguration
                        .builder()
                        .build(),
                output
        );
    }

    public static int length(Component component) {
        int len = 0;
        if (component instanceof TextComponent) len += ((TextComponent) component).content().length();

        return len + component.children().stream().mapToInt(ComponentUtils::length).sum();
    }

    // Unicode parser
    public static String unicode(String str) {
        final boolean[] first = {true};
        return Arrays.stream(str.split("\\\\u"))
                .map(s -> {
                    if (first[0]) {
                        first[0] = false;
                        return s;
                    }

                    if (s.length() < 4) return '\\' + s;

                    String hexCode = s.substring(0, 4);
                    if (!hexCode.matches("^[0-9a-fA-F]{4}$")) return '\\' + s;
                    int charCode = Integer.parseInt(hexCode, 16);
                    if (charCode < 32) return '\\' + s;
                    return (char)charCode + s.substring(4);
                })
                .collect(Collectors.joining());
    }

    // Not the greatest thing in the world, but as long as it works...
    public static Component build(String str) {
        return build(Component.text(str), true);
    }
    public static Component build(String str, boolean allowMagic) {
        return build(Component.text(str), allowMagic);
    }
    public static Component build(Component component) {
        return build(component, true);
    }
    public static Component build(Component component, boolean allowMagic) {
        if (!(component instanceof TextComponent)) return component;

        String str = unicode(((TextComponent) component).content());

        List<TextComponent> components = new ArrayList<>();

        {
            AtomicReference<Style> style = new AtomicReference<>(Style.empty());
            AtomicBoolean first = new AtomicBoolean(true);
            Arrays.stream(str.split("&"))
                    .forEach(s -> {
                        if (first.get()) {
                            first.set(false);
                            if (s.length() > 0) components.add(
                                    Component.text(s)
                                            .style(style.get())
                            );
                            return;
                        }

                        // First character is always a color code

                        // Unless there's no first character
                        if (s.length() == 0) {
                            components.add(
                                    Component.text("&" + s)
                                            .style(style.get())
                            );
                            return;
                        }

                        char colorChar = s.charAt(0); // Get the color code

                        // There can be no case when there are 2 '&'s in
                        // a singular string
                        switch (colorChar) {
                            case 'a':
                                style.set(style.get().color(NamedTextColor.GREEN));
                                break;
                            case 'b':
                                style.set(style.get().color(NamedTextColor.AQUA));
                                break;
                            case 'c':
                                style.set(style.get().color(NamedTextColor.RED));
                                break;
                            case 'd':
                                style.set(style.get().color(NamedTextColor.LIGHT_PURPLE));
                                break;
                            case 'e':
                                style.set(style.get().color(NamedTextColor.YELLOW));
                                break;
                            case 'f':
                                style.set(style.get().color(NamedTextColor.WHITE));
                                break;
                            case '0':
                                style.set(style.get().color(NamedTextColor.BLACK));
                                break;
                            case '1':
                                style.set(style.get().color(NamedTextColor.DARK_BLUE));
                                break;
                            case '2':
                                style.set(style.get().color(NamedTextColor.DARK_GREEN));
                                break;
                            case '3':
                                style.set(style.get().color(NamedTextColor.DARK_AQUA));
                                break;
                            case '4':
                                style.set(style.get().color(NamedTextColor.DARK_RED));
                                break;
                            case '5':
                                style.set(style.get().color(NamedTextColor.DARK_PURPLE));
                                break;
                            case '6':
                                style.set(style.get().color(NamedTextColor.GOLD));
                                break;
                            case '7':
                                style.set(style.get().color(NamedTextColor.DARK_GRAY));
                                break;
                            case '8':
                                style.set(style.get().color(NamedTextColor.GRAY));
                                break;
                            case '9':
                                style.set(style.get().color(NamedTextColor.BLUE));
                                break;
                            case 'k':
                                if (allowMagic) style.set(style.get().decoration(TextDecoration.OBFUSCATED, TextDecoration.State.TRUE));
                                break;
                            case 'l':
                                style.set(style.get().decoration(TextDecoration.BOLD, TextDecoration.State.TRUE));
                                break;
                            case 'm':
                                style.set(style.get().decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.TRUE));
                                break;
                            case 'n':
                                style.set(style.get().decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE));
                                break;
                            case 'o':
                                style.set(style.get().decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE));
                                break;
                            case 'r':
                                // Reset everything
                                style.set(
                                        style.get().color(NamedTextColor.WHITE)
                                                .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                                .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                                .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                                                .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                );
                                break;
                            case '#':
                                // HEX colors
                                if (s.length() < 7) {
                                    components.add(
                                            Component.text("&" + s)
                                                    .style(style.get())
                                    );
                                    return;
                                }
                                String hexCode = s.substring(1, 7);
                                if (!hexCode.matches("^[0-9a-fA-F]{6}$")) {
                                    components.add(
                                            Component.text("&" + s)
                                                    .style(style.get())
                                    );
                                    return;
                                }
                                try {
                                    int color = Integer.parseInt(hexCode, 16);
                                    style.set(style.get().color(TextColor.color(color)));
                                } catch (Exception ignored) {}
                                components.add(
                                        Component.text(s.substring(7))
                                                .style(style.get())
                                );
                                return;

                            default:
                                components.add(
                                        Component.text("&" + s)
                                                .style(style.get())
                                );
                                return;
                        }

                        components.add(
                                Component.text(s.substring(1))
                                        .style(style.get())
                        );
                    });
        }

        return Component.join(
                JoinConfiguration
                        .builder()
                        .build(),
                components
        );
    }

    private static void doNothing() {}
}
