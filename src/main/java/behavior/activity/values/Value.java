package behavior.activity.values;

public interface Value {
    <RETURN> RETURN accept(ValueVisitor<RETURN> visitor);
}
