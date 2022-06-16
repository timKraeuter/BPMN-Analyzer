package behavior.activity.expression.bool;

import behavior.activity.expression.BinaryExpression;
import behavior.activity.expression.visitor.ExpressionVisitor;
import behavior.activity.variables.BooleanVariable;

public class BooleanBinaryExpression extends BooleanExpression implements BinaryExpression {
    private final BooleanVariable operand1;
    private final BooleanVariable operand2;

    private final BooleanBinaryOperator operator;

    public BooleanBinaryExpression(
            BooleanVariable operand1,
            BooleanVariable operand2,
            BooleanBinaryOperator operator,
            BooleanVariable assignee) {
        super(assignee);
        if (assignee.getName().equals(operand1.getName()) || assignee.getName().equals(operand2.getName())) {
            throw new IllegalArgumentException("Assignee is not allowed to be equal to operand1 or operand2!");
        }
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public BooleanBinaryOperator getOperator() {
        return this.operator;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public String getNameOfOperand1() {
        return this.operand1.getName();
    }

    @Override
    public String getNameOfOperand2() {
        return this.operand2.getName();
    }

    @Override
    public String getNameOfAssignee() {
        return this.getAssignee().getName();
    }
}
