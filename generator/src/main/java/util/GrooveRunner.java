package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private void printOutput(Process p) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
    }
}
