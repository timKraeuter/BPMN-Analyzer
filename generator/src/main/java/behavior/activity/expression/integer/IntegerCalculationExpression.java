package behavior.activity.expression.integer;

import behavior.activity.expression.visitor.ExpressionVisitor;
import behavior.activity.variables.IntegerVariable;

public class IntegerCalculationExpression extends IntegerExpression {
  private final IntegerVariable assignee;
  private final IntegerCalculationOperator operator;

  public IntegerCalculationExpression(
      IntegerVariable operand1,
      IntegerVariable operand2,
      IntegerVariable assignee,
      IntegerCalculationOperator operator) {
    super(operand1, operand2);
    this.operator = operator;
    this.assignee = assignee;
  }

  @Override
  public IntegerVariable getAssignee() {
    return this.assignee;
  }

  public IntegerCalculationOperator getOperator() {
    return this.operator;
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.handle(this);
  }
}
