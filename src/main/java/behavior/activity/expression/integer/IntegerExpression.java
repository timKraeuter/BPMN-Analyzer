package behavior.activity.expression.integer;

import behavior.activity.expression.Expression;
import behavior.activity.variables.IntegerVariable;

public abstract class IntegerExpression implements Expression {
    private final IntegerVariable operand1;
    private final IntegerVariable operand2;

    protected IntegerExpression(IntegerVariable operand1, IntegerVariable operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public IntegerVariable getOperand1() {
        return this.operand1;
    }

    public IntegerVariable getOperand2() {
        return this.operand2;
    }
}
