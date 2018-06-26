package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OperatorRunner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProblemInstance problemInstance;
    private final DistanceHolder distanceHolder;
    private SolutionInstance bestSolution;

    public OperatorRunner(ProblemInstance problemInstance, DistanceHolder distanceHolder, SolutionInstance bestSolution) {
        this.problemInstance = problemInstance;
        this.distanceHolder = distanceHolder;
        this.bestSolution = bestSolution;
    }


    public SolutionInstance runAlgorithm() {
        Map<Pair<Route, Route>, Boolean> hopeLess_exchange = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
        Map<Pair<Route, Route>, NewRoutes> alreadyComputed_exchange = new ConcurrentHashMap<>();
        Map<Pair<Route, Route>, Boolean> hopeLess_relocate = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
        Map<Pair<Route, Route>, NewRoutes> alreadyComputed_relocate = new ConcurrentHashMap<>();
        Map<Pair<Route, Route>, Boolean> hopeLess_cross = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
        Map<Pair<Route, Route>, NewRoutes> alreadyComputed_cross = new ConcurrentHashMap<>();


        double bestDistance = Double.MAX_VALUE;
        double currentDistance = bestSolution.getDistanceSum();
        while (currentDistance < bestDistance) {
            bestDistance = currentDistance;

            Optional<SolutionInstance> sol =
                    new Relocate(bestSolution, problemInstance, distanceHolder).optimize(hopeLess_relocate, alreadyComputed_relocate);
            if (sol.isPresent() && sol.get().getDistanceSum() < bestDistance) {
                logger.debug("relocate found new best solution");
                bestSolution = sol.get();
            }

            sol = new Exchange(bestSolution, problemInstance, distanceHolder).optimize(hopeLess_exchange, alreadyComputed_exchange);
            if (sol.isPresent() && sol.get().getDistanceSum() < bestDistance) {
                logger.debug("exchange found new best solution");
                bestSolution = sol.get();
            }

            sol = new CrossExchange(bestSolution, problemInstance, distanceHolder).optimize(hopeLess_cross, alreadyComputed_cross);
            if (sol.isPresent() && sol.get().getDistanceSum() < bestDistance) {
                logger.debug("cross exchange found new best solution");
                bestSolution = sol.get();
            }

            SolutionInstance instance = OptRunner.runOpts(bestSolution, problemInstance, distanceHolder);
            if (instance.getDistanceSum() < bestDistance) {
                logger.debug("opts found new best solution");
                bestSolution = instance;
            }
            currentDistance = bestSolution.getDistanceSum();
        }
        return bestSolution;
    }
}
