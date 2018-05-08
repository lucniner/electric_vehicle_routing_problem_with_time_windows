package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;

import java.util.Optional;

public interface IOptimizeSolution {
  /**
   * Optimize an existing solution within a specific timeout
   * @param solutionInstance
   * @param timeout
   * @return
   */
  Optional<SolutionInstance> optimizeSolution(SolutionInstance solutionInstance, int timeout);
}
