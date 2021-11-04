package buj.chatplugin.operations;

public interface MessageTemplateOperationList extends MessageTemplateOperation {
    MessageTemplateOperationList append(MessageTemplateOperation op);
}
