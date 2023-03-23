package no.hvl.tk.rulegenerator.server.endpoint;

import static no.hvl.tk.rulegenerator.server.endpoint.RuleGeneratorControllerHelper.deleteGGsAndStateSpacesOlderThanOneHour;
import static no.hvl.tk.rulegenerator.server.endpoint.RuleGeneratorControllerHelper.generateGGForBPMNFile;

import behavior.bpmn.BPMNCollaboration;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingRequest;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingResponse;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.ModelCheckingRequest;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.ModelCheckingResponse;
import no.hvl.tk.rulegenerator.server.endpoint.verification.BPMNModelChecker;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class RuleGeneratorController {

  /**
   * Generate a graph grammar for a given BPMN file.
   *
   * @param file BPMN file defining a collaboration.
   * @param response will be a ZIP of the graph grammar for the BPMN file.
   */
  @PostMapping(value = "/generateGGAndZip", produces = "application/zip")
  public void generateGGAndZip(
      @RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
    deleteGGsAndStateSpacesOlderThanOneHour();

    Path resultDir = generateGGForBPMNFile(file).getLeft();

    // Zip all files
    try (DirectoryStream<Path> graphGrammarFiles = Files.newDirectoryStream(resultDir)) {
      zipAndReturnFiles(response, graphGrammarFiles);
    }
  }

  private void zipAndReturnFiles(HttpServletResponse response, DirectoryStream<Path> files)
      throws IOException {
    // Setting headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.addHeader("Content-Disposition", "attachment; filename=\"graphGrammar.gps.zip\"");

    // Zipping files
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
      for (Path file : files) {
        zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));

        Files.copy(file, zipOutputStream);

        zipOutputStream.closeEntry();
      }
    }
  }

  /**
   * Run model-checking of certain BPMN-specific properties for a BPMN collaboration.
   *
   * @param request contains the BPMN file and properties to be checked.
   * @return model-checking results for the requested properties.
   */
  @PostMapping(value = "/checkBPMNSpecificProperties")
  public BPMNSpecificPropertyCheckingResponse checkBPMNSpecificProperties(
      @ModelAttribute BPMNSpecificPropertyCheckingRequest request)
      throws IOException, InterruptedException {
    deleteGGsAndStateSpacesOlderThanOneHour();

    Pair<Path, BPMNCollaboration> result = generateGGForBPMNFile(request.getFile());

    return new BPMNModelChecker(result.getLeft(), result.getRight()).checkBPMNProperties(request);
  }

  /**
   * Run model-checking of certain BPMN-specific properties for a BPMN collaboration.
   *
   * @param request contains the BPMN file and properties to be checked.
   * @return model-checking results for the requested properties.
   */
  @PostMapping(value = "/checkTemporalLogic")
  public ModelCheckingResponse checkTemporalLogicProperty(
      @ModelAttribute ModelCheckingRequest request) throws IOException, InterruptedException {
    deleteGGsAndStateSpacesOlderThanOneHour();

    Pair<Path, BPMNCollaboration> result = generateGGForBPMNFile(request.getFile());

    return new BPMNModelChecker(result.getLeft(), result.getRight())
        .checkTemporalLogicProperty(request.getLogic(), request.getProperty());
  }
}
