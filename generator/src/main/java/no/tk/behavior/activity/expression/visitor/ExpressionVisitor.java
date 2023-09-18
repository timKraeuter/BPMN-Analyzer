package no.tk.behavior.activity.expression.visitor;

import no.tk.behavior.activity.expression.SetVariableExpression;
import no.tk.behavior.activity.expression.bool.BooleanBinaryExpression;
import no.tk.behavior.activity.expression.bool.BooleanUnaryExpression;
import no.tk.behavior.activity.expression.integer.IntegerCalculationExpression;
import no.tk.behavior.activity.expression.integer.IntegerComparisonExpression;
import no.tk.behavior.activity.values.Value;

public interface ExpressionVisitor {
  <V extends Value> void handle(SetVariableExpression<V> setVariableExpression);

  void handle(IntegerCalculationExpression integerCalculationExpression);

  void handle(IntegerComparisonExpression integerComparisonExpression);

  void handle(BooleanBinaryExpression booleanBinaryExpression);

  void handle(BooleanUnaryExpression booleanUnaryExpression);
}
