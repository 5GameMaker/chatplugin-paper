package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;

import java.util.List;

public class CheckIfPermitted extends IfStatement {
    private final List<String> permissions;

    public CheckIfPermitted(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean validate(OperationContext context) {
        return permissions.stream().allMatch(context.getPlayer()::hasPermission);
    }
}
