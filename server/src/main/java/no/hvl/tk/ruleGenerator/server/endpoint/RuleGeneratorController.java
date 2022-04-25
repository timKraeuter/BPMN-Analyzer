package no.hvl.tk.ruleGenerator.server.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviorTransformer.BehaviorToGrooveTransformer;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RuleGeneratorController {
    private final String tempDir = System.getProperty("java.io.tmpdir") + "bpmnAnalyzerRules/";

    // One Method to generate the GG (+ download zip)
    @RequestMapping(value = "/zip", produces = "application/zip")
    @CrossOrigin(origins = "http://localhost:4200")
    public void generateGGAndReturnZIP(@RequestPart("file") MultipartFile file,
                                       HttpServletResponse response) throws IOException {
        File tempSubDir = deleteOldGGSAndCreateNewDir();

        File resultDir = generateGGForBPMNFile(file, tempSubDir);

        // Zip all files
        File[] allFiles = resultDir.listFiles();
        List<File> graphGrammarFiles = Arrays.asList(allFiles != null ? allFiles : new File[]{});
        zipAndReturnFiles(response, graphGrammarFiles);
    }

    private File generateGGForBPMNFile(MultipartFile file, File tempSubDir) throws IOException {
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        return transformer.generateGrooveGrammar(bpmnCollaboration, tempSubDir, false);
    }

    private File deleteOldGGSAndCreateNewDir() throws IOException {
        FileUtils.deleteDirectory(new File(tempDir));
        return new File(tempDir + UUID.randomUUID() + "/");
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