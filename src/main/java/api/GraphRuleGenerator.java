package api;

public interface GraphRuleGenerator {

    /**
     * Define that the current rule adds the given node.
     */
    void addNode(Node node);

    /**
     * Define that the current rule adds a node with the given name.
     */
    Node addNode(String nodeName);

    /**
     * Define that the current rule deletes the given node.
     */
    void deleteNode(Node node);

    /**
     * Define that the current rule deletes a node with the given name.
     */
    void deleteNode(String nodeName);

    /**
     * Define that the current rule adds the given edge.
     */
    void addEdge(Edge edge);

    /**
     * Define that the current rule adds an edge between the two given nodes.
     */
    void addEdge(String name, Node source, Node target);


    /**
     * Define that the current rule deletes the given edge.
     */
    void deleteEdge(Edge edge);

    void generateRule();

    void newRule(String ruleName);
}
