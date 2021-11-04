package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;

public interface MessageTemplateOperation {
    void execute(OperationContext context);
}
