package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;
import net.kyori.adventure.text.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class IfStatement implements MessageTemplateOperationList {
    private final List<MessageTemplateOperation> ops = new LinkedList<>();

    private boolean expected = false;

    public void reverse() {
        expected = !expected;
    }

    public abstract boolean validate(OperationContext context);

    @Override
    public void execute(OperationContext context) {
        if (validate(context) == expected) return;
        ops.forEach(op -> op.execute(context));
    }

    @Override
    public MessageTemplateOperationList append(MessageTemplateOperation op) {
        ops.add(op);
        return this;
    }
}
