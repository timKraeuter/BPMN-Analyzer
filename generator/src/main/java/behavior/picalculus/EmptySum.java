package behavior.picalculus;

public class EmptySum implements Sum {
    @Override
    public <T> T accept(PiProcessVisitor<T> visitor) {
        return visitor.handle(this);
    }

    @Override
    public boolean isEmptySum() {
        return true;
    }
}
