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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

import static java.lang.Math.toIntExact;

public class Evrptw {
  private final Logger logger = LoggerFactory.getLogger(getClass());


  // default instance path, can be overwritten with -f or -d
  private static String instanceDirectory = "instances/";
  private static String instanceName = "r102C10.txt";

  // timeout of 0 means no timeout
  private int timeout;
  // number of times construct or optimize algorithm should run (1 is default)
  private int nbConstruct;
  private int nbOptimize;
  private boolean optimize; // should optimization algorithm run?
  // solution verifier as jar program in tmp directory
  private SolutionVerifier solutionVerifier;

  private IConstructSolution constructSolution;
  private IOptimizeSolution optimizeSolution;

  private Map<String, ProblemInstance> problemInstances;

  public static Evrptw build(CommandLine cmd) throws EvrptwInitializeException {
    int timeout = 0;
    boolean optimize = false;
    int nbConstruct = 1;
    int nbOptimize = 1;
    SolutionVerifier solutionVerifier;
    String problemInstance;
    Map<String, ProblemInstance> problemInstances = new HashMap<>();

    if (cmd.hasOption("timeout")) {
      timeout = Integer.parseInt(cmd.getOptionValue("timeout"));

      if (timeout < 1) {
        throw new EvrptwInitializeException("Timeout for algorithms must be greater than 0");
      }
    }

    if (cmd.hasOption("runNumberConstruct")) {
      nbConstruct = Integer.parseInt(cmd.getOptionValue("runNumberConstruct"));

      if(nbConstruct < 1) {
        throw new EvrptwInitializeException("number of iterations of the construction algorithm must be greater than 0");
      }
    }

    if (cmd.hasOption("runNumberOptimize")) {
      nbOptimize = Integer.parseInt(cmd.getOptionValue("runNumberOptimize"));

      if(nbOptimize < 1) {
        throw new EvrptwInitializeException("number of iterations of the optimization algorithm must be greater than 0");
      }
    }

    if (cmd.hasOption("optimize")) {
      optimize = true;
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

    return new Evrptw(timeout, solutionVerifier, problemInstances, optimize, nbConstruct, nbOptimize);
  }

  private Evrptw(int timeout,
                 SolutionVerifier solutionVerifier,
                 Map<String, ProblemInstance> problemInstances,
                 boolean optimize,
                 int nbConstruct,
                 int nbOptimize) {

    this.problemInstances = new HashMap<>();
    this.timeout = timeout;
    this.solutionVerifier = solutionVerifier;
    this.problemInstances = problemInstances;
    this.constructSolution = new ConstructSolutionStub(); // choose algorithm to construct routes
    this.optimizeSolution = new OptimizeSolutionStub(); // choose algorithm to optimize routes
    this.optimize = optimize;
    this.nbConstruct = nbConstruct;
    this.nbOptimize = nbOptimize;
  }

  public void evrptwRun() {
    Report report = new Report(optimize);
    report.setConstructAlgorithmName(constructSolution.getClass().getSimpleName());
    report.setOptimizeAlgorithmName(optimizeSolution.getClass().getSimpleName());

    for(String instancePath : problemInstances.keySet()) {
      InstanceReport instanceReport = runInstance(instancePath, problemInstances.get(instancePath));
      report.setInstanceReport(instanceReport);
    }

    logger.info(report.toString());
  }

  private InstanceReport runInstance(String instancePath, ProblemInstance problemInstance) {

    String instanceName = instancePath.substring(instancePath.lastIndexOf("/")+1, instancePath.lastIndexOf("_"));

    InstanceReport instanceReport = new InstanceReport(instanceName);

    Optional<SolutionInstance> solutionInstance = Optional.empty();

    for(int i = 0; i<nbConstruct; i++) {
      // Construct solution with construction heuristic
      Instant begin = Instant.now();
      solutionInstance = constructSolution.constructSolution(problemInstance, timeout);
      Instant end = Instant.now();

      logger.info("Elapsed time: " + (end.getEpochSecond() - begin.getEpochSecond()) + " Seconds");
      instanceReport.addSolutionInstances(solutionInstance);

      if (!solutionInstance.isPresent()) {
        logger.info("Could not create solution within the time limit");
        instanceReport.addRunTimeConstruct(OptionalInt.empty());
      } else {
        instanceReport.addRunTimeConstruct(OptionalInt.of(toIntExact(end.getEpochSecond() - begin.getEpochSecond())));
      }
    }

    if (!solutionInstance.isPresent()) {
      logger.info("Cannot proceed with optimizing solution because there is not start solution");
      return instanceReport;
    }

    Optional<SolutionInstance> optimizedSolution = Optional.empty();

    if (optimize) {

      for (int i = 0; i < nbOptimize; i++) {
        // Optimize solution with metaheuristic
        Instant begin = Instant.now();
        optimizedSolution = optimizeSolution.optimizeSolution(solutionInstance.get(), timeout);
        Instant end = Instant.now();

        logger.info("Elapsed time: " + (end.getEpochSecond() - begin.getEpochSecond()) + " Seconds");
        instanceReport.addOptimizedInstances(optimizedSolution);

        if (!optimizedSolution.isPresent()) {
          logger.info("Could not create optimized solution within the time limit");
          instanceReport.addRunTimeOptimize(OptionalInt.empty());
        } else {
          instanceReport.addRunTimeOptimize(OptionalInt.of(toIntExact(end.getEpochSecond() - begin.getEpochSecond())));
        }
      }
    } else {
      optimizedSolution = solutionInstance;
    }

    if (!optimizedSolution.isPresent()) {
      logger.info("Cannot check for valid solution because there is no solution available");
      logger.info("Note: Check for invalid solutions are only made for the last run");
      return instanceReport;
    }

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
