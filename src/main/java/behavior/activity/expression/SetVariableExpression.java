package behavior.activity.expression;

import behavior.activity.values.Value;
import behavior.activity.variables.Variable;

/**
 * Represents an expression for setting values such as:
 * x = 5,
 * containsBugs = false.
 */
public class SetVariableExpression<VALUE extends Value> implements Expression {
    private final VALUE value;
    private final Variable<VALUE> variableToBeSet;

    public SetVariableExpression(VALUE value, Variable<VALUE> variableToBeSet) {
        this.value = value;
        this.variableToBeSet = variableToBeSet;
    }

    public VALUE getValue() {
        return this.value;
    }

    public Variable<VALUE> getVariableToBeSet() {
        return this.variableToBeSet;
    }
}
