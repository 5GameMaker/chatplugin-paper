package buj.chatplugin.operations;

import buj.chatplugin.ComponentUtils;
import buj.chatplugin.OperationContext;

public class SetOperation implements MessageTemplateOperation {
    private final String tag;
    private final String value;

    public SetOperation(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public void execute(OperationContext context) {
        context.getTags().put(tag, ComponentUtils.format(
                ComponentUtils.unicode(value),
                context.getTags()
        ));
    }
}
