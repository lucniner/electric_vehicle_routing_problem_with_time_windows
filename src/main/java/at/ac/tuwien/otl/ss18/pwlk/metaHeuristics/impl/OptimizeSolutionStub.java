package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OptimizeSolutionStub extends AbstractOptimizeSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  Optional<SolutionInstance> runAlgorithm(
          SolutionInstance solutionInstance,
          ProblemInstance problemInstance,
          DistanceHolder distanceHolder) {

    Map<Pair<Route, Route>, Boolean> hopeLess_exchange = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
    Map<Pair<Route, Route>, NewRoutes> alreadyComputed_exchange = new ConcurrentHashMap<>();
    Map<Pair<Route, Route>, Boolean> hopeLess_relocate = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
    Map<Pair<Route, Route>, NewRoutes> alreadyComputed_relocate = new ConcurrentHashMap<>();
    Map<Pair<Route, Route>, Boolean> hopeLess_cross = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
    Map<Pair<Route, Route>, NewRoutes> alreadyComputed_cross = new ConcurrentHashMap<>();

    solutionInstance = new SimulatedAnnealing().runAlgorithm(solutionInstance, problemInstance, distanceHolder).get();
    logger.info("Optimize solution with algorithm 'Stub'");

    SolutionInstance bestSolution = runOpts(solutionInstance, problemInstance, distanceHolder);

    double bestDistance = Double.MAX_VALUE;
    double currentDistance = bestSolution.getDistanceSum();
    while (currentDistance < bestDistance) {
      bestDistance = currentDistance;

      Optional<SolutionInstance> sol =
              new Relocate(bestSolution, problemInstance, distanceHolder).optimize(hopeLess_relocate, alreadyComputed_relocate);
      if (sol.isPresent() && sol.get().getDistanceSum() < bestDistance) {
        logger.info("relocate found new best solution");
        bestSolution = sol.get();
      }

      sol = new Exchange(bestSolution, problemInstance, distanceHolder).optimize(hopeLess_exchange, alreadyComputed_exchange);
      if (sol.isPresent() && sol.get().getDistanceSum() < bestDistance) {
        logger.info("exchange found new best solution");
        bestSolution = sol.get();
      }

      sol = new CrossExchange(bestSolution, problemInstance, distanceHolder).optimize(hopeLess_cross, alreadyComputed_cross);
      if (sol.isPresent() && sol.get().getDistanceSum() < bestDistance) {
        logger.info("cross exchange found new best solution");
        bestSolution = sol.get();
      }

      currentDistance = bestSolution.getDistanceSum();
      SolutionInstance instance = runOpts(bestSolution, problemInstance, distanceHolder);
      if (instance.getDistanceSum() < bestDistance) {
        logger.info("opts found new best solution");
        bestSolution = instance;
      }
    }
    finalizeRoutes(bestSolution.getRoutes());

    return Optional.of(bestSolution);
  }

  private void finalizeRoutes(final List<Route> routes) {
    int i = 0;
    for (final Route r : new ArrayList<>(routes)) {
      if (r.getRoute().size() == 2) {
        routes.remove(i--);
      }
      i++;
    }
  }

  private SolutionInstance runOpts(
          final SolutionInstance solutionInstance,
          ProblemInstance problemInstance,
          DistanceHolder distanceHolder) {
    SolutionInstance optimizedSolution = new SolutionInstance();
    final List<Route> optimizedRoutes = new ArrayList<>();

    solutionInstance
            .getRoutes()
            .forEach(
                    r -> {
                      BestOrOptExchange exchange =
                              new BestOrOptExchange(r, problemInstance, distanceHolder);
                      final Optional<Route> route = exchange.optimizeRoute();
                      if (route.isPresent()) {
                optimizedRoutes.add(route.get());
                      } else {
                optimizedRoutes.add(r);
                      }
                    });

    optimizedSolution.setRoutes(optimizedRoutes);

    SolutionInstance bestSolution = new SolutionInstance();

    final List<Route> bestRoutes = new ArrayList<>();

    optimizedSolution
            .getRoutes()
            .forEach(
                    r -> {
                      BestTwoOptExchagne exchange =
                              new BestTwoOptExchagne(r, problemInstance, distanceHolder);
                      final Optional<Route> route = exchange.optimizeRoute();
                      if (route.isPresent()) {
                bestRoutes.add(route.get());
                      } else {
                bestRoutes.add(r);
                      }
                    });
    bestSolution.setRoutes(bestRoutes);
    return bestSolution;
  }
}
