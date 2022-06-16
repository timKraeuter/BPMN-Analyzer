package behavior.picalculus;

public interface PiProcessVisitor<R> {
    R handle(Parallelism parallelism);

    R handle(NameRestriction restriction);

    R handle(PrefixedProcess prefixedProcess);

    R handle(EmptySum emptySum);

    R handle(MultiarySum multiarySum);
}
