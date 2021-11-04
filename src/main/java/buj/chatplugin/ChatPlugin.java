package buj.chatplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class ChatPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(this, this);

        {
            PluginCommand command = getCommand("chatplugin");
            if (command != null) {
                command.setExecutor(new ChatPluginCommand(this));
            }
            else getLogger().warning("Unable to attach command listener");
        }
    }

    public ChatPluginConfig config = new ChatPluginConfig(this);

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private Map<String, String> getTags() {
        return new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);

        if (!event.getPlayer().hasPermission("chatplugin.send")) return;

        Component message = this.config.getMessageTemplate().build(ComponentUtils.flatten(event.message()), event.getPlayer(), getTags());

        int len = ComponentUtils.length(message);
        if (len > 0) event.viewers().forEach(viewer -> viewer.sendMessage(message));
    }
}
