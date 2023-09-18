package no.tk.behavior.activity.values;

public interface Value {
  <R> R accept(ValueVisitor<R> visitor);
}
