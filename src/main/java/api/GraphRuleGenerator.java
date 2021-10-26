package api;

public interface GraphRuleGenerator {

    /**
     * Define that the current rule adds the given node.
     */
    public void addNode(Node node);

    /**
     * Define that the current rule adds a node with the given name.
     */
    public void addNode(String nodeName);

    /**
     * Define that the current rule deletes the given node.
     */
    public void deleteNode(Node node);

    /**
     * Define that the current rule deletes a node with the given name.
     */
    public void deleteNode(String nodeName);

    /**
     * Define that the current rule adds the given edge.
     */
    public void addEdge(Edge edge);

    /**
     * Define that the current rule adds an edge between the two given nodes.
     */
    public void addEdge(Node source, Node target);

    /**
     * Define that the current rule deletes the given edge.
     */
    public void deleteEdge(Edge edge);
}
