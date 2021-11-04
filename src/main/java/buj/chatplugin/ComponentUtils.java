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

                case "no_color":
                {
                    AtomicReference<@Nullable Component> tmp = new AtomicReference<>(null);
                    if (tag instanceof TextComponent) tmp.set(tag.color(NamedTextColor.WHITE));
                    tag.children().forEach(c -> tmp.set(c.color(NamedTextColor.WHITE)));

                    if (tmp.get() != null) tag = Objects.requireNonNull(tmp.get());
                }

                case "no_format":
                {
                    AtomicReference<@Nullable Component> tmp = new AtomicReference<>(null);
                    if (tag instanceof TextComponent) tmp.set(tag.style(
                            Style.style()
                                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                                    .build()
                    ));
                    tag.children().forEach(c -> tmp.set(c.style(
                            Style.style()
                                    .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                                    .build()
                    )));

                    if (tmp.get() != null) tag = Objects.requireNonNull(tmp.get());
                }
                    break;

                case "no_magic":
                {
                    AtomicReference<@Nullable Component> tmp = new AtomicReference<>(null);
                    if (tag instanceof TextComponent) tmp.set(tag.style(
                            Style.style()
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .build()
                    ));
                    tag.children().forEach(c -> tmp.set(c.style(
                            Style.style()
                                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                                    .build()
                    )));

                    if (tmp.get() != null) tag = Objects.requireNonNull(tmp.get());
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
        TextColor textColor = NamedTextColor.WHITE;

        boolean bold = false;
        boolean italic = false;
        boolean magic = false;
        boolean stroke = false;
        boolean underline = false;

        // A pretty weird builder
        int index;
        while ((index = str.indexOf("&")) != -1) {
            // First, we must check if this is a last character of a string
            if (index == str.length() - 1) break;

            // Now we can re-color our current component differently depending on the next character

            // Totally not a terrible code

            // We must make sure we aren't putting garbage objects in our array
            if (index > 0) {
                // Push whatever was before
                components.add(
                        Component.text(str.substring(0, index))
                                .color(textColor)
                                .decoration(TextDecoration.BOLD, bold)
                                .decoration(TextDecoration.ITALIC, italic)
                                .decoration(TextDecoration.OBFUSCATED, magic)
                                .decoration(TextDecoration.STRIKETHROUGH, stroke)
                                .decoration(TextDecoration.UNDERLINED, underline)
                );
            }

            char nextChar = str.toLowerCase().charAt(index + 1);

            // Clean things up a bit (drop the color code and whatever before it)
            str = str.substring(index + 2);

            // There's no point in coloring if there's nothing to color
            if (str.length() == 0) break;

            // I haven't found any ways to do this thing better
            switch (nextChar) {
                case 'a':
                    textColor = NamedTextColor.GREEN;
                    break;
                case 'b':
                    textColor = NamedTextColor.AQUA;
                    break;
                case 'c':
                    textColor = NamedTextColor.RED;
                    break;
                case 'd':
                    textColor = NamedTextColor.LIGHT_PURPLE;
                    break;
                case 'e':
                    textColor = NamedTextColor.YELLOW;
                    break;
                case 'f':
                    textColor = NamedTextColor.WHITE;
                    break;
                case '0':
                    textColor = NamedTextColor.BLACK;
                    break;
                case '1':
                    textColor = NamedTextColor.DARK_BLUE;
                    break;
                case '2':
                    textColor = NamedTextColor.DARK_GREEN;
                    break;
                case '3':
                    textColor = NamedTextColor.DARK_AQUA;
                    break;
                case '4':
                    textColor = NamedTextColor.DARK_RED;
                    break;
                case '5':
                    textColor = NamedTextColor.DARK_PURPLE;
                    break;
                case '6':
                    textColor = NamedTextColor.GOLD;
                    break;
                case '7':
                    textColor = NamedTextColor.DARK_GRAY;
                    break;
                case '8':
                    textColor = NamedTextColor.GRAY;
                    break;
                case '9':
                    textColor = NamedTextColor.BLUE;
                    break;
                case 'k':
                    if (allowMagic) magic = true;
                    break;
                case 'l':
                    bold = true;
                    break;
                case 'm':
                    stroke = true;
                    break;
                case 'n':
                    underline = true;
                    break;
                case 'o':
                    italic = true;
                    break;
                case 'r':
                    // Reset everything
                    textColor = NamedTextColor.WHITE;
                    bold = false;
                    italic = false;
                    magic = false;
                    underline = false;
                    stroke = false;
                    break;
                case '#':
                    // HEX colors
                    if (str.length() < 6) break;
                    String hexCode = str.substring(0, 6);
                    str = str.substring(6);
                    if (!hexCode.matches("^[0-9a-fA-F]{6}$")) break;
                    try {
                        int color = Integer.parseInt(hexCode, 16);
                        textColor = TextColor.color(color);
                    } catch (Exception ignored) {}
                    break;
            }
        }

        // If there's some stuff we haven't touched yet, add them in the end
        if (str.length() > 0) {
            components.add(
                    Component.text(str)
                            .color(textColor)
                            .decoration(TextDecoration.BOLD, bold)
                            .decoration(TextDecoration.ITALIC, italic)
                            .decoration(TextDecoration.OBFUSCATED, magic)
                            .decoration(TextDecoration.STRIKETHROUGH, stroke)
                            .decoration(TextDecoration.UNDERLINED, underline)
            );
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
