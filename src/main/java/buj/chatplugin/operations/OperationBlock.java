package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;

import java.util.LinkedList;
import java.util.List;

public class OperationBlock implements MessageTemplateOperationList {
    private final List<MessageTemplateOperation> ops = new LinkedList<>();

    @Override
    public void execute(OperationContext context) {
        ops.forEach(op -> op.execute(context));
    }

    @Override
    public MessageTemplateOperationList append(MessageTemplateOperation op) {
        ops.add(op);
        return this;
    }
}
