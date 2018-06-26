package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.util.RouteFilter;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Car;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimulatedAnnealing extends AbstractOptimizeSolution {
    private static final int MAX_ITERATION = 500;
    private static final int PENALTY = 60;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ProblemInstance problemInstance;
    private DistanceHolder distanceHolder;

    @Override
    Optional<SolutionInstance> runAlgorithm(
            SolutionInstance solutionInstance,
            ProblemInstance problemInstance,
            DistanceHolder distanceHolder) {
        logger.info("Optimize solution with algorithm 'Simulated Annealing'");
        this.problemInstance = problemInstance;
        this.distanceHolder = distanceHolder;

        SolutionInstance bestSolution = solutionInstance;

        double temperature = 500;
        double cooling_factor = 0.98;


        Map<Pair<Route, Route>, Boolean> hopeLess_exchange = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
        Map<Pair<Route, Route>, NewRoutes> alreadyComputed_exchange = new ConcurrentHashMap<>();
        Map<Pair<Route, Route>, Boolean> hopeLess_relocate = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
        Map<Pair<Route, Route>, NewRoutes> alreadyComputed_relocate = new ConcurrentHashMap<>();
        Map<Pair<Route, Route>, Boolean> hopeLess_cross = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
        Map<Pair<Route, Route>, NewRoutes> alreadyComputed_cross = new ConcurrentHashMap<>();


        for (int i = 0; i < MAX_ITERATION; i++) {
            logger.info("Current iteration: " + i);

            bestSolution.setRoutes(RouteFilter.filterNodesInRoutes(bestSolution.getRoutes()));
            bestSolution = new ChargingChecker(problemInstance, distanceHolder, bestSolution).optimize();
            SolutionInstance solutionInstance1 = bestSolution.copy();
            for (Route route : solutionInstance1.getRoutes()) {
                route.setDistance(route.getDistance() + PENALTY);
            }
            Optional<SolutionInstance> sol =
                    new Relocate(solutionInstance1, problemInstance, distanceHolder).optimize(hopeLess_relocate, alreadyComputed_relocate);
            if (sol.isPresent()) {
                recalculateDistances(sol.get());
            }
            if (sol.isPresent() && sol.get().getDistanceSum() < bestSolution.getDistanceSum()) {
                logger.info("relocate found new best solution");
                bestSolution = sol.get();
            } else if (sol.isPresent() && temperature > 1) {
                double probability = 1 / (1 + Math.exp((bestSolution.getDistanceSum() - sol.get().getDistanceSum()) / temperature));
                if (Math.random() > probability) {
                    logger.info("relocate found new solution");
                    bestSolution = sol.get();
                }
            }


            solutionInstance1 = bestSolution.copy();
            for (Route route : solutionInstance1.getRoutes()) {
                route.setDistance(route.getDistance() + PENALTY);
            }

            sol = new Exchange(solutionInstance1, problemInstance, distanceHolder).optimize(hopeLess_exchange, alreadyComputed_exchange);
            if (sol.isPresent()) {
                recalculateDistances(sol.get());
            }
            if (sol.isPresent() && sol.get().getDistanceSum() < bestSolution.getDistanceSum()) {
                logger.info("exchange found new best solution");
                bestSolution = sol.get();
            } else if (sol.isPresent() && temperature > 1) {
                double probability = 1 / (1 + Math.exp((bestSolution.getDistanceSum() - sol.get().getDistanceSum()) / temperature));
                if (Math.random() > probability) {
                    logger.info("exchange found new solution");
                    bestSolution = sol.get();
                }
            }

            solutionInstance1 = bestSolution.copy();
            for (Route route : solutionInstance1.getRoutes()) {
                route.setDistance(route.getDistance() + PENALTY);
            }
            sol = new CrossExchange(solutionInstance1, problemInstance, distanceHolder).optimize(hopeLess_cross, alreadyComputed_cross);
            if (sol.isPresent()) {
                recalculateDistances(sol.get());
            }
            if (sol.isPresent() && sol.get().getDistanceSum() < bestSolution.getDistanceSum()) {
                logger.info("cross exchange found new best solution");
                bestSolution = sol.get();
            } else if (sol.isPresent() && temperature > 1) {
                double probability = 1 / (1 + Math.exp((bestSolution.getDistanceSum() - sol.get().getDistanceSum()) / temperature));
                if (Math.random() > probability) {
                    logger.info("cross exchange found new solution");
                    bestSolution = sol.get();
                }
            }

            SolutionInstance instance = OptRunner.runOpts(bestSolution, problemInstance, distanceHolder);
            if (instance.getDistanceSum() < bestSolution.getDistanceSum()) {
                logger.info("opts found new best solution");
                bestSolution = instance;
            }
            temperature *= cooling_factor;
            if (temperature <= 1) {
                temperature = 1;
            }

        }
        RouteFilter.finalizeRoutes(bestSolution.getRoutes());


        return Optional.of(bestSolution);
    }




    private void recalculateDistances(SolutionInstance solutionInstance) {
        for (Route route : solutionInstance.getRoutes()) {
            Car car = new Car(problemInstance, distanceHolder);
            try {
                car.driveRoute(route.getRoute());
            } catch (BatteryViolationException | TimewindowViolationException e) {
                logger.error("should not happen");
            }
            route.setDistance(car.getCurrentDistance());
        }
    }
}
