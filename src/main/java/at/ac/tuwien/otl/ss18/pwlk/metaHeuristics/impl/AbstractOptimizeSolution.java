package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.IOptimizeSolution;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractOptimizeSolution implements IOptimizeSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Optional<SolutionInstance> optimizeSolution(SolutionInstance solutionInstance, int timeout) {
    SimpleTimeLimiter limiter = SimpleTimeLimiter.create(Executors.newCachedThreadPool());

    Optional<SolutionInstance> optimizedSolutionInstance = Optional.empty();

    Instant begin = Instant.now();

    try {
      if (timeout != 0) {
        optimizedSolutionInstance = limiter.callWithTimeout(() -> runAlgorithm(solutionInstance), timeout, TimeUnit.SECONDS);
      } else { // if timeout is zero, then timeout is deactivated
        optimizedSolutionInstance = runAlgorithm(solutionInstance);
      }

    } catch (ExecutionException | InterruptedException e) {
      logger.error("Error occured during optimization algorithm: " + e);
    } catch (TimeoutException t) {
      logger.info("Finishing algorithm within time limit of " + timeout  + " " + TimeUnit.SECONDS.name() + " was not possible");
    }

    Instant end = Instant.now();

    logger.info("Elapsed time: " + (end.getEpochSecond() - begin.getEpochSecond()) + " Seconds");
    return optimizedSolutionInstance;
  }

  abstract Optional<SolutionInstance> runAlgorithm(SolutionInstance solutionInstance);
}
