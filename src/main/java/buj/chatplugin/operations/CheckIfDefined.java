package buj.chatplugin.operations;

import buj.chatplugin.OperationContext;

import java.util.List;

public class CheckIfDefined extends IfStatement {
    public CheckIfDefined(List<String> toCheck) {
        super();

        checkList = toCheck;
    }

    private final List<String> checkList;

    @Override
    public boolean validate(OperationContext context) {
        return checkList.stream().allMatch(context.getTags()::containsKey);
    }
}
