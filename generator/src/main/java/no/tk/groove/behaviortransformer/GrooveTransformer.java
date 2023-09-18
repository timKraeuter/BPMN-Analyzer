package no.tk.groove.behaviortransformer;

import static no.tk.groove.behaviortransformer.BehaviorToGrooveTransformer.START_GST;

import no.tk.behavior.Behavior;
import no.tk.groove.GxlToXMLConverter;
import no.tk.groove.graph.GrooveGraph;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.groove.graph.rule.GrooveRuleWriter;
import no.tk.groove.gxl.Gxl;

import java.nio.file.Path;
import java.nio.file.Paths;
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

  static void writeStartGraph(Path targetFolder, GrooveGraph startGraph, boolean layoutActivated) {
    Gxl gxl = BehaviorToGrooveTransformer.createGxlFromGrooveGraph(startGraph, layoutActivated);
    Path startGraphFile = Paths.get(targetFolder.toString(), START_GST);

    GxlToXMLConverter.toXml(gxl, startGraphFile);
  }

  GrooveGraph generateStartGraph(S source);

  default void generateAndWriteStartGraph(S source, Path targetFolder) {
    GrooveGraph startGraph = this.generateStartGraph(source);
    writeStartGraph(targetFolder, startGraph, this.isLayoutActivated());
  }

  Stream<GrooveGraphRule> generateRules(S source);

  default void generateAndWriteRules(S source, Path targetFolder) {
    Stream<GrooveGraphRule> rules = this.generateRules(source);
    GrooveRuleWriter.writeRules(rules, targetFolder);
    this.generateAndWriteRulesFurther(source, targetFolder);
  }

  default void generateAndWriteRulesFurther(S source, Path targetFolder) {
    // to be overridden if needed
  }

  boolean isLayoutActivated();
}
