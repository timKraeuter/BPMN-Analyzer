package no.hvl.tk.ruleGenerator.server.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import behavior.bpmn.BPMNCollaboration;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingRequest;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import static no.hvl.tk.ruleGenerator.server.endpoint.RuleGeneratorControllerHelper.deleteOldGGsAndCreateNewDir;
import static no.hvl.tk.ruleGenerator.server.endpoint.RuleGeneratorControllerHelper.generateGGForBPMNFile;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class RuleGeneratorController {

    // TODO: Testcase zip
    // TODO: Testcase model checking

    /**
     * Generate a graph grammar for a given BPMN file.
     * @param file BPMN file defining a collaboration.
     * @param response will be a ZIP of the graph grammar for the BPMN file.
     */
    @RequestMapping(value = "/generateGGAndZip", produces = "application/zip")
    public void generateGGAndReturnZIP(@RequestPart("file") MultipartFile file,
                                       HttpServletResponse response) throws IOException {
        // Not made for concurrent access of the application!
        deleteOldGGsAndCreateNewDir();

        File resultDir = generateGGForBPMNFile(file).getLeft();

        // Zip all files
        File[] allFiles = resultDir.listFiles();
        List<File> graphGrammarFiles = Arrays.asList(allFiles != null ? allFiles : new File[]{});
        zipAndReturnFiles(response, graphGrammarFiles);
    }

    private void zipAndReturnFiles(HttpServletResponse response, List<File> files) throws IOException {
        // Setting headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"graphGrammar.gps.zip\"");

        // Zipping files
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for (File file : files) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);

                IOUtils.copy(fileInputStream, zipOutputStream);

                fileInputStream.close();
                zipOutputStream.closeEntry();
            }
        }
    }

    /**
     * Run model-checking of certain BPMN-specific properties for a BPMN collaboration.
     * @param request contains the BPMN file and properties to be checked.
     * @return model-checking results for the requested properties.
     */
    @RequestMapping(value = "/checkBPMNSpecificProperties")
    public ModelCheckingResponse modelCheckBPMN(@ModelAttribute ModelCheckingRequest request) throws IOException {
        deleteOldGGsAndCreateNewDir();

        Pair<File, BPMNCollaboration> result = generateGGForBPMNFile(request.getFile());

        return new BPMNModelChecker(result.getLeft(), result.getRight()).runModelChecking(request);
    }
}