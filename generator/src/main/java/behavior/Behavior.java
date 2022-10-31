package behavior;

public interface Behavior {
  String getName();

  void accept(BehaviorVisitor visitor);
}
