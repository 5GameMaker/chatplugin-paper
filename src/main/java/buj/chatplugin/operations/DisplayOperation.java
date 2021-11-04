package buj.chatplugin.operations;

import buj.chatplugin.ComponentUtils;
import buj.chatplugin.OperationContext;

public class DisplayOperation implements MessageTemplateOperation {
    public DisplayOperation(String displayString) {
        this.displayString = displayString;
    }

    private final String displayString;

    @Override
    public void execute(OperationContext context) {
        context.getDisplay().add(ComponentUtils.formatComponent(displayString, context.getTags()));
    }
}
