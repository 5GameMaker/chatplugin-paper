package buj.chatplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public interface OperationContext {
    Player getPlayer();
    Server getServer();
    Map<String, String> getTags();
    List<Component> getDisplay();
}
