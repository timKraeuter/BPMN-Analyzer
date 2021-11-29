package groove.behaviorTransformer;

import behavior.piCalculus.EmptySum;
import behavior.piCalculus.NamedPiProcess;
import org.junit.jupiter.api.Test;

class PiCalcToGrooveTransformerTest implements BehaviorToGrooveTransformerTestHelper {

    @Test
    void emptySum() throws Exception {
        EmptySum empty = new EmptySum();

        NamedPiProcess namedProcess = new NamedPiProcess("EmptySum", empty);
        this.checkGrooveGeneration(namedProcess);
    }

    @Test
    void summation() throws Exception {

    }

    @Test
    void restriction() throws Exception {

    }

    @Test
    void parallel() throws Exception {

    }

    @Test
    void in() throws Exception {

    }

    @Test
    void out() throws Exception {

    }

    @Test
    void fig20() throws Exception {

    }
}