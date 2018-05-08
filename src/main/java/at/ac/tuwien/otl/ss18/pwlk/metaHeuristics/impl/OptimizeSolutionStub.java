package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.IOptimizeSolution;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizeSolutionStub implements IOptimizeSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public SolutionInstance optimizeSolution(SolutionInstance solutionInstance) {
    logger.info("Optimize solution with algorithm 'Stub'");
    return solutionInstance;
  }
}
