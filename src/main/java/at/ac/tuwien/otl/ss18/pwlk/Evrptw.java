package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl.ConstructSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.EvrptwInitializeException;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.IOptimizeSolution;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl.OptimizeSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.reader.ProblemReader;
import at.ac.tuwien.otl.ss18.pwlk.report.InstanceReport;
import at.ac.tuwien.otl.ss18.pwlk.report.Report;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import at.ac.tuwien.otl.ss18.pwlk.verifier.SolutionVerifier;
import at.ac.tuwien.otl.ss18.pwlk.writer.SolutionWriter;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

import static java.lang.Math.toIntExact;

public class Evrptw {
  private final Logger logger = LoggerFactory.getLogger(getClass());


  // default instance path, can be overwritten with -f
  private static String instanceDirectory = "instances/";
  private static String instanceName = "r102C10.txt";

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

    if (!cmd.hasOption("directory")) {
      if (cmd.hasOption("file")) {
        problemInstance = cmd.getOptionValue("file");
        problemInstance = loadProblemFile(problemInstance, false);
      } else {
        problemInstance = loadProblemFile(instanceDirectory + instanceName, true);
      }
      try {
        problemInstances.put(problemInstance, new ProblemReader(problemInstance).retrieveProblemInstance());
      } catch (IOException e) {
        throw new EvrptwInitializeException("Exception while retrieving problem instance from file: " + e);
      }
    } else {
      try {
        File[] files = new File(cmd.getOptionValue("directory")).listFiles();

        if (files == null) {
          throw new EvrptwInitializeException("Could not read from specified directory");
        }

        for (File file : files) {
          if (file.isFile()) {
            problemInstance = loadProblemFile(file.getAbsolutePath(), false);
            problemInstances.put(problemInstance, new ProblemReader(problemInstance).retrieveProblemInstance());
          }
        }
      }catch (IOException e) {
        throw new EvrptwInitializeException("Could not read all files from specified directory: " + e);
      }
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
    IConstructSolution constructSolution = new ConstructSolutionStub(); // choose algorithm to construct routes
    IOptimizeSolution optimizeSolution = new OptimizeSolutionStub(); // choose algorithm to optimize routes
    //TODO noch so machen dass nicht alles so oft ausgeführt wird
    Report report = new Report();
    report.setConstructAlgorithmName(constructSolution.getClass().getSimpleName());
    report.setOptimizeAlgorithmName(constructSolution.getClass().getSimpleName());

    for(String instancePath : problemInstances.keySet()) {
      InstanceReport instanceReport = runInstance(instancePath,
              problemInstances.get(instancePath),
              constructSolution,
              optimizeSolution);

      report.setInstanceReport(instanceReport);
    }

    logger.info(report.toString());
  }

  private InstanceReport runInstance(String instancePath,
                                     ProblemInstance problemInstance,
                                     IConstructSolution constructSolution,
                                     IOptimizeSolution optimizeSolution) {

    String instanceName = instancePath.substring(instancePath.lastIndexOf("/")+1, instancePath.lastIndexOf("_"));

    InstanceReport instanceReport = new InstanceReport(instanceName);

    // Construct solution with construction heuristic
    Instant begin = Instant.now();
    final Optional<SolutionInstance> solutionInstance = constructSolution.constructSolution(problemInstance, timeout);
    Instant end = Instant.now();

    logger.info("Elapsed time: " + (end.getEpochSecond() - begin.getEpochSecond()) + " Seconds");

    if (!solutionInstance.isPresent()) {
      logger.info("Could not create solution within the time limit");
      instanceReport.addRunTimeConstruct(OptionalInt.empty());
      return instanceReport;
    }
    instanceReport.addRunTimeConstruct(OptionalInt.of(toIntExact(end.getEpochSecond() - begin.getEpochSecond())));


    // Optimize solution with metaheuristic
    begin = Instant.now();
    final Optional<SolutionInstance> optimizedSolution = optimizeSolution.optimizeSolution(solutionInstance.get(), timeout);
    end = Instant.now();

    logger.info("Elapsed time: " + (end.getEpochSecond() - begin.getEpochSecond()) + " Seconds");

    if (!optimizedSolution.isPresent()) {
      logger.info("Could not create optimized solution within the time limit");
      instanceReport.addRunTimeOptimize(OptionalInt.empty());
      return instanceReport;
    }
    instanceReport.addRunTimeOptimize(OptionalInt.of(toIntExact(end.getEpochSecond() - begin.getEpochSecond())));

    final String tempSolutionFile;

    try {
      File tempSolution = File.createTempFile(
              instanceName +".solution", ".tmp");
      tempSolution.deleteOnExit();
      tempSolutionFile = tempSolution.getAbsolutePath();
    } catch (IOException e) {
      logger.error("Could not create temporary file to save solution " + e);
      return instanceReport;
    }

    // Write solution to problem to file
    final SolutionWriter solutionWriter = new SolutionWriter(
            tempSolutionFile,
            instanceName);
    solutionWriter.write(optimizedSolution.get());

    // Verify solution with the given verifier java program
    solutionVerifier.verify(instancePath, tempSolutionFile);

    return instanceReport;
  }

  private static String loadProblemFile(String pathToProblem, boolean fromResourceDirectory) throws EvrptwInitializeException {
    try {
      final InputStream problemInstance;
      if (fromResourceDirectory) {
        problemInstance = Evrptw.class.getClassLoader().getResourceAsStream(pathToProblem);
      } else {
        problemInstance = new FileInputStream(pathToProblem);
      }

      File problemFile = File.createTempFile(
              pathToProblem.substring(pathToProblem.lastIndexOf("/"),
                      pathToProblem.length() - 4) + "_", ".txt");
      problemFile.deleteOnExit();
      Path problemDestination = problemFile.toPath();
      Files.copy(problemInstance, problemDestination, StandardCopyOption.REPLACE_EXISTING);

      return problemDestination.toString();
    } catch (IOException e) {
      throw new EvrptwInitializeException("Could not load problem instance from resource directory: " + e.getLocalizedMessage());
    }
  }
}
