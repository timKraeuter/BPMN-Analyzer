package behavior.piCalculus;

public interface PiProcessVisitor {
    void handle(Parallelism parallelism);

    void handle(NameRestriction restriction);

    void handle(PrefixedProcess prefixedProcess);

    void handle(EmptySum emptySum);

    void handle(MultiarySum multiarySum);
}
