package behavior.piCalculus;

import java.util.Iterator;
import java.util.Set;

public class MultiarySum extends Sum {
    private final Set<Sum> sums;

    public MultiarySum(Set<Sum> sums) {
        this.sums = sums;
    }

    @Override
    public <T> T accept(PiProcessVisitor<T> visitor) {
        return visitor.handle(this);
    }

    @Override
    public boolean isEmptySum() {
        return false;
    }

    public Sum getFirst() {
        assert this.sums.size() == 2;
        return this.sums.iterator().next();
    }

    public Sum getSecond() {
        assert this.sums.size() == 2;
        Iterator<Sum> iterator = this.sums.iterator();
        iterator.next(); // Ignore first.
        return iterator.next();
    }
}
