package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.util.RouteFilter;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;

import java.util.Optional;

public class OptimizeSolutionStub extends AbstractOptimizeSolution {

  @Override
  Optional<SolutionInstance> runAlgorithm(
          SolutionInstance solutionInstance,
          ProblemInstance problemInstance,
          DistanceHolder distanceHolder) {


    SolutionInstance bestSolution = new SimulatedAnnealing().runAlgorithm(solutionInstance, problemInstance, distanceHolder).get();
    OperatorRunner runner = new OperatorRunner(problemInstance, distanceHolder, bestSolution);
    bestSolution = runner.runAlgorithm();

    RouteFilter.finalizeRoutes(bestSolution.getRoutes());
    return Optional.of(bestSolution);
  }


}
