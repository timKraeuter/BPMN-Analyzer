package no.tk.behavior;

public interface Behavior {
  String getName();

  void accept(BehaviorVisitor visitor);
}
