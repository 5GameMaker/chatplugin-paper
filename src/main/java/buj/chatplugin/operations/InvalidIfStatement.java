package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;

// Will never execute
public class InvalidIfStatement extends IfStatement {
    @Override
    public boolean validate(OperationContext context) {
        return false;
    }

    @Override
    public void reverse() {}
}
