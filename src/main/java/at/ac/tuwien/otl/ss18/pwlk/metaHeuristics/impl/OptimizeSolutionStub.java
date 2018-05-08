package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class OptimizeSolutionStub extends AbstractOptimizeSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Optional<SolutionInstance> optimizeSolution(SolutionInstance solutionInstance, int timeout) {
    logger.info("Optimize solution with algorithm 'Stub'");
    return super.optimizeSolution(solutionInstance, timeout);
  }

  @Override
  Optional<SolutionInstance> runAlgorithm(SolutionInstance solutionInstance) {
    return Optional.of(solutionInstance);
  }
}
