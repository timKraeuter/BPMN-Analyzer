package no.tk.groove.behaviortransformer;

import io.github.timkraeuter.groove.GrooveGTSBuilder;
import io.github.timkraeuter.groove.graph.GrooveGraphBuilder;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.nio.file.Path;
import no.tk.behavior.Behavior;

public abstract class GrooveTransformer<S extends Behavior> {
  // Special groove labels
  public static final String AT = "@";
  public static final String FORALL = "forall:";
  public static final String EXISTS_OPTIONAL = "existsx:";
  public static final String EXISTS = "exists:";
  // Nesting of quantifiers
  public static final String IN = "in";
  public static final String BOOL = "bool:";
  public static final String FALSE = BOOL + "false";
  public static final String TRUE = BOOL + "true";
  public static final String BOOL_NOT = "bool:not";
  public static final String BOOL_AND = "bool:and";
  public static final String BOOL_OR = "bool:or";
  public static final String UNEQUALS = "!=";
  public static final String INT = "int:";
  public static final String ARG_0 = "arg:0";
  public static final String ARG_1 = "arg:1";
  public static final String INT_ADD = "int:add";
  public static final String INT_SUB = "int:sub";
  public static final String PROD = "prod:";
  public static final String INT_LT = "int:lt";
  public static final String INT_LE = "int:le";
  public static final String INT_EQ = "int:eq";
  public static final String INT_GE = "int:ge";
  public static final String INT_GT = "int:gt";

  public static final String TYPE = "type:";
  public static final String STRING = "string:";

  protected final GrooveGTSBuilder builder;

  protected GrooveTransformer(boolean layout) {
    this.builder = new GrooveGTSBuilder().layout(layout);
  }

  public Path buildAndWriteGTS(S source, Path targetFolder) {
    this.generateStartGraph(source, this.builder.startGraph());
    this.generateRules(source, this.builder.rules());

    Path path = this.builder.writeGTS(targetFolder);
    this.generateAndWriteRulesFurther(source, path);

    return path;
  }

  public abstract void generateStartGraph(S source, GrooveGraphBuilder builder);

  public abstract void generateRules(S source, GrooveRuleBuilder rules);

  public void generateAndWriteRulesFurther(S source, Path targetFolder) {
    // to be overridden if needed
    // TODO: fix
  }
}
