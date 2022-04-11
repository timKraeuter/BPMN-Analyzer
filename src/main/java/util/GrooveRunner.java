package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GrooveRunner {
    private final String grooveBinDir;

    public GrooveRunner(String grooveBinDir) {

        this.grooveBinDir = grooveBinDir;
    }

    public File generateStateSpace(String graphGrammar,
                                   String resultFilePath,
                                   boolean printOutput) throws IOException, InterruptedException {
        // java -jar GraphGrammarPath -o "StateSpaceFilePath"
        ProcessBuilder builder = new ProcessBuilder("java",
                                                    "-jar",
                                                    grooveBinDir + "\\Generator.jar",
                                                    graphGrammar,
                                                    "-o",
                                                    String.format("\"%s\"", resultFilePath));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        p.waitFor();
        if (printOutput) {
            printOutput(p);
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
