package no.hvl.tk.ruleGenerator.server.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviorTransformer.BehaviorToGrooveTransformer;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RuleGeneratorController {

    // One Method to generate the GG (+ download zip)
    @RequestMapping(value = "/zip", produces = "application/zip")
    public void generateGGAndReturnZIP(@RequestParam("file") MultipartFile file,
                                       HttpServletResponse response) throws IOException {

        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());
        String randomTempDir = System.getProperty("java.io.tmpdir") + UUID.randomUUID() + "/";

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(randomTempDir);
        transformer.generateGrooveGrammar(bpmnCollaboration, outputDir, false);

        // TODO: clean up. Make transformer return this dir.
        File resultDir = new File(outputDir.getAbsolutePath() + "/" + bpmnCollaboration.getName() + ".gps");
        // create a list to add files to be zipped
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

    // One method to generate the state space (and do model-checking).
}