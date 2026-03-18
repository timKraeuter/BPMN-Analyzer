package no.tk.rulegenerator.server;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import no.tk.rulegenerator.server.endpoint.RuleGeneratorController;
import no.tk.rulegenerator.server.endpoint.RuleGeneratorControllerHelper;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificProperty;
import no.tk.rulegenerator.server.util.Pair;
import org.apache.commons.io.file.PathUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RuleGeneratorControllerTests {

  public static final String BPMN_FILE = "/ruleGeneratorController/name-with-numbers.bpmn";
  public static final String BPMN_FILE_ERROR = "/ruleGeneratorController/errorModel.bpmn";
  public static final String BPMN_FILE_DEAD = "/ruleGeneratorController/dead.bpmn";
  public static final String BPMN_FILE_PROPOSITIONS = "/ruleGeneratorController/propositions.bpmn";
  public static final String FOUR_PROPOSITIONS_JSON =
      "/ruleGeneratorController/four-propositions.json";
  public static final String SINGLE_PROPOSITION_JSON =
      "/ruleGeneratorController/single-proposition.json";
  public static final String PROPERTIES_TO_BE_CHECKED = "propertiesToBeChecked";
  public static final String CHECK_BPMN_SPECIFIC_PROPERTIES = "checkBPMNSpecificProperties";
  public static final String CHECK_TEMPORAL_LOGIC = "checkTemporalLogic";

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private RuleGeneratorController restController;

  @Test
  void testContextLoads() {
    assertNotNull(restController);
  }

  @Test
  void testGenerateGGAndZip() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(bpmnModelFile));

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<byte[]> response =
        restTemplate.postForEntity("/generateGGAndZip", requestEntity, byte[].class);

    // Check response
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(response.getBody()));
    Set<String> zipEntryNames = this.getZipEntryNames(zis);
    // We only check file names not content since the content contains timestamps etc.
    assertThat(
        zipEntryNames,
        is(
            Set.of(
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
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties = convertProps(List.of(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

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
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_DEAD);
    String properties = convertProps(List.of(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));

    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"No dead activities\","
                + "\"valid\":false,\"additionalInfo\":\"dead1_id,dead2_id\"}]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesSafenessAndOptionToComplete() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties =
        convertProps(
            List.of(BPMNSpecificProperty.SAFENESS, BPMNSpecificProperty.OPTION_TO_COMPLETE));

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"Safeness\",\"valid\":true,\"additionalInfo\":\"CTL: AG(!Unsafe)\"},"
                + "{\"name\":\"Option to complete\",\"valid\":true,\"additionalInfo\":\"CTL: AF(AllTerminated)\"}]}"));
  }

  private String convertProps(Collection<BPMNSpecificProperty> properties) {
    return properties.stream().map(BPMNSpecificProperty::toString).collect(Collectors.joining(","));
  }

  @Test
  void testCheckBPMNSpecificPropertiesError() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_ERROR);

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
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);

    String response =
        makeMultipartRequest(
            CHECK_TEMPORAL_LOGIC,
            bpmnModelFile,
            List.of(Pair.of("logic", "CTL"), Pair.of("property", "AG(!Unsafe)")));
    assertThat(response, is("{\"property\":\"AG(!Unsafe)\",\"valid\":true,\"error\":\"\"}"));
  }

  @Test
  void testCheckCTLWithCustomPropositions() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_PROPOSITIONS);

    String response =
        makeMultipartRequest(
            CHECK_TEMPORAL_LOGIC,
            bpmnModelFile,
            List.of(
                Pair.of("logic", "CTL"),
                Pair.of("property", "EF(a) & EF(sf) & EF(unnamedFlow) & EF(unnamedTask)"),
                Pair.of("propositions", readResourceAsString(FOUR_PROPOSITIONS_JSON))));
    assertThat(
        response,
        is(
            "{\"property\":\"EF(a) & EF(sf) & EF(unnamedFlow) & EF(unnamedTask)\",\"valid\":true,\"error\":\"\"}"));
  }

  @Test
  void testCheckCTLError() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);

    String response =
        makeMultipartRequest(
            CHECK_TEMPORAL_LOGIC,
            bpmnModelFile,
            List.of(Pair.of("logic", "CTL"), Pair.of("property", "G(!Unsafe)")));
    assertThat(
        response,
        is(
            "{\"property\":\"G(!Unsafe)\",\"valid\":false,\"error\":\"Error: Error while parsing 'G(!Unsafe)': Temporal operator 'G' should be nested inside path quantifier in CTL formula\"}"));
  }

  @Test
  void testCheckAllFourBPMNSpecificProperties() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties =
        convertProps(
            List.of(
                BPMNSpecificProperty.SAFENESS,
                BPMNSpecificProperty.OPTION_TO_COMPLETE,
                BPMNSpecificProperty.PROPER_COMPLETION,
                BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));

    // Results should be sorted by property ordering: Safeness, Option to complete, Proper
    // completion, No dead activities
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":["
                + "{\"name\":\"Safeness\",\"valid\":true,\"additionalInfo\":\"CTL: AG(!Unsafe)\"},"
                + "{\"name\":\"Option to complete\",\"valid\":true,\"additionalInfo\":\"CTL: AF(AllTerminated)\"},"
                + "{\"name\":\"Proper completion\",\"valid\":true,\"additionalInfo\":\"\"},"
                + "{\"name\":\"No dead activities\",\"valid\":true,\"additionalInfo\":\"\"}"
                + "]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesEmptyFile() {
    Pair<Integer, String> response =
        makeMultipartRequestWithBytes(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            new byte[0],
            "empty.bpmn",
            List.of(
                Pair.of(
                    PROPERTIES_TO_BE_CHECKED,
                    convertProps(List.of(BPMNSpecificProperty.NO_DEAD_ACTIVITIES)))));

    // An empty file is rejected by input validation — expect a 400 Bad Request
    assertThat(response.left(), is(400));
    assertThat(response.right(), containsString("BPMN file is required"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesMissingFile() {
    Pair<Integer, String> response =
        makeMultipartRequestWithoutFile(CHECK_BPMN_SPECIFIC_PROPERTIES);

    // Missing required file part — expect a 400 Bad Request
    assertThat(response.left(), is(400));
    assertThat(response.right(), containsString("BPMN file is required"));
  }

  @Test
  void testUploadExceedingSizeLimitIsRejected() {
    // Create an 11MB byte array (exceeds the 10MB max-file-size)
    byte[] oversizedPayload = new byte[11 * 1024 * 1024];
    try {
      Pair<Integer, String> response =
          makeMultipartRequestWithBytes(
              CHECK_BPMN_SPECIFIC_PROPERTIES,
              oversizedPayload,
              "oversized.bpmn",
              List.of(
                  Pair.of(
                      PROPERTIES_TO_BE_CHECKED,
                      convertProps(List.of(BPMNSpecificProperty.NO_DEAD_ACTIVITIES)))));

      // Spring should reject with a 413 Payload Too Large (or 500 wrapping MaxUploadSizeExceeded)
      assertTrue(
          response.left() == 413 || response.left() == 500,
          "Expected 413 or 500 but got " + response.left());
    } catch (ResourceAccessException _) {
      // On some JDKs, Tomcat closes the connection before a response is fully sent for oversized
      // uploads. A ResourceAccessException (connection reset/aborted) still means the server
      // correctly rejected the payload.
    }
  }

  @Test
  void testCheckTemporalLogicEmptyFile() {
    // Given: An empty file and valid parameters
    Pair<Integer, String> response =
        makeMultipartRequestWithBytes(
            CHECK_TEMPORAL_LOGIC,
            new byte[0],
            "empty.bpmn",
            List.of(Pair.of("logic", "CTL"), Pair.of("property", "AG(!Unsafe)")));

    // Then: Expect 400 Bad Request
    assertThat(response.left(), is(400));
    assertThat(response.right(), containsString("BPMN file is required"));
  }

  @Test
  void testCheckTemporalLogicMissingFile() {
    // Given: No file part at all
    Pair<Integer, String> response = makeMultipartRequestWithoutFile(CHECK_TEMPORAL_LOGIC);

    // Then: Expect 400 Bad Request
    assertThat(response.left(), is(400));
    assertThat(response.right(), containsString("BPMN file is required"));
  }

  @Test
  void testGenerateGGAndZipError() throws Exception {
    // Given: An invalid BPMN model
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_ERROR);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(bpmnModelFile));

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<String> response =
        restTemplate.postForEntity("/generateGGAndZip", requestEntity, String.class);

    // Then: Expect error response with structured message (400 since it's a BPMN validation error)
    assertThat(response.getStatusCode().value(), is(400));
    assertThat(response.getBody(), containsString("message"));
    assertThat(
        response.getBody(),
        containsString(
            "Intermediate throw events should have exactly one incoming sequence flow!"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesOptionToComplete() throws Exception {
    // Given: A valid BPMN model and only OPTION_TO_COMPLETE property
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties = convertProps(List.of(BPMNSpecificProperty.OPTION_TO_COMPLETE));

    // When
    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));

    // Then
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"Option to complete\","
                + "\"valid\":true,\"additionalInfo\":\"CTL: AF(AllTerminated)\"}]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesProperCompletion() throws Exception {
    // Given: A valid BPMN model and only PROPER_COMPLETION property
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties = convertProps(List.of(BPMNSpecificProperty.PROPER_COMPLETION));

    // When
    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));

    // Then
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"Proper completion\","
                + "\"valid\":true,\"additionalInfo\":\"\"}]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesSafeness() throws Exception {
    // Given: A valid BPMN model and only SAFENESS property
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
    String properties = convertProps(List.of(BPMNSpecificProperty.SAFENESS));

    // When
    String response =
        makeMultipartRequest(
            CHECK_BPMN_SPECIFIC_PROPERTIES,
            bpmnModelFile,
            Pair.of(PROPERTIES_TO_BE_CHECKED, properties));

    // Then
    assertThat(
        response,
        is(
            "{\"propertyCheckingResults\":[{\"name\":\"Safeness\","
                + "\"valid\":true,\"additionalInfo\":\"CTL: AG(!Unsafe)\"}]}"));
  }

  @Test
  void testGenerateGGAndZipWithPropositions() throws Exception {
    // Given: A BPMN model that supports propositions
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_PROPOSITIONS);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // Use the same propositions as testCheckCTLWithCustomPropositions, but only one
    String propositionsJson = readResourceAsString(SINGLE_PROPOSITION_JSON);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(bpmnModelFile));
    body.add("propositions", propositionsJson);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<byte[]> response =
        restTemplate.postForEntity("/generateGGAndZip", requestEntity, byte[].class);

    // Then: Response should be a valid ZIP with more files than without propositions
    assertThat(response.getStatusCode().value(), is(200));
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(response.getBody()));
    Set<String> zipEntryNames = getZipEntryNames(zis);
    // Should contain at least the base files plus the proposition file
    assertFalse(zipEntryNames.isEmpty());
    assertTrue(zipEntryNames.contains("start.gst"));
    assertTrue(zipEntryNames.contains("system.properties"));
    // The proposition should generate an additional .gpr file named "a.gpr"
    assertTrue(
        zipEntryNames.contains("a.gpr"),
        "ZIP should contain proposition file a.gpr but found: " + zipEntryNames);
  }

  @Test
  void timestampFormatTest() {
    Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    MatcherAssert.assertThat(
        RuleGeneratorControllerHelper.DTF.parse(
            RuleGeneratorControllerHelper.DTF.format(instant), Instant::from),
        is(instant));
  }

  @Test
  void deleteGGsOlderThanOneHourTest() throws IOException {
    deleteGGTempDir();
    // Given
    // Create two GGs: one older than one hour and one younger
    Files.createDirectories(Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR));
    String oldGG =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(
            "old", Instant.now().minus(1, ChronoUnit.HOURS));
    String youngGG = RuleGeneratorControllerHelper.getGGOrStateSpaceDirName("young");
    Path oldGGPath =
        Files.createDirectories(
            Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR, oldGG));
    Path youngGGPath =
        Files.createDirectories(
            Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR, youngGG));

    // When
    RuleGeneratorControllerHelper.deleteGGsAndStateSpacesOlderThanOneHour();

    // Then: Only the younger GG survives.
    assertFalse(Files.exists(oldGGPath));
    assertTrue(Files.exists(youngGGPath));
  }

  private void deleteGGTempDir() throws IOException {
    Path tempDir = Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR);
    if (Files.exists(tempDir)) {
      PathUtils.deleteDirectory(tempDir);
    }
  }

  private Path getBpmnModelFile(String bpmnFile) throws URISyntaxException {
    return Paths.get(Objects.requireNonNull(this.getClass().getResource(bpmnFile)).toURI());
  }

  private String readResourceAsString(String resource) throws IOException, URISyntaxException {
    return Files.readString(
        Paths.get(Objects.requireNonNull(this.getClass().getResource(resource)).toURI()));
  }

  private String makeMultipartRequest(
      String url, Path bpmnModelFile, List<Pair<String, String>> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
    body.forEach(bodyPart -> multipartBody.add(bodyPart.left(), bodyPart.right()));
    multipartBody.add("file", new FileSystemResource(bpmnModelFile));

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
        new HttpEntity<>(multipartBody, headers);
    ResponseEntity<String> response =
        restTemplate.postForEntity("/" + url, requestEntity, String.class);
    return response.getBody();
  }

  private String makeMultipartRequest(String url, Path bpmnModelFile, Pair<String, String> body) {
    return makeMultipartRequest(url, bpmnModelFile, List.of(body));
  }

  private Pair<Integer, String> makeMultipartRequestWithBytes(
      String url, byte[] fileContent, String fileName, List<Pair<String, String>> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
    body.forEach(bodyPart -> multipartBody.add(bodyPart.left(), bodyPart.right()));
    multipartBody.add(
        "file",
        new ByteArrayResource(fileContent) {
          @Override
          public String getFilename() {
            return fileName;
          }
        });

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
        new HttpEntity<>(multipartBody, headers);
    ResponseEntity<String> response =
        restTemplate.postForEntity("/" + url, requestEntity, String.class);
    return Pair.of(response.getStatusCode().value(), response.getBody());
  }

  private Pair<Integer, String> makeMultipartRequestWithoutFile(String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
    multipartBody.add("dummy", "value");

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
        new HttpEntity<>(multipartBody, headers);
    ResponseEntity<String> response =
        restTemplate.postForEntity("/" + url, requestEntity, String.class);
    return Pair.of(response.getStatusCode().value(), response.getBody());
  }
}
