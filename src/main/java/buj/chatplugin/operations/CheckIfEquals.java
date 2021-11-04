package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;
import org.jetbrains.annotations.NotNull;

public class CheckIfEquals extends IfStatement {
    private final String tag;
    private final String equality;

    public CheckIfEquals(String tag, String equality) {
        this.tag = tag;
        this.equality = equality;
    }

    @Override
    public boolean validate(@NotNull OperationContext context) {
        return context.getTags().containsKey(tag)
                && context.getTags().get(tag).equals(equality);
    }
}
