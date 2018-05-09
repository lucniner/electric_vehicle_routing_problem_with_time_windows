package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl.ConstructSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.EvrptwInitializeException;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.IOptimizeSolution;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl.OptimizeSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.reader.ProblemReader;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import at.ac.tuwien.otl.ss18.pwlk.verifier.SolutionVerifier;
import at.ac.tuwien.otl.ss18.pwlk.writer.SolutionWriter;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Evrptw {
  private final Logger logger = LoggerFactory.getLogger(getClass());


  // default instance path, can be overwritten with -f
  private static String instancePath = "instances/r102C10.txt";

  // timeout of 0 means no timeout
  private int timeout = 0;
  // solution verifier as jar program in tmp directory
  private SolutionVerifier solutionVerifier;

  private Map<String, ProblemInstance> problemInstances;

  public static Evrptw build(CommandLine cmd) throws EvrptwInitializeException {
    int timeout = 0;
    SolutionVerifier solutionVerifier;
    String problemInstance;
    Map<String, ProblemInstance> problemInstances = new HashMap<>();

    if (cmd.hasOption("timeout")) {
      timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
    }
    if (cmd.hasOption("file")) {
      problemInstance = cmd.getOptionValue("file");
    } else {
      try {
        problemInstance = loadProblemFile(instancePath);
      } catch (IOException e) {
        throw new EvrptwInitializeException("Could not load problem instance from resource directory: " + e.getLocalizedMessage());
      }
    }
    final ProblemReader problemReader = new ProblemReader(problemInstance);
    try {
      problemInstances.put(problemInstance, problemReader.retrieveProblemInstance());
    } catch (IOException e) {
      throw new EvrptwInitializeException("Exception while retrieving problem instance from file: " + e);
    }

    try {
      solutionVerifier = SolutionVerifier.build();
    } catch (IOException e) {
      throw new EvrptwInitializeException("Exception while loading solution verifier: " + e);
    }

    return new Evrptw(timeout, solutionVerifier, problemInstances);
  }

  private Evrptw(int timeout, SolutionVerifier solutionVerifier, Map<String, ProblemInstance> problemInstances) {
    this.problemInstances = new HashMap<>();
    this.timeout = timeout;
    this.solutionVerifier = solutionVerifier;
    this.problemInstances = problemInstances;
  }


  public void evrptwRun() {
    for(String instancePath : problemInstances.keySet()) {
      runInstance(instancePath, problemInstances.get(instancePath));
    }
  }

  private void runInstance(String instancePath, ProblemInstance problemInstance) {
    // Construct solution with construction heuristic
    IConstructSolution constructSolution = new ConstructSolutionStub(); // choose algorithm to construct routes
    final Optional<SolutionInstance> solutionInstance = constructSolution.constructSolution(problemInstance, timeout);

    if (!solutionInstance.isPresent()) {
      logger.info("Could not create solution within the time limit");
      return;
    }

    // Optimize solution with metaheuristic
    IOptimizeSolution optimizeSolution = new OptimizeSolutionStub(); // choose algorithm to optimize routes
    final Optional<SolutionInstance> optimizedSolution = optimizeSolution.optimizeSolution(solutionInstance.get(), timeout);

    final String tempSolutionFile;

    try {
      File tempSolution = File.createTempFile(
              instancePath.substring(instancePath.lastIndexOf("/")+1,
                      instancePath.lastIndexOf("_")) +".solution", ".tmp");
      tempSolution.deleteOnExit();
      tempSolutionFile = tempSolution.getAbsolutePath();
    } catch (IOException e) {
      logger.error("Could not create temporary file to save solution " + e);
      return;
    }

    if (!optimizedSolution.isPresent()) {
      logger.info("Could not create optimized solution within the time limit");
      return;
    }

    // Write solution to problem to file
    final SolutionWriter solutionWriter = new SolutionWriter(
            tempSolutionFile,
            instancePath.substring(instancePath.lastIndexOf("/")+1,
                    instancePath.lastIndexOf("_")));
    solutionWriter.write(optimizedSolution.get());

    // Verify solution with the given verifier java program
    solutionVerifier.verify(instancePath, tempSolutionFile);

  }

  private static String loadProblemFile(String pathToProblem) throws IOException {
    InputStream problemInstance = Evrptw.class.getClassLoader().getResourceAsStream(pathToProblem);
    File problemFile = File.createTempFile(
            pathToProblem.substring(pathToProblem.lastIndexOf("/"),
                    pathToProblem.length()-4) + "_", ".txt");
    problemFile.deleteOnExit();
    Path problemDestination = problemFile.toPath();
    Files.copy(problemInstance, problemDestination, StandardCopyOption.REPLACE_EXISTING);

    return problemDestination.toString();
  }
}
