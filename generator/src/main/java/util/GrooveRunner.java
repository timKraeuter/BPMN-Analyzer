package util;

import java.io.*;
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
        if (printOutput) {
            new Thread(() -> printOutput(process)).start();
        }
        process.waitFor(60, TimeUnit.SECONDS);
        process.destroy(); // no op if already stopped.
        process.waitFor();
        return new File(resultFilePath);
    }

    private void printOutput(Process p) {
        try {
            byte[] buffer = new byte[1024];
            for (int length; (length = p.getInputStream().read(buffer)) != -1; ) {
                System.out.write(buffer, 0, length);
            }
        }
        catch (IOException e) {
            System.out.println("Process output could not be read! " + e.getMessage());
        }
    }
}
