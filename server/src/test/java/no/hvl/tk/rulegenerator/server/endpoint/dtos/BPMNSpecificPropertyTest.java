package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

class BPMNSpecificPropertyTest {

  @Test
  void orderingTest() {
    List<BPMNSpecificProperty> properties =
        Lists.newArrayList(
            BPMNSpecificProperty.SAFENESS,
            BPMNSpecificProperty.NO_DEAD_ACTIVITIES,
            BPMNSpecificProperty.OPTION_TO_COMPLETE);

    // shuffle
    Collections.shuffle(properties);

    // sort
    properties.sort(Enum::compareTo);
    assertThat(
        properties,
        is(
            Lists.newArrayList(
                BPMNSpecificProperty.SAFENESS,
                BPMNSpecificProperty.OPTION_TO_COMPLETE,
                BPMNSpecificProperty.NO_DEAD_ACTIVITIES)));
  }
}
