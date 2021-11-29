package behavior.piCalculus;

public interface PiProcess {
    <T> T accept(PiProcessVisitor<T> visitor);

    boolean isEmptySum();
}
