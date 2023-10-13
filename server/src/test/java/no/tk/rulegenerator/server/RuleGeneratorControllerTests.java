package no.tk.rulegenerator.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RuleGeneratorControllerTests {

  public static final String BPMN_FILE = "/ruleGeneratorController/name-with-numbers.bpmn";
  public static final String BPMN_FILE_ERROR = "/ruleGeneratorController/errorModel.bpmn";
  public static final String BPMN_FILE_DEAD = "/ruleGeneratorController/dead.bpmn";
  public static final String BPMN_FILE_PROPOSITIONS = "/ruleGeneratorController/propositions.bpmn";
  public static final String LOCALHOST = "http://localhost:%s/%s";
  public static final String PROPERTIES_TO_BE_CHECKED = "propertiesToBeChecked";
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
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);

    CloseableHttpResponse response;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      // Build request
      HttpPost uploadFile = new HttpPost(getFullUrl("generateGGAndZip"));
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addBinaryBody(
          "file",
          Files.newInputStream(bpmnModelFile),
          ContentType.APPLICATION_OCTET_STREAM,
          bpmnModelFile.getFileName().toString());
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
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
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
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_DEAD);
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
                + "\"valid\":false,\"additionalInfo\":\"dead1_id,dead2_id\"}]}"));
  }

  @Test
  void testCheckBPMNSpecificPropertiesSafenessAndOptionToComplete() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE);
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
            Lists.newArrayList(Pair.of("logic", "CTL"), Pair.of("property", "AG(!Unsafe)")));
    assertThat(response, is("{\"property\":\"AG(!Unsafe)\",\"valid\":true,\"error\":\"\"}"));
  }

  @Test
  void testCheckCTLWithCustomPropositions() throws Exception {
    Path bpmnModelFile = getBpmnModelFile(BPMN_FILE_PROPOSITIONS);

    String response =
        makeMultipartRequest(
            CHECK_TEMPORAL_LOGIC,
            bpmnModelFile,
            Lists.newArrayList(
                Pair.of("logic", "CTL"),
                Pair.of("property", "EF(a) & EF(sf) & EF(unnamedFlow) & EF(unnamedTask)"),
                Pair.of(
                    "propositions",
                    // Added propositions as string instead of reading three files.
                    "[{\"name\":\"a\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n<definitions xmlns=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL\\\" xmlns:bpmndi=\\\"http://www.omg.org/spec/BPMN/20100524/DI\\\" xmlns:omgdc=\\\"http://www.omg.org/spec/DD/20100524/DC\\\" xmlns:omgdi=\\\"http://www.omg.org/spec/DD/20100524/DI\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xmlns:bt=\\\"http://tk/schema/1.0/bt\\\" xmlns:bioc=\\\"http://bpmn.io/schema/bpmn/biocolor/1.0\\\" xmlns:color=\\\"http://www.omg.org/spec/BPMN/non-normative/color/1.0\\\" targetNamespace=\\\"\\\" xsi:schemaLocation=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd\\\"><process id=\\\"Process_00fdlmt\\\"><extensionElements><bt:processSnapshot id=\\\"ProcessSnapshot_0vizifk\\\" /><bt:token id=\\\"Token_1ak8s1u\\\" processSnapshot=\\\"ProcessSnapshot_0vizifk\\\" /></extensionElements><startEvent id=\\\"Event_18czgyp\\\"><outgoing>Flow_1nyqtbr</outgoing></startEvent><task id=\\\"Activity_066r1vr\\\" name=\\\"A\\\"><incoming>Flow_1nyqtbr</incoming><outgoing>Flow_1puhjbw</outgoing></task><sequenceFlow id=\\\"Flow_1nyqtbr\\\" name=\\\"sf\\\" sourceRef=\\\"Event_18czgyp\\\" targetRef=\\\"Activity_066r1vr\\\" /><task id=\\\"Activity_1fzxoyj\\\"><incoming>Flow_1puhjbw</incoming><outgoing>Flow_06vnlyp</outgoing></task><sequenceFlow id=\\\"Flow_1puhjbw\\\" sourceRef=\\\"Activity_066r1vr\\\" targetRef=\\\"Activity_1fzxoyj\\\" /><endEvent id=\\\"Event_0qqd83m\\\"><incoming>Flow_06vnlyp</incoming></endEvent><sequenceFlow id=\\\"Flow_06vnlyp\\\" sourceRef=\\\"Activity_1fzxoyj\\\" targetRef=\\\"Event_0qqd83m\\\" /><association id=\\\"Association_0grzgrz\\\" sourceRef=\\\"Activity_066r1vr\\\" targetRef=\\\"Token_1ak8s1u\\\" /></process><bpmndi:BPMNDiagram id=\\\"sid-74620812-92c4-44e5-949c-aa47393d3830\\\"><bpmndi:BPMNPlane id=\\\"sid-cdcae759-2af7-4a6d-bd02-53f3352a731d\\\" bpmnElement=\\\"Process_00fdlmt\\\"><bpmndi:BPMNShape id=\\\"Event_18czgyp_di\\\" bpmnElement=\\\"Event_18czgyp\\\"><omgdc:Bounds x=\\\"222\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_066r1vr_di\\\" bpmnElement=\\\"Activity_066r1vr\\\"><omgdc:Bounds x=\\\"310\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /><bpmndi:BPMNLabel /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_1fzxoyj_di\\\" bpmnElement=\\\"Activity_1fzxoyj\\\"><omgdc:Bounds x=\\\"470\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Event_0qqd83m_di\\\" bpmnElement=\\\"Event_0qqd83m\\\"><omgdc:Bounds x=\\\"632\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"ProcessSnapshot_0vizifk_di\\\" bpmnElement=\\\"ProcessSnapshot_0vizifk\\\" bioc:fill=\\\"#1298ba\\\" color:background-color=\\\"#1298ba\\\"><omgdc:Bounds x=\\\"210\\\" y=\\\"290\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Token_1ak8s1u_di\\\" bpmnElement=\\\"Token_1ak8s1u\\\" bioc:fill=\\\"#1298ba\\\" color:background-color=\\\"#1298ba\\\"><omgdc:Bounds x=\\\"350\\\" y=\\\"280\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNEdge id=\\\"Flow_1nyqtbr_di\\\" bpmnElement=\\\"Flow_1nyqtbr\\\"><omgdi:waypoint x=\\\"258\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"310\\\" y=\\\"190\\\" /><bpmndi:BPMNLabel><omgdc:Bounds x=\\\"279\\\" y=\\\"172\\\" width=\\\"11\\\" height=\\\"14\\\" /></bpmndi:BPMNLabel></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_1puhjbw_di\\\" bpmnElement=\\\"Flow_1puhjbw\\\"><omgdi:waypoint x=\\\"410\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"470\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_06vnlyp_di\\\" bpmnElement=\\\"Flow_06vnlyp\\\"><omgdi:waypoint x=\\\"570\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"632\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Association_0grzgrz_di\\\" bpmnElement=\\\"Association_0grzgrz\\\"><omgdi:waypoint x=\\\"360\\\" y=\\\"230\\\" /><omgdi:waypoint x=\\\"360\\\" y=\\\"280\\\" /></bpmndi:BPMNEdge></bpmndi:BPMNPlane><bpmndi:BPMNLabelStyle id=\\\"sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"11\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle><bpmndi:BPMNLabelStyle id=\\\"sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"12\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle></bpmndi:BPMNDiagram></definitions>\"},{\"name\":\"sf\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n<definitions xmlns=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL\\\" xmlns:bpmndi=\\\"http://www.omg.org/spec/BPMN/20100524/DI\\\" xmlns:omgdc=\\\"http://www.omg.org/spec/DD/20100524/DC\\\" xmlns:omgdi=\\\"http://www.omg.org/spec/DD/20100524/DI\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xmlns:bt=\\\"http://tk/schema/1.0/bt\\\" xmlns:bioc=\\\"http://bpmn.io/schema/bpmn/biocolor/1.0\\\" xmlns:color=\\\"http://www.omg.org/spec/BPMN/non-normative/color/1.0\\\" targetNamespace=\\\"\\\" xsi:schemaLocation=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd\\\"><process id=\\\"Process_00fdlmt\\\"><extensionElements><bt:processSnapshot id=\\\"ProcessSnapshot_0txhj1e\\\" /><bt:token id=\\\"Token_06dl8kn\\\" processSnapshot=\\\"ProcessSnapshot_0txhj1e\\\" /></extensionElements><startEvent id=\\\"Event_18czgyp\\\"><outgoing>Flow_1nyqtbr</outgoing></startEvent><task id=\\\"Activity_066r1vr\\\" name=\\\"A\\\"><incoming>Flow_1nyqtbr</incoming><outgoing>Flow_1puhjbw</outgoing></task><sequenceFlow id=\\\"Flow_1nyqtbr\\\" name=\\\"sf\\\" sourceRef=\\\"Event_18czgyp\\\" targetRef=\\\"Activity_066r1vr\\\" /><task id=\\\"Activity_1fzxoyj\\\"><incoming>Flow_1puhjbw</incoming><outgoing>Flow_06vnlyp</outgoing></task><sequenceFlow id=\\\"Flow_1puhjbw\\\" sourceRef=\\\"Activity_066r1vr\\\" targetRef=\\\"Activity_1fzxoyj\\\" /><endEvent id=\\\"Event_0qqd83m\\\"><incoming>Flow_06vnlyp</incoming></endEvent><sequenceFlow id=\\\"Flow_06vnlyp\\\" sourceRef=\\\"Activity_1fzxoyj\\\" targetRef=\\\"Event_0qqd83m\\\" /><association id=\\\"Association_1ka5ysn\\\" sourceRef=\\\"Flow_1nyqtbr\\\" targetRef=\\\"Token_06dl8kn\\\" /></process><bpmndi:BPMNDiagram id=\\\"sid-74620812-92c4-44e5-949c-aa47393d3830\\\"><bpmndi:BPMNPlane id=\\\"sid-cdcae759-2af7-4a6d-bd02-53f3352a731d\\\" bpmnElement=\\\"Process_00fdlmt\\\"><bpmndi:BPMNShape id=\\\"Event_18czgyp_di\\\" bpmnElement=\\\"Event_18czgyp\\\"><omgdc:Bounds x=\\\"222\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_066r1vr_di\\\" bpmnElement=\\\"Activity_066r1vr\\\"><omgdc:Bounds x=\\\"310\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /><bpmndi:BPMNLabel /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_1fzxoyj_di\\\" bpmnElement=\\\"Activity_1fzxoyj\\\"><omgdc:Bounds x=\\\"470\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Event_0qqd83m_di\\\" bpmnElement=\\\"Event_0qqd83m\\\"><omgdc:Bounds x=\\\"632\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"ProcessSnapshot_0txhj1e_di\\\" bpmnElement=\\\"ProcessSnapshot_0txhj1e\\\" bioc:fill=\\\"#451984\\\" color:background-color=\\\"#451984\\\"><omgdc:Bounds x=\\\"170\\\" y=\\\"240\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Token_06dl8kn_di\\\" bpmnElement=\\\"Token_06dl8kn\\\" bioc:fill=\\\"#451984\\\" color:background-color=\\\"#451984\\\"><omgdc:Bounds x=\\\"274.5\\\" y=\\\"241\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNEdge id=\\\"Flow_1nyqtbr_di\\\" bpmnElement=\\\"Flow_1nyqtbr\\\"><omgdi:waypoint x=\\\"258\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"310\\\" y=\\\"190\\\" /><bpmndi:BPMNLabel><omgdc:Bounds x=\\\"279\\\" y=\\\"172\\\" width=\\\"11\\\" height=\\\"14\\\" /></bpmndi:BPMNLabel></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_1puhjbw_di\\\" bpmnElement=\\\"Flow_1puhjbw\\\"><omgdi:waypoint x=\\\"410\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"470\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_06vnlyp_di\\\" bpmnElement=\\\"Flow_06vnlyp\\\"><omgdi:waypoint x=\\\"570\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"632\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Association_1ka5ysn_di\\\" bpmnElement=\\\"Association_1ka5ysn\\\"><omgdi:waypoint x=\\\"285\\\" y=\\\"186\\\" /><omgdi:waypoint x=\\\"285\\\" y=\\\"241\\\" /></bpmndi:BPMNEdge></bpmndi:BPMNPlane><bpmndi:BPMNLabelStyle id=\\\"sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"11\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle><bpmndi:BPMNLabelStyle id=\\\"sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"12\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle></bpmndi:BPMNDiagram></definitions>\"},{\"name\":\"unnamedTask\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n<definitions xmlns=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL\\\" xmlns:bpmndi=\\\"http://www.omg.org/spec/BPMN/20100524/DI\\\" xmlns:omgdc=\\\"http://www.omg.org/spec/DD/20100524/DC\\\" xmlns:omgdi=\\\"http://www.omg.org/spec/DD/20100524/DI\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xmlns:bt=\\\"http://tk/schema/1.0/bt\\\" xmlns:bioc=\\\"http://bpmn.io/schema/bpmn/biocolor/1.0\\\" xmlns:color=\\\"http://www.omg.org/spec/BPMN/non-normative/color/1.0\\\" targetNamespace=\\\"\\\" xsi:schemaLocation=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd\\\"><process id=\\\"Process_00fdlmt\\\"><extensionElements><bt:processSnapshot id=\\\"ProcessSnapshot_0llz96x\\\" /><bt:token id=\\\"Token_1su6pcl\\\" processSnapshot=\\\"ProcessSnapshot_0llz96x\\\" /></extensionElements><startEvent id=\\\"Event_18czgyp\\\"><outgoing>Flow_1nyqtbr</outgoing></startEvent><task id=\\\"Activity_066r1vr\\\" name=\\\"A\\\"><incoming>Flow_1nyqtbr</incoming><outgoing>Flow_1puhjbw</outgoing></task><sequenceFlow id=\\\"Flow_1nyqtbr\\\" name=\\\"sf\\\" sourceRef=\\\"Event_18czgyp\\\" targetRef=\\\"Activity_066r1vr\\\" /><task id=\\\"Activity_1fzxoyj\\\"><incoming>Flow_1puhjbw</incoming><outgoing>Flow_06vnlyp</outgoing></task><sequenceFlow id=\\\"Flow_1puhjbw\\\" sourceRef=\\\"Activity_066r1vr\\\" targetRef=\\\"Activity_1fzxoyj\\\" /><endEvent id=\\\"Event_0qqd83m\\\"><incoming>Flow_06vnlyp</incoming></endEvent><sequenceFlow id=\\\"Flow_06vnlyp\\\" sourceRef=\\\"Activity_1fzxoyj\\\" targetRef=\\\"Event_0qqd83m\\\" /><association id=\\\"Association_1ubbg30\\\" sourceRef=\\\"Activity_1fzxoyj\\\" targetRef=\\\"Token_1su6pcl\\\" /></process><bpmndi:BPMNDiagram id=\\\"sid-74620812-92c4-44e5-949c-aa47393d3830\\\"><bpmndi:BPMNPlane id=\\\"sid-cdcae759-2af7-4a6d-bd02-53f3352a731d\\\" bpmnElement=\\\"Process_00fdlmt\\\"><bpmndi:BPMNShape id=\\\"Event_18czgyp_di\\\" bpmnElement=\\\"Event_18czgyp\\\"><omgdc:Bounds x=\\\"222\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_066r1vr_di\\\" bpmnElement=\\\"Activity_066r1vr\\\"><omgdc:Bounds x=\\\"310\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /><bpmndi:BPMNLabel /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_1fzxoyj_di\\\" bpmnElement=\\\"Activity_1fzxoyj\\\"><omgdc:Bounds x=\\\"470\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Event_0qqd83m_di\\\" bpmnElement=\\\"Event_0qqd83m\\\"><omgdc:Bounds x=\\\"632\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"ProcessSnapshot_0llz96x_di\\\" bpmnElement=\\\"ProcessSnapshot_0llz96x\\\" bioc:fill=\\\"#7d7ad3\\\" color:background-color=\\\"#7d7ad3\\\"><omgdc:Bounds x=\\\"220\\\" y=\\\"300\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Token_1su6pcl_di\\\" bpmnElement=\\\"Token_1su6pcl\\\" bioc:fill=\\\"#7d7ad3\\\" color:background-color=\\\"#7d7ad3\\\"><omgdc:Bounds x=\\\"510\\\" y=\\\"280\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNEdge id=\\\"Flow_1nyqtbr_di\\\" bpmnElement=\\\"Flow_1nyqtbr\\\"><omgdi:waypoint x=\\\"258\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"310\\\" y=\\\"190\\\" /><bpmndi:BPMNLabel><omgdc:Bounds x=\\\"279\\\" y=\\\"172\\\" width=\\\"11\\\" height=\\\"14\\\" /></bpmndi:BPMNLabel></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_1puhjbw_di\\\" bpmnElement=\\\"Flow_1puhjbw\\\"><omgdi:waypoint x=\\\"410\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"470\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_06vnlyp_di\\\" bpmnElement=\\\"Flow_06vnlyp\\\"><omgdi:waypoint x=\\\"570\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"632\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Association_1ubbg30_di\\\" bpmnElement=\\\"Association_1ubbg30\\\"><omgdi:waypoint x=\\\"520\\\" y=\\\"230\\\" /><omgdi:waypoint x=\\\"520\\\" y=\\\"280\\\" /></bpmndi:BPMNEdge></bpmndi:BPMNPlane><bpmndi:BPMNLabelStyle id=\\\"sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"11\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle><bpmndi:BPMNLabelStyle id=\\\"sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"12\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle></bpmndi:BPMNDiagram></definitions>\"},{\"name\":\"unnamedFlow\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n<definitions xmlns=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL\\\" xmlns:bpmndi=\\\"http://www.omg.org/spec/BPMN/20100524/DI\\\" xmlns:omgdc=\\\"http://www.omg.org/spec/DD/20100524/DC\\\" xmlns:omgdi=\\\"http://www.omg.org/spec/DD/20100524/DI\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xmlns:bt=\\\"http://tk/schema/1.0/bt\\\" xmlns:bioc=\\\"http://bpmn.io/schema/bpmn/biocolor/1.0\\\" xmlns:color=\\\"http://www.omg.org/spec/BPMN/non-normative/color/1.0\\\" targetNamespace=\\\"\\\" xsi:schemaLocation=\\\"http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd\\\"><process id=\\\"Process_00fdlmt\\\"><extensionElements><bt:processSnapshot id=\\\"ProcessSnapshot_0lc5bi8\\\" /><bt:token id=\\\"Token_0nsrtn0\\\" processSnapshot=\\\"ProcessSnapshot_0lc5bi8\\\" /></extensionElements><startEvent id=\\\"Event_18czgyp\\\"><outgoing>Flow_1nyqtbr</outgoing></startEvent><task id=\\\"Activity_066r1vr\\\" name=\\\"A\\\"><incoming>Flow_1nyqtbr</incoming><outgoing>Flow_1puhjbw</outgoing></task><sequenceFlow id=\\\"Flow_1nyqtbr\\\" name=\\\"sf\\\" sourceRef=\\\"Event_18czgyp\\\" targetRef=\\\"Activity_066r1vr\\\" /><task id=\\\"Activity_1fzxoyj\\\"><incoming>Flow_1puhjbw</incoming><outgoing>Flow_06vnlyp</outgoing></task><sequenceFlow id=\\\"Flow_1puhjbw\\\" sourceRef=\\\"Activity_066r1vr\\\" targetRef=\\\"Activity_1fzxoyj\\\" /><endEvent id=\\\"Event_0qqd83m\\\"><incoming>Flow_06vnlyp</incoming></endEvent><sequenceFlow id=\\\"Flow_06vnlyp\\\" sourceRef=\\\"Activity_1fzxoyj\\\" targetRef=\\\"Event_0qqd83m\\\" /><association id=\\\"Association_127p1jo\\\" sourceRef=\\\"Flow_1puhjbw\\\" targetRef=\\\"Token_0nsrtn0\\\" /></process><bpmndi:BPMNDiagram id=\\\"sid-74620812-92c4-44e5-949c-aa47393d3830\\\"><bpmndi:BPMNPlane id=\\\"sid-cdcae759-2af7-4a6d-bd02-53f3352a731d\\\" bpmnElement=\\\"Process_00fdlmt\\\"><bpmndi:BPMNShape id=\\\"Event_18czgyp_di\\\" bpmnElement=\\\"Event_18czgyp\\\"><omgdc:Bounds x=\\\"222\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_066r1vr_di\\\" bpmnElement=\\\"Activity_066r1vr\\\"><omgdc:Bounds x=\\\"310\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /><bpmndi:BPMNLabel /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Activity_1fzxoyj_di\\\" bpmnElement=\\\"Activity_1fzxoyj\\\"><omgdc:Bounds x=\\\"470\\\" y=\\\"150\\\" width=\\\"100\\\" height=\\\"80\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Event_0qqd83m_di\\\" bpmnElement=\\\"Event_0qqd83m\\\"><omgdc:Bounds x=\\\"632\\\" y=\\\"172\\\" width=\\\"36\\\" height=\\\"36\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"ProcessSnapshot_0lc5bi8_di\\\" bpmnElement=\\\"ProcessSnapshot_0lc5bi8\\\" bioc:fill=\\\"#714fc6\\\" color:background-color=\\\"#714fc6\\\"><omgdc:Bounds x=\\\"260\\\" y=\\\"310\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNShape id=\\\"Token_0nsrtn0_di\\\" bpmnElement=\\\"Token_0nsrtn0\\\" bioc:fill=\\\"#714fc6\\\" color:background-color=\\\"#714fc6\\\"><omgdc:Bounds x=\\\"430\\\" y=\\\"250\\\" width=\\\"20\\\" height=\\\"20\\\" /></bpmndi:BPMNShape><bpmndi:BPMNEdge id=\\\"Flow_1nyqtbr_di\\\" bpmnElement=\\\"Flow_1nyqtbr\\\"><omgdi:waypoint x=\\\"258\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"310\\\" y=\\\"190\\\" /><bpmndi:BPMNLabel><omgdc:Bounds x=\\\"279\\\" y=\\\"172\\\" width=\\\"11\\\" height=\\\"14\\\" /></bpmndi:BPMNLabel></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_1puhjbw_di\\\" bpmnElement=\\\"Flow_1puhjbw\\\"><omgdi:waypoint x=\\\"410\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"470\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Flow_06vnlyp_di\\\" bpmnElement=\\\"Flow_06vnlyp\\\"><omgdi:waypoint x=\\\"570\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"632\\\" y=\\\"190\\\" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id=\\\"Association_127p1jo_di\\\" bpmnElement=\\\"Association_127p1jo\\\"><omgdi:waypoint x=\\\"440\\\" y=\\\"190\\\" /><omgdi:waypoint x=\\\"440\\\" y=\\\"250\\\" /></bpmndi:BPMNEdge></bpmndi:BPMNPlane><bpmndi:BPMNLabelStyle id=\\\"sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"11\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle><bpmndi:BPMNLabelStyle id=\\\"sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b\\\"><omgdc:Font name=\\\"Arial\\\" size=\\\"12\\\" isBold=\\\"false\\\" isItalic=\\\"false\\\" isUnderline=\\\"false\\\" isStrikeThrough=\\\"false\\\" /></bpmndi:BPMNLabelStyle></bpmndi:BPMNDiagram></definitions>\"}]\n")));
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
            Lists.newArrayList(Pair.of("logic", "CTL"), Pair.of("property", "G(!Unsafe)")));
    assertThat(
        response,
        is(
            "{\"property\":\"G(!Unsafe)\",\"valid\":false,\"error\":\"Error: nl.utwente.groove.util.parse.FormatException: Temporal operator 'G' should be nested inside path quantifier in CTL formula\"}"));
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

  private String makeMultipartRequest(
      String url, Path bpmnModelFile, List<Pair<String, String>> body) throws IOException {
    String responseString;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      CloseableHttpResponse response;
      // Build request
      HttpPost uploadFile = new HttpPost(getFullUrl(url));
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      body.forEach(bodyPart -> builder.addTextBody(bodyPart.getLeft(), bodyPart.getRight()));
      builder.addBinaryBody(
          "file",
          Files.newInputStream(bpmnModelFile),
          ContentType.APPLICATION_OCTET_STREAM,
          bpmnModelFile.getFileName().toString());
      HttpEntity multipart = builder.build();
      uploadFile.setEntity(multipart);

      response = httpClient.execute(uploadFile);

      // Check response
      responseString =
          IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
    }
    return responseString;
  }

  private String makeMultipartRequest(String url, Path bpmnModelFile, Pair<String, String> body)
      throws IOException {
    return makeMultipartRequest(url, bpmnModelFile, Lists.newArrayList(body));
  }
}
