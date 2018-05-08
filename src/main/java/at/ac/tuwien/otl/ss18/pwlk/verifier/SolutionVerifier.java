package at.ac.tuwien.otl.ss18.pwlk.verifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

public class SolutionVerifier {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private String jarPath;

  public SolutionVerifier() {
    try {
      InputStream verifierExecutable = this.getClass().getClassLoader().getResourceAsStream("verifier/evrptw-verifier-0.2.0.jar.verify");
      Path jarDestination = File.createTempFile("verifier", ".jar").toPath();
      Files.copy(verifierExecutable, jarDestination, StandardCopyOption.REPLACE_EXISTING);

      jarPath = jarDestination.toString();

    } catch (IOException i) {
      logger.error("Exception occured while loading verifier: " + i);
    }
  }

  public void verify(String pathToProblem, String pathToSolution) {
    try {
      InputStream problemInstance = this.getClass().getClassLoader().getResourceAsStream(pathToProblem);
      Path problemDestination = File.createTempFile("problem", ".txt").toPath();
      Files.copy(problemInstance, problemDestination, StandardCopyOption.REPLACE_EXISTING);

      Process proc = Runtime.getRuntime().exec("java -jar "
              + jarPath
              + " "
              + problemDestination.toString()
              + " "
              + pathToSolution);

      BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

      String error = err.lines().collect(Collectors.joining("\n"));
      if (error.length() != 0) {
        logger.error(error);
      }

      String output = in.lines().collect(Collectors.joining("\n"));
      if (output.length() != 0) {
        logger.info(output);
      }
    } catch (IOException e) {
      logger.error("Error occured while verifying solution: " + e);
    }
  }
}
