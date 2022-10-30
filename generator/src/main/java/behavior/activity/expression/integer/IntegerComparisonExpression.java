package behavior.activity.expression.integer;

import behavior.activity.expression.visitor.ExpressionVisitor;
import behavior.activity.variables.BooleanVariable;
import behavior.activity.variables.IntegerVariable;

public class IntegerComparisonExpression extends IntegerExpression {
  private final BooleanVariable assignee;
  private final IntegerComparisonOperator operator;

  public IntegerComparisonExpression(
      IntegerVariable operand1,
      IntegerVariable operand2,
      BooleanVariable assignee,
      IntegerComparisonOperator operator) {
    super(operand1, operand2);
    this.assignee = assignee;
    this.operator = operator;
  }

  @Override
  public BooleanVariable getAssignee() {
    return this.assignee;
  }

  public IntegerComparisonOperator getOperator() {
    return this.operator;
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.handle(this);
  }
}
