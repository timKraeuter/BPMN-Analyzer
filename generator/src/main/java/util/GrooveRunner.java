package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class GrooveRunner {
    private static final String defaultGrooveDir = "groove/bin";
    private final String grooveBinDir;

    public GrooveRunner() {
        this(defaultGrooveDir);
    }

    public GrooveRunner(String grooveBinDir) {
        this.grooveBinDir = grooveBinDir;
    }

    public File generateStateSpace(String graphGrammar,
                                   String resultFilePath,
                                   boolean printOutput) throws IOException, InterruptedException {
        // java -jar GraphGrammarPath -o StateSpaceFilePath
        ProcessBuilder builder = new ProcessBuilder("java",
                                                    "-jar",
                                                    grooveBinDir + "/Generator.jar",
                                                    graphGrammar,
                                                    "-o",
                                                    resultFilePath);

        builder.redirectErrorStream(true);
        Process process = builder.start();
        process.waitFor(60, TimeUnit.SECONDS);
        process.destroy(); // no op if already stopped.
        process.waitFor();
        if (printOutput) {
            printOutput(process);
        }
        return new File(resultFilePath);
    }

    private void printOutput(Process p) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {

            byte[] buffer = new byte[1024];
            for (int length; (length = p.getInputStream().read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
        }
        catch (IOException e) {
            System.out.println("Process output could not be read! " + e.getMessage());
        }
        System.out.println(result.toString(StandardCharsets.UTF_8));
    }
}
