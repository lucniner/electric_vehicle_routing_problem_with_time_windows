package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OptimizeSolutionStub extends AbstractOptimizeSolution {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    Optional<SolutionInstance> runAlgorithm(SolutionInstance solutionInstance, ProblemInstance problemInstance, DistanceHolder distanceHolder) {
        logger.info("Optimize solution with algorithm 'Stub'");

        SolutionInstance optimizedSolution = new SolutionInstance();
        final List<Route> optimizedRoutes = new ArrayList<>();

        solutionInstance.getRoutes().forEach(r -> {
            BestOrOptExchange exchange = new BestOrOptExchange(r, problemInstance, distanceHolder);
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

        optimizedSolution.getRoutes().forEach(r -> {
          BestTwoOptExchagne exchange = new BestTwoOptExchagne(r, problemInstance, distanceHolder);
            final Optional<Route> route = exchange.optimizeRoute();
            if (route.isPresent()) {
                bestRoutes.add(route.get());
            } else {
                bestRoutes.add(r);
            }
        });

        bestSolution.setRoutes(bestRoutes);

        Optional<SolutionInstance> sol = new Relocate(bestSolution, problemInstance, distanceHolder).optimize();
        if (sol.isPresent()) {
            bestSolution = sol.get();
        }

        return Optional.of(bestSolution);
    }
}
