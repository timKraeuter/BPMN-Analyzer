package groove.behaviorTransformer;

import behavior.piCalculus.*;
import com.google.common.collect.Lists;
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
        // x!(z).0
        Prefix outPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("z"));
        PrefixedProcess out = new PrefixedProcess(outPrefix, new EmptySum());

        // x?(y).0 + x!(z).0
        MultiarySum sum = new MultiarySum(Lists.newArrayList(in, out));

        NamedPiProcess namedProcess = new NamedPiProcess("sum", sum);
        this.checkGrooveGeneration(namedProcess);
    }

    @Test
    void restriction() throws Exception {

    }

    @Test
    void parallel() throws Exception {
        // x?(y).0
        Prefix inPrefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("y"));
        PrefixedProcess in = new PrefixedProcess(inPrefix, new EmptySum());
        // x!(z).0
        Prefix outPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("z"));
        PrefixedProcess out = new PrefixedProcess(outPrefix, new EmptySum());

        // x?(y).0 | x!(z).0
        Parallelism par = new Parallelism(Lists.newArrayList(in, out));

        NamedPiProcess namedProcess = new NamedPiProcess("par", par);
        this.checkGrooveGeneration(namedProcess);
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
        // z!(w).0
        Prefix wOutPrefix = new Prefix(PrefixType.OUT, "z", Sets.newHashSet("w"));
        PrefixedProcess wOutOnZ = new PrefixedProcess(wOutPrefix, new EmptySum());
        // x?(z).z!(w).0
        Prefix xInPrefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("z"));
        PrefixedProcess zInOnX = new PrefixedProcess(xInPrefix, wOutOnZ);

        // x!(x).0
        Prefix xOutPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("x"));
        PrefixedProcess xOutOnX = new PrefixedProcess(xOutPrefix, new EmptySum());

        // x?(z).z!(w).0 | x!(x).0
        Parallelism par = new Parallelism(Lists.newArrayList(zInOnX, xOutOnX));

        NamedPiProcess namedProcess = new NamedPiProcess("fig20", par);
        this.checkGrooveGeneration(namedProcess);
    }
}