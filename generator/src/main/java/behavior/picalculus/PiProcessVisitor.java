package behavior.picalculus;

public interface PiProcessVisitor<RETURN> {
    RETURN handle(Parallelism parallelism);

    RETURN handle(NameRestriction restriction);

    RETURN handle(PrefixedProcess prefixedProcess);

    RETURN handle(EmptySum emptySum);

    RETURN handle(MultiarySum multiarySum);
}
