package buj.chatplugin.operations;

import buj.chatplugin.ComponentUtils;
import buj.chatplugin.OperationContext;

public class ReplaceOperation implements MessageTemplateOperation {
    private final String tag;
    private final String value;
    private final String replacement;

    public ReplaceOperation(String tag, String value, String replacement) {
        this.tag = tag;
        this.value = value;
        this.replacement = replacement;
    }

    @Override
    public void execute(OperationContext context) {
        if (!context.getTags().containsKey(tag)) return;
        context.getTags().put(tag, context.getTags().get(tag).replaceAll(
                value, ComponentUtils.format(
                        ComponentUtils.unicode(replacement),
                        context.getTags()
                )
        ));
    }
}
