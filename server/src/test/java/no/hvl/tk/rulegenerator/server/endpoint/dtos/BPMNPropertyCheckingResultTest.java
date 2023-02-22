package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

class BPMNPropertyCheckingResultTest {

  @Test
  void orderingTest() {
    List<BPMNPropertyCheckingResult> properties =
        Lists.newArrayList(
            createResult(BPMNSpecificProperty.SAFENESS),
            createResult(BPMNSpecificProperty.NO_DEAD_ACTIVITIES),
            createResult(BPMNSpecificProperty.OPTION_TO_COMPLETE));

    // shuffle
    Collections.shuffle(properties);

    // sort
    properties.sort(BPMNPropertyCheckingResult::compareTo);
    assertThat(
        properties,
        is(
            Lists.newArrayList(
                createResult(BPMNSpecificProperty.SAFENESS),
                createResult(BPMNSpecificProperty.OPTION_TO_COMPLETE),
                createResult(BPMNSpecificProperty.NO_DEAD_ACTIVITIES))));
  }

  private BPMNPropertyCheckingResult createResult(BPMNSpecificProperty name) {
    return new BPMNPropertyCheckingResult(name, false, "");
  }

  @Test
  void equalsAndHashcode() {
    assertThat(
        new BPMNPropertyCheckingResult(BPMNSpecificProperty.SAFENESS, false, "123"),
        is(new BPMNPropertyCheckingResult(BPMNSpecificProperty.SAFENESS, false, "123")));
    assertThat(
        new BPMNPropertyCheckingResult(BPMNSpecificProperty.SAFENESS, false, "123").hashCode(),
        is(new BPMNPropertyCheckingResult(BPMNSpecificProperty.SAFENESS, false, "123").hashCode()));
  }
}
