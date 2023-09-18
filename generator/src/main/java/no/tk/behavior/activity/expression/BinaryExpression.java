package no.tk.behavior.activity.expression;

public interface BinaryExpression extends Expression {
  String getNameOfOperand1();

  String getNameOfOperand2();

  String getNameOfAssignee();
}
