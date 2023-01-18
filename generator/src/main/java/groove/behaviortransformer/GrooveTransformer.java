package groove.behaviortransformer;

import static groove.behaviortransformer.BehaviorToGrooveTransformer.START_GST;

import behavior.Behavior;
import groove.GxlToXMLConverter;
import groove.graph.GrooveGraph;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleWriter;
import groove.gxl.Gxl;
import java.io.File;
import java.util.stream.Stream;

public interface GrooveTransformer<S extends Behavior> {
  // Special groove labels
  String AT = "@";
  String FORALL = "forall:";
  String EXISTS_OPTIONAL = "existsx:";
  String EXISTS = "exists:";
  // Nesting of quantifiers
  String IN = "in";

  String BOOL = "bool:";
  String FALSE = BOOL + "false";
  String TRUE = BOOL + "true";
  String BOOL_NOT = "bool:not";
  String BOOL_AND = "bool:and";
  String BOOL_OR = "bool:or";
  String UNEQUALS = "!=";
  String INT = "int:";
  String ARG_0 = "arg:0";
  String ARG_1 = "arg:1";
  String INT_ADD = "int:add";
  String INT_SUB = "int:sub";
  String PROD = "prod:";
  String INT_LT = "int:lt";
  String INT_LE = "int:le";
  String INT_EQ = "int:eq";
  String INT_GE = "int:ge";
  String INT_GT = "int:gt";

  String TYPE = "type:";
  String STRING = "string:";

  static void writeStartGraph(File targetFolder, GrooveGraph startGraph, boolean layoutActivated) {
    Gxl gxl = BehaviorToGrooveTransformer.createGxlFromGrooveGraph(startGraph, layoutActivated);
    File startGraphFile = new File(targetFolder.getPath() + START_GST);

    GxlToXMLConverter.toXml(gxl, startGraphFile);
  }

  GrooveGraph generateStartGraph(S source);

  default void generateAndWriteStartGraph(S source, File targetFolder) {
    GrooveGraph startGraph = this.generateStartGraph(source);
    writeStartGraph(targetFolder, startGraph, this.isLayoutActivated());
  }

  Stream<GrooveGraphRule> generateRules(S source);

  default void generateAndWriteRules(S source, File targetFolder) {
    Stream<GrooveGraphRule> rules = this.generateRules(source);
    GrooveRuleWriter.writeRules(rules, targetFolder);
    this.generateAndWriteRulesFurther(source, targetFolder);
  }

  default void generateAndWriteRulesFurther(S source, File targetFolder) {
    // to be overridden if needed
  }

  boolean isLayoutActivated();
}
