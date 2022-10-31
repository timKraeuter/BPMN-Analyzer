package groove.behaviortransformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import behavior.picalculus.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import util.FileTestHelper;

class PiCalcToGrooveTransformerTest extends BehaviorToGrooveTransformerTestHelper {

  public static final String SUB_DIR = "piCalc";

  @Override
  protected void setUpFurther() {
    this.setFileNameFilter(s -> s.endsWith(".gty") || s.endsWith(".gpr"));
  }

  @Override
  public String getTestResourcePathSubFolderName() {
    return SUB_DIR;
  }

  @Override
  public String getOutputPathSubFolderName() {
    return getTestResourcePathSubFolderName();
  }

  @Test
  void checkCopyOfRulesAndTypeGraph() throws Exception {
    EmptySum empty = new EmptySum();

    NamedPiProcess namedProcess = new NamedPiProcess("emptySum", empty);
    this.checkGrooveGeneration(namedProcess);

    File outputDir =
        new File(
            new File(this.getOutputPathIncludingSubFolder())
                + "/"
                + namedProcess.getName()
                + ".gps/");
    File ruleAndTypeGraphDir = new File(this.getClass().getResource("/GaducciPi").getFile());

    FileTestHelper.testDirEquals(
        ruleAndTypeGraphDir,
        outputDir,
        s -> s.equals("start.gst") || s.equals("system.properties"));
  }

  @Test
  void checkTypeAndDanglingInPropertiesFile() throws Exception {
    EmptySum empty = new EmptySum();

    NamedPiProcess namedProcess = new NamedPiProcess("emptySum", empty);
    this.checkGrooveGeneration(namedProcess);

    File outputDir = new File(this.getOutputPathIncludingSubFolder());
    File propertiesFile =
        new File(outputDir + "/" + namedProcess.getName() + ".gps/system.properties");
    final String propertiesContent =
        FileUtils.readFileToString(propertiesFile, StandardCharsets.UTF_8)
            .replaceAll("\r?\n", "\r\n");

    assertThat(
        propertiesContent.contains("typeGraph=Type\r\n" + "checkDangling=true\r\n"), is(true));
  }

  @Test
  void summation() throws Exception {
    // x?(y).0
    Prefix inPrefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("y"));
    PrefixedProcess in = new PrefixedProcess(inPrefix, new EmptySum());
    // x!(z).0
    Prefix outPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("z"));
    PrefixedProcess out = new PrefixedProcess(outPrefix, new EmptySum());

    // x?(y).0 + x!(z).0
    MultiarySum sum = new MultiarySum(Lists.newArrayList(in, out));

    NamedPiProcess namedProcess = new NamedPiProcess("sum", sum);
    this.checkGrooveGeneration(namedProcess);
  }

  @Test
  void restriction() throws Exception {
    // new x(0)
    final NameRestriction xRestriction = new NameRestriction("x", new EmptySum());

    NamedPiProcess namedProcess = new NamedPiProcess("res", xRestriction);
    this.checkGrooveGeneration(namedProcess);
  }

  @Test
  void parallel() throws Exception {
    // x?(y).0
    Prefix inPrefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("y"));
    PrefixedProcess in = new PrefixedProcess(inPrefix, new EmptySum());
    // x!(z).0
    Prefix outPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("z"));
    PrefixedProcess out = new PrefixedProcess(outPrefix, new EmptySum());

    // x?(y).0 | x!(z).0
    Parallelism par = new Parallelism(Lists.newArrayList(in, out));

    NamedPiProcess namedProcess = new NamedPiProcess("par", par);
    this.checkGrooveGeneration(namedProcess);
  }

  @Test
  void in() throws Exception {
    // x?(y).0
    Prefix prefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("y"));
    PrefixedProcess in = new PrefixedProcess(prefix, new EmptySum());

    NamedPiProcess namedProcess = new NamedPiProcess("in", in);
    this.checkGrooveGeneration(namedProcess);
  }

  @Test
  void out() throws Exception {
    // x!(y).0
    Prefix prefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("y"));
    PrefixedProcess out = new PrefixedProcess(prefix, new EmptySum());

    NamedPiProcess namedProcess = new NamedPiProcess("out", out);
    this.checkGrooveGeneration(namedProcess);
  }

  @Test
  void fig20() throws Exception {
    // z!(w).0
    Prefix wOutPrefix = new Prefix(PrefixType.OUT, "z", Sets.newHashSet("w"));
    PrefixedProcess wOutOnZ = new PrefixedProcess(wOutPrefix, new EmptySum());
    // x?(z).z!(w).0
    Prefix xInPrefix = new Prefix(PrefixType.IN, "x", Sets.newHashSet("z"));
    PrefixedProcess zInOnX = new PrefixedProcess(xInPrefix, wOutOnZ);

    // x!(x).0
    Prefix xOutPrefix = new Prefix(PrefixType.OUT, "x", Sets.newHashSet("x"));
    PrefixedProcess xOutOnX = new PrefixedProcess(xOutPrefix, new EmptySum());

    // x?(z).z!(w).0 | x!(x).0
    Parallelism par = new Parallelism(Lists.newArrayList(zInOnX, xOutOnX));

    NamedPiProcess namedProcess = new NamedPiProcess("fig20", par);
    this.checkGrooveGeneration(namedProcess);
  }
}
