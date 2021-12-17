package behavior.activity.expression.bool;

import behavior.activity.expression.visitor.ExpressionVisitor;
import behavior.activity.variables.BooleanVariable;

public class BooleanUnaryExpression extends BooleanExpression {
    private final BooleanVariable operand;
    private final BooleanUnaryOperator operator;

    public BooleanUnaryExpression(BooleanVariable operand, BooleanVariable assignee, BooleanUnaryOperator operator) {
        super(assignee);
        assert !assignee.getName().equals(operand.getName());
        this.operator = operator;
        this.operand = operand;
    }

    public BooleanVariable getOperand() {
        return this.operand;
    }

    public BooleanUnaryOperator getOperator() {
        return this.operator;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.handle(this);
    }
}
