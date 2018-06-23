package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.EvrptwRunException;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;

import java.util.Optional;

public interface IConstructSolution {
  /**
   * Calculate a solution from the given problem instance within a specific timeout
   * @param problemInstance
   * @param timeout
   * @return
   */
  Optional<SolutionInstance> constructSolution(ProblemInstance problemInstance, int timeout, DistanceHolder distanceHolder) throws EvrptwRunException;
}
