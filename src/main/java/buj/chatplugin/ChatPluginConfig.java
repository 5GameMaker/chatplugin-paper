package buj.chatplugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ChatPluginConfig {
    public ChatPluginConfig(ChatPlugin plugin) {
        this.plugin = plugin;

        try {
            reload();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private final ChatPlugin plugin;

    public void reload() throws IOException, NullPointerException {
        boolean ignored = false;
        File dataDir = this.plugin.getDataFolder();

        if (!dataDir.exists()) {
            ignored = dataDir.mkdirs();
        }

        File messageFile = new File(dataDir, "message");

        if (!messageFile.exists()) {
            FileWriter writer = new FileWriter(messageFile);

            writer.write("display <$name.colored> $content.colored.no_magic\n");

            writer.flush();
            writer.close();

            template = MessageTemplate.from("display <$name.colored> $content.colored.no_magic");
        }
        else {
            FileReader reader = new FileReader(messageFile);
            StringBuilder builder = new StringBuilder();

            int ch;
            while ((ch = reader.read()) != -1) {
                builder.append((char)ch);
            }

            reader.close();

            template = MessageTemplate.from(builder.toString());
        }

        // Ignore output from these functions
        if (ignored) doNothing();
    }

    // Very useful
    private static void doNothing() {}



    private MessageTemplate template;
    public MessageTemplate getMessageTemplate() {
        return template;
    }
}
