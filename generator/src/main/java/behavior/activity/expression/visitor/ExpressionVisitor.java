package behavior.activity.expression.visitor;

import behavior.activity.expression.SetVariableExpression;
import behavior.activity.expression.bool.BooleanBinaryExpression;
import behavior.activity.expression.bool.BooleanUnaryExpression;
import behavior.activity.expression.integer.IntegerCalculationExpression;
import behavior.activity.expression.integer.IntegerComparisonExpression;
import behavior.activity.values.Value;

public interface ExpressionVisitor {
    <V extends Value> void handle(SetVariableExpression<V> setVariableExpression);

    void handle(IntegerCalculationExpression integerCalculationExpression);

    void handle(IntegerComparisonExpression integerComparisonExpression);

    void handle(BooleanBinaryExpression booleanBinaryExpression);

    void handle(BooleanUnaryExpression booleanUnaryExpression);
}
