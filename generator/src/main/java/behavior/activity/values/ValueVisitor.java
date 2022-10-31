package behavior.activity.values;

public interface ValueVisitor<R> {
  R handle(IntegerValue integerValue);

  R handle(BooleanValue booleanValue);
}
