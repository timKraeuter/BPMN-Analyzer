package groove.behaviorTransformer;

import behavior.piCalculus.*;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

class PiCalcToGrooveTransformerTest implements BehaviorToGrooveTransformerTestHelper {

    @Test
    void emptySum() throws Exception {
        EmptySum empty = new EmptySum();

        NamedPiProcess namedProcess = new NamedPiProcess("emptySum", empty);
        this.checkGrooveGeneration(namedProcess);
    }

    @Test
    void summation() throws Exception {
        // x?(y).0
        Prefix inPrefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("y"));
        PrefixedProcess in = new PrefixedProcess(inPrefix, new EmptySum());
        // x!(y).0
        Prefix outPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("y"));
        PrefixedProcess out = new PrefixedProcess(outPrefix, new EmptySum());
        MultiarySum sum = new MultiarySum(Sets.newHashSet(in, out));

        NamedPiProcess namedProcess = new NamedPiProcess("sum", sum);
        this.checkGrooveGeneration(namedProcess);
    }

    @Test
    void restriction() throws Exception {

    }

    @Test
    void parallel() throws Exception {

    }

    @Test
    void in() throws Exception {
        // x?(y).0
        Prefix prefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("y"));
        PrefixedProcess in = new PrefixedProcess(prefix, new EmptySum());

        NamedPiProcess namedProcess = new NamedPiProcess("in", in);
        this.checkGrooveGeneration(namedProcess);
    }

    @Test
    void out() throws Exception {
        // x!(y).0
        Prefix prefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("y"));
        PrefixedProcess out = new PrefixedProcess(prefix, new EmptySum());

        NamedPiProcess namedProcess = new NamedPiProcess("out", out);
        this.checkGrooveGeneration(namedProcess);

    }

    @Test
    void fig20() throws Exception {

    }
}