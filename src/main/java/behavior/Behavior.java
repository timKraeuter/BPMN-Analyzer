package behavior;

public interface Behavior {
    String getName();

    void handle(BehaviorVisitor visitor);
}
