package no.tk.behavior.activity.expression.bool;

import no.tk.behavior.activity.expression.Expression;
import no.tk.behavior.activity.variables.BooleanVariable;

public abstract class BooleanExpression implements Expression {
  private final BooleanVariable assignee;

  protected BooleanExpression(BooleanVariable assignee) {
    this.assignee = assignee;
  }

  public BooleanVariable getAssignee() {
    return this.assignee;
  }
}
