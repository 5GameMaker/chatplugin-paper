# ChatPlugin

Simple yet powerful chat plugin

# Config

This plugin is a bit different from others. It has a
singular config file with a script that can be modified
however you want

```
display <$name.colored> $content.colored.no_magic
```

There are permissions such as `chatplugin.color`, but
they've must be handled by the script itself

# Permissions

- `chatplugin.admin`  - Provides every permission for this plugin
- `chatplugin.user`   - Every non-admin permission
- `chatplugin.reload` - Allows usage of `/chatplugin reload`
- `chatplugin.send`   - Allows user to send messages
- `chatplugin.color`  - (Script-only) Allows user to use chat colors
- `chatplugin.format` - (Script-only) Allows user to format their text
- `chatplugin.magic`  - (Script-only) Allows user to obfuscate messages
