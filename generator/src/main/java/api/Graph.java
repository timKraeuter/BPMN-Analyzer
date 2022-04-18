package api;

import java.util.stream.Stream;

/**
 * Represents graphs. Graphs should be immutable. That is why we return streams of nodes and edges.
 */
public interface Graph {

    Stream<? extends Node> nodes();

    Stream<? extends Edge> edges();
}
