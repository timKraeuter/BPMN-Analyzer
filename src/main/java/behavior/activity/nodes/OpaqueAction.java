package behavior.activity.nodes;

import behavior.activity.expression.Expression;

import java.util.List;
import java.util.stream.Stream;

/**
 * Executable Activity Node consisting of one opaque action.
 */
public class OpaqueAction extends ActivityNode {
    private final List<Expression> expressions;

    public OpaqueAction(String name, List<Expression> expressions) {
        super(name);
        this.expressions = expressions;
    }

    @Override
    public void accept(ActivityNodeVisitor visitor) {
        visitor.handle(this);
    }

    public Stream<Expression> expressions() {
        return this.expressions.stream();
    }
}
