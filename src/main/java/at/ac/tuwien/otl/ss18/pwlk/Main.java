package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl.ConstructSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.IOptimizeSolution;
import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.OptimizeSolutionStub;
import at.ac.tuwien.otl.ss18.pwlk.reader.ProblemReader;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import at.ac.tuwien.otl.ss18.pwlk.writer.SolutionWriter;

import java.io.File;
import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    final String instanceDirectory = "instances/";
    final String instanceFile = "r102C10";
    
    // Read problem from file
    final ProblemReader problemReader = new ProblemReader(instanceDirectory + instanceFile + ".txt");
    final ProblemInstance instance = problemReader.retrieveProblemInstance();

    // Construct solution with construction heuristic
    IConstructSolution constructSolution = new ConstructSolutionStub(); // choose algorithm to construct routes
    final SolutionInstance solutionInstance = constructSolution.constructSolution(instance);

    // Optimize solution with metaheuristic
    IOptimizeSolution optimizeSolution = new OptimizeSolutionStub(); // choose algorithm to optimize routes
    final SolutionInstance optimizedSolution = optimizeSolution.optimizeSolution(solutionInstance);

    // Write solution to problem to file
    final SolutionWriter solutionWriter = new SolutionWriter(File.createTempFile(instanceFile +".solution", ".tmp").getAbsolutePath(), instanceFile);
    solutionWriter.write(optimizedSolution);
  }
}
