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
                                   String resultFilePath) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(

                "cmd.exe",
                "/c",
                String.format("cd \"%s\" && java -jar Generator.jar %s -o \"%s\"",
                              grooveBinDir,
                              graphGrammar,
                              resultFilePath));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        printOutput(p);
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
