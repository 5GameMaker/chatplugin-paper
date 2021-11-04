package buj.chatplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ChatPluginCommand implements CommandExecutor {
    public ChatPluginCommand(ChatPlugin plugin) {
        this.plugin = plugin;
    }

    private final ChatPlugin plugin;

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) return false;

        switch (args[0]) {
            case "reload":
                if (args.length != 1) return false;
                if (
                        !sender.isOp() &&
                                !sender.hasPermission("chatplugin.reload")
                ) {
                    sender.sendMessage(
                            Component.text("You have no permission to execute this command")
                                    .color(NamedTextColor.RED)
                    );
                    return true;
                }

                try {
                    this.plugin.config.reload();
                    sender.sendMessage(
                            Component.text("Reloaded successfully")
                                    .color(NamedTextColor.GREEN)
                    );
                } catch (Exception e) {
                    plugin.getLogger().severe("Something happened while reloading config files");
                    e.printStackTrace();
                    sender.sendMessage(
                            Component.text("Failed to reload config files")
                                    .color(NamedTextColor.RED)
                    );
                }
                return true;

            case "cat":
                sender.sendMessage("meow");
                return true;

            default:
                return false;
        }
    }
}
