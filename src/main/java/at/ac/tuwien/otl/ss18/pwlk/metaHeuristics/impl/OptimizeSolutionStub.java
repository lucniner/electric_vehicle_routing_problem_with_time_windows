package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class OptimizeSolutionStub extends AbstractOptimizeSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  Optional<SolutionInstance> runAlgorithm(SolutionInstance solutionInstance) {
    logger.info("Optimize solution with algorithm 'Stub'");
    //TODO richtigen algorithmus implementieren
    return Optional.of(solutionInstance);
  }
}
