package behavior.activity.expression;

import behavior.activity.expression.visitor.ExpressionVisitor;
import behavior.activity.values.Value;
import behavior.activity.variables.Variable;

/** Represents an expression for setting values such as: x = 5, containsBugs = false. */
public class SetVariableExpression<V extends Value> implements Expression {
  private final V value;
  private final Variable<V> variableToBeSet;

  public SetVariableExpression(V value, Variable<V> variableToBeSet) {
    this.value = value;
    this.variableToBeSet = variableToBeSet;
  }

  public V getValue() {
    return this.value;
  }

  public Variable<V> getVariableToBeSet() {
    return this.variableToBeSet;
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.handle(this);
  }
}
