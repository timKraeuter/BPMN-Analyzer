package no.tk.behavior.activity.expression.bool;

import no.tk.behavior.activity.expression.visitor.ExpressionVisitor;
import no.tk.behavior.activity.variables.BooleanVariable;

public class BooleanUnaryExpression extends BooleanExpression {
  private final BooleanVariable operand;
  private final BooleanUnaryOperator operator;

  public BooleanUnaryExpression(
      BooleanVariable operand, BooleanVariable assignee, BooleanUnaryOperator operator) {
    super(assignee);
    if (assignee.getName().equals(operand.getName())) {
      throw new IllegalArgumentException("Assignee is not allowed to be equal to operand!");
    }
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
