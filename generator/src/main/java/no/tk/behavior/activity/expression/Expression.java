package no.tk.behavior.activity.expression;

import no.tk.behavior.activity.expression.visitor.ExpressionVisitor;

public interface Expression {
  void accept(ExpressionVisitor visitor);
}
