package no.tk.rulegenerator.server.util;

/**
 * A generic immutable pair of two values, replacing {@code org.apache.commons.lang3.tuple.Pair}.
 *
 * @param left the left element
 * @param right the right element
 */
public record Pair<L, R>(L left, R right) {

  /** Creates a new pair. */
  public static <L, R> Pair<L, R> of(L left, R right) {
    return new Pair<>(left, right);
  }
}
