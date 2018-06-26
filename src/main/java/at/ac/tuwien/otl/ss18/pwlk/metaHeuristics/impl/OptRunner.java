package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OptRunner {

    public static SolutionInstance runOpts(
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
