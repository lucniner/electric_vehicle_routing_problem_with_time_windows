package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl.ConstructSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.IOptimizeSolution;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl.OptimizeSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.reader.ProblemReader;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import at.ac.tuwien.otl.ss18.pwlk.verifier.SolutionVerifier;
import at.ac.tuwien.otl.ss18.pwlk.writer.SolutionWriter;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class Main {
  private final Logger logger = LoggerFactory.getLogger(getClass());


  // default instance path, can be overwritten with -f
  private static String instancePath = "instances/r102C10.txt";

  // timeout of 0 means no timeout
  private static int timeout = 0;
  // solution verifier as jar program in tmp directory
  private static SolutionVerifier solutionVerifier;


  public static void main(String[] args) {
    CommandLine cmd = parseArguments(args);
    new Main().execute(cmd);
    System.exit(0);
  }

  private void execute(CommandLine cmd) {
    try {
      solutionVerifier = SolutionVerifier.build();
    } catch (IOException e) {
      logger.error("Exception occured while loading verifier: " + e);
      return;
    }

    final String problemInstance;

    try {
      if (cmd.hasOption("file")) {
        problemInstance = cmd.getOptionValue("file");
      } else {
        problemInstance = loadProblemFile(instancePath);
      }
    } catch (IOException e) {
      logger.error("Could not load problem instance: " + e);
      return;
    }

    // Read problem from file
    final ProblemReader problemReader = new ProblemReader(problemInstance);

    final ProblemInstance instance;

    try {
      instance = problemReader.retrieveProblemInstance();
    } catch (IOException e) {
      logger.error("Could not retrieve problem instance: " + e);
      return;
    }


    // Construct solution with construction heuristic
    IConstructSolution constructSolution = new ConstructSolutionStub(); // choose algorithm to construct routes
    final Optional<SolutionInstance> solutionInstance = constructSolution.constructSolution(instance, timeout);

    if (!solutionInstance.isPresent()) {
      logger.info("Could not create solution within the time limit");
      return;
    }

    // Optimize solution with metaheuristic
    IOptimizeSolution optimizeSolution = new OptimizeSolutionStub(); // choose algorithm to optimize routes
    final Optional<SolutionInstance> optimizedSolution = optimizeSolution.optimizeSolution(solutionInstance.get(), timeout);

    final String tempSolutionFile;

    try {
      tempSolutionFile = File.createTempFile(
              problemInstance.substring(problemInstance.lastIndexOf("/")+1,
                      problemInstance.lastIndexOf("_")) +".solution", ".tmp").getAbsolutePath();
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
            problemInstance.substring(problemInstance.lastIndexOf("/")+1,
                    problemInstance.lastIndexOf("_")));
    solutionWriter.write(optimizedSolution.get());

    // Verify solution with the given verifier java program
    solutionVerifier.verify(problemInstance, tempSolutionFile);
  }

  private static CommandLine parseArguments(String[] args) {
    CommandLine cmd = null;

    Options options = new Options();
    Option help = new Option( "h", "help", false, "print this message" );
    Option input = new Option("t", "timeout", true, "timeout for algorithm");
    Option file = new Option("f", "file", true, "specify file for input problem");

    options.addOption(help);
    options.addOption(input);
    options.addOption(file);

    try {
      cmd = new DefaultParser().parse(options, args, false);
    } catch (ParseException e) {
      System.out.println(e.getMessage() + "\n");
      new HelpFormatter().printHelp("java -jar <name>", options, true);
      System.exit(0);
    }

    if (cmd.hasOption("help")) {
      new HelpFormatter().printHelp("java -jar <name>", options, true);
      System.exit(0);
    }

    if (cmd.hasOption("timeout")) {
      timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
    }

    return cmd;
  }

  private String loadProblemFile(String pathToProblem) throws IOException {
    InputStream problemInstance = this.getClass().getClassLoader().getResourceAsStream(pathToProblem);
    Path problemDestination = File.createTempFile(
            pathToProblem.substring(pathToProblem.lastIndexOf("/"),
                    pathToProblem.length()-4) + "_", ".txt").toPath();
    Files.copy(problemInstance, problemDestination, StandardCopyOption.REPLACE_EXISTING);

    return problemDestination.toString();
  }
}
