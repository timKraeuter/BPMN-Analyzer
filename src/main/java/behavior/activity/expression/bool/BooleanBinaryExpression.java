package behavior.activity.expression.bool;

import behavior.activity.variables.BooleanVariable;

public class BooleanBinaryExpression extends BooleanExpression {
    private final BooleanVariable operand1;
    private final BooleanVariable operand2;

    private final BooleanBinaryOperator operator;

    public BooleanBinaryExpression(
            BooleanVariable operand1,
            BooleanVariable operand2,
            BooleanBinaryOperator operator,
            BooleanVariable assignee) {
        super(assignee);
        this.operator = operator;
        assert !assignee.getName().equals(operand1.getName());
        assert !assignee.getName().equals(operand2.getName());
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public BooleanVariable getOperand1() {
        return this.operand1;
    }

    public BooleanVariable getOperand2() {
        return this.operand2;
    }
}
