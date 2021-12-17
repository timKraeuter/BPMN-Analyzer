package behavior.activity.expression;

import behavior.activity.expression.visitor.ExpressionVisitor;

public interface Expression {
    void accept(ExpressionVisitor visitor);
}
