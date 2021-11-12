package behavior.piCalculus;

import java.util.Set;

public class MultiarySum extends Sum {
    private final Set<Sum> sums;

    public MultiarySum(Set<Sum> sums) {
        this.sums = sums;
    }
}
