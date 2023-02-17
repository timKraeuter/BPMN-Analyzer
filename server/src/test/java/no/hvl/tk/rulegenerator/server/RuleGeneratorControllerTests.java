package no.hvl.tk.rulegenerator.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import no.hvl.tk.rulegenerator.server.endpoint.RuleGeneratorController;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificProperty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RuleGeneratorControllerTests {
  public static final String BPMN_FILE = "/ruleGeneratorController/name-with-numbers.bpmn";
  public static final String BPMN_FILE_ERROR = "/ruleGeneratorController/errorModel.bpmn";
  public static final String BPMN_FILE_DEAD = "/ruleGeneratorController/dead.bpmn";
  public static final String LOCALHOST = "http://localhost:%s/%s";
  public static final String PROPERTIES_TO_BE_CHECKED = "propertiesToBeChecked[]";
  public static final String CHECK_BPMN_SPECIFIC_PROPERTIES = "checkBPMNSpecificProperties";
  public static final String CHECK_TEMPORAL_LOGIC = "checkTemporalLogic";
  @LocalServerPort private int port;

  @Autowired private RuleGeneratorController restController;

  @Test
  void testContextLoads() {
    assertNotNull(restController);
  }

  @Test
  void testGenerateGGAndZip() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    assertNotNull(bpmnModelFile);

    CloseableHttpResponse response;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      // Build request
      HttpPost uploadFile = new HttpPost(getFullUrl("generateGGAndZip"));
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addBinaryBody(
          "file",
          new FileInputStream(bpmnModelFile),
          ContentType.APPLICATION_OCTET_STREAM,
          bpmnModelFile.getName());
      HttpEntity multipart = builder.build();
      uploadFile.setEntity(multipart);

      response = httpClient.execute(uploadFile);

      // Check response
      HttpEntity responseEntity = response.getEntity();
      ZipInputStream zis = new ZipInputStream(responseEntity.getContent());
      Set<String> zipEntryNames = this.getZipEntryNames(zis);
      // We only check file names not content since the content contains timestamps etc.
      assertThat(
          zipEntryNames,
          is(
              Sets.newHashSet(
                  "_1.gpr",
                  "_2__345_end.gpr",
                  "_2__345_start.gpr",
                  "_3.gpr",
                  "bpmn_e_model.gty",
                  "AllTerminated.gpr",
                  "start.gst",
                  "system.properties",
                  "Terminate.gpr",
                  "Unsafe.gpr")));
    }
  }

  private String getFullUrl(String generateGGAndZip) {
    return String.format(LOCALHOST, port, generateGGAndZip);
  }

  private Set<String> getZipEntryNames(ZipInputStream zis) throws IOException {
    Set<String> fileNames = new HashSet<>();
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
      fileNames.add(entry.getName());
    }
    return fileNames;
  }

  @Test
  void testCheckBPMNSpecificPropertiesNoDeadActivities() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties = convertProps(Lists.newArrayList(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"No dead activities\","
                + "\"valid\":true,\"additionalInfo\":\"\"}]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesDeadActivities() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE_DEAD);
    String properties = convertProps(Lists.newArrayList(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));

    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"No dead activities\","
                + "\"valid\":false,\"additionalInfo\":\"Dead activities: DEAD_1,"
                + "DEAD_2\"}]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesSafenessAndOptionToComplete() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties =
        convertProps(
            Lists.newArrayList(
                BPMNSpecificProperty.SAFENESS, BPMNSpecificProperty.OPTION_TO_COMPLETE));

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"Safeness\",\"valid\":true,\"additionalInfo\":\"\"},"
                + "{\"name\":\"Option to complete\",\"valid\":false,\"additionalInfo\":\"Checking BPMN-specific properties is not implemented in the web interface yet due to the following bug in Groove https://sourceforge.net/p/groove/bugs/499/\"}]}"));
  }

  private String convertProps(Collection<BPMNSpecificProperty> properties) {
    return properties.stream().map(BPMNSpecificProperty::toString).collect(Collectors.joining(","));
  }

  @Test
  void testCheckBPMNSpecificPropertiesError() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE_ERROR);

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES, bpmnModelFile, Pair.of(PROPERTIES_TO_BE_CHECKED, ""));
    assertThat(
        response,
        is(
            "{\"message\":\"Intermediate throw events should have exactly one incoming sequence "
                + "flow!\"}"));
  }

  @Test
  void testCheckCTL() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE);

    String response =
        makeMultipartRequest(
            CHECK_TEMPORAL_LOGIC,
            bpmnModelFile,
            Lists.newArrayList(Pair.of("logic", "CTL"), Pair.of("property", "AG(!Unsafe)")));
    assertThat(response, is("{\"valid\":true,\"error\":\"\"}"));
  }

  @Test
  void testCheckCTLError() throws Exception {
    File bpmnModelFile = getBpmnModelFile(BPMN_FILE);

    String response =
        makeMultipartRequest(
            CHECK_TEMPORAL_LOGIC,
            bpmnModelFile,
            Lists.newArrayList(Pair.of("logic", "CTL"), Pair.of("property", "G(!Unsafe)")));
    assertThat(
        response,
        is(
            "{\"valid\":false,\"error\":\"Error: groove.util.parse.FormatException: Temporal operator 'G' should be nested inside path quantifier in CTL formula\"}"));
  }

  private File getBpmnModelFile(String bpmnFile) {
    @SuppressWarnings("ConstantConditions")
    File bpmnModelFile = new File(this.getClass().getResource(bpmnFile).getFile());
    return bpmnModelFile;
  }

  private String makeMultipartRequest(
      String url, File bpmnModelFile, List<Pair<String, String>> body) throws IOException {
    String responseString;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      CloseableHttpResponse response;
      // Build request
      HttpPost uploadFile = new HttpPost(getFullUrl(url));
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      body.forEach(bodyPart -> builder.addTextBody(bodyPart.getLeft(), bodyPart.getRight()));
      builder.addBinaryBody(
          "file",
          new FileInputStream(bpmnModelFile),
          ContentType.APPLICATION_OCTET_STREAM,
          bpmnModelFile.getName());
      HttpEntity multipart = builder.build();
      uploadFile.setEntity(multipart);

      response = httpClient.execute(uploadFile);

      // Check response
      responseString =
          IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
    }
    return responseString;
  }

  private String makeMultipartRequest(String url, File bpmnModelFile, Pair<String, String> body)
      throws IOException {
    return makeMultipartRequest(url, bpmnModelFile, Lists.newArrayList(body));
  }
}
