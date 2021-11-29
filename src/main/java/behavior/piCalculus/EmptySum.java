package behavior.piCalculus;

public class EmptySum extends Sum {
    @Override
    public void accept(PiProcessVisitor visitor) {
        visitor.handle(this);
    }
}
