package at.ac.tuwien.otl.ss18.pwlk.verifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SolutionVerifier {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String jarPath;

    public SolutionVerifier() {
        this.jarPath = this.getClass().getClassLoader().getResource("verifier/evrptw-verifier-0.2.0.jar.verify").getPath();
    }

    public void verify(String pathToProblem, String pathToSolution) {
        try {
            Process proc = Runtime.getRuntime().exec("java -jar "
                    + jarPath
                    + " "
                    + this.getClass().getClassLoader().getResource(pathToProblem).getFile()
                    + " "
                    + pathToSolution);

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String errLine = err.readLine();
            while (errLine != null) {
                logger.error(errLine);
                errLine = err.readLine();
            }

            String inLine = in.readLine();
            while (inLine != null) {
                logger.error(inLine);
                inLine = in.readLine();
            }
        } catch (IOException e) {
            logger.error("Error occured while verifying solution: " + e);
        }
    }
}
