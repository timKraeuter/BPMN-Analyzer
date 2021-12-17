package behavior.activity.values;

public interface ValueVisitor<RETURN> {
    RETURN handle(IntegerValue integerValue);

    RETURN handle(BooleanValue booleanValue);
}
