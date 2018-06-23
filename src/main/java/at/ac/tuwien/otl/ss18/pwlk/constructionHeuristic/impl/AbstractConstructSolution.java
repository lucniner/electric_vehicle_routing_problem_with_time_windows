package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.EvrptwRunException;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractConstructSolution implements IConstructSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Optional<SolutionInstance> constructSolution(ProblemInstance problemInstance, int timeout, DistanceHolder distanceHolder) throws EvrptwRunException {
    SimpleTimeLimiter limiter = SimpleTimeLimiter.create(Executors.newCachedThreadPool());

    Optional<SolutionInstance> solutionInstance = Optional.empty();

    try {
      if (timeout != 0) {
        solutionInstance = limiter.callWithTimeout(() -> runAlgorithm(problemInstance, distanceHolder), timeout, TimeUnit.SECONDS);
      } else { // if timeout is zero, then timeout is deactivated
        solutionInstance = runAlgorithm(problemInstance, distanceHolder);
      }

    } catch (ExecutionException | InterruptedException e) {
      logger.error("Error occured during construction algorithm: " + e);
    } catch (TimeoutException t) {
      logger.info("Finishing algorithm within time limit of " + timeout  + " " + TimeUnit.SECONDS.name() + " was not possible");
    }

    return solutionInstance;
  }

  abstract Optional<SolutionInstance> runAlgorithm(ProblemInstance problemInstance, DistanceHolder distanceHolder) throws EvrptwRunException;
}
