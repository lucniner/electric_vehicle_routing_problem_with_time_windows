package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Relocate {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private SolutionInstance solutionInstance;
  private ProblemInstance problemInstance;
  private DistanceHolder distanceHolder;

  private Map hopeLessRelocate;
  private Map<Pair<Route, Route>, NewRoutes> alreadyComputed;

  private class NewRoutes {
    private double saving;
    private Route route1;
    private Route route2;

    NewRoutes(double saving, Route route1, Route route2) {
      this.saving = saving;
      this.route1 = route1;
      this.route2 = route2;
    }
  }

  public Relocate(SolutionInstance solutionInstance, ProblemInstance problemInstance, DistanceHolder distanceHolder) {
    this.solutionInstance = solutionInstance;
    this.problemInstance = problemInstance;
    this.distanceHolder = distanceHolder;
  }


  public Optional<SolutionInstance> optimize() {
    SolutionInstance bestSolutionInstance = solutionInstance;
    SolutionInstance currSolutionInstance = solutionInstance.copy();

    hopeLessRelocate = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
    alreadyComputed = new ConcurrentHashMap<>();

    Map<Pair<Route, Route>, NewRoutes> savings = calculateSavingsValue(currSolutionInstance);
    while (!savings.isEmpty()) {
      Map.Entry<Pair<Route, Route>, NewRoutes> bestSaving = null;

      for (Map.Entry<Pair<Route, Route>, NewRoutes> saving : savings.entrySet()) {
        if (bestSaving == null) {
          bestSaving = saving;
        } else {
          double value_saving = saving.getValue().saving;
          double value_best = bestSaving.getValue().saving;

          if (value_best < value_saving) {
            bestSaving = saving;
          }
        }
      }

      logger.debug("Relocate customer");

      List<Route> routeList = currSolutionInstance.getRoutes();
      routeList.add(bestSaving.getValue().route1);
      routeList.add(bestSaving.getValue().route2);
      routeList.remove(bestSaving.getKey().getKey());
      routeList.remove(bestSaving.getKey().getValue());

      savings = calculateSavingsValue(currSolutionInstance);
    }

    if (currSolutionInstance.getDistanceSum() < bestSolutionInstance.getDistanceSum()) {
      bestSolutionInstance = currSolutionInstance;
      return Optional.of(bestSolutionInstance);
    } else {
      return Optional.empty();
    }
  }


  private Map<Pair<Route, Route>, NewRoutes> calculateSavingsValue(SolutionInstance currSolution) {
    Map<Pair<Route, Route>, NewRoutes> savings = new ConcurrentHashMap<>();

    currSolution.getRoutes().parallelStream().forEach((route1) -> {
      for (final Route route2 : currSolution.getRoutes()) {
        if ((!route1.equals(route2))) {
          if (!hopeLessRelocate.containsKey(new Pair<Route, Route>(route1, route2))) {
            boolean hopeless = true;
            if (alreadyComputed.containsKey(new Pair<Route, Route>(route1, route2))) {
              NewRoutes newRoutes = alreadyComputed.get(new Pair<Route, Route>(route1, route2));
              savings.put(new Pair<Route, Route>(route1, route2), newRoutes);
              hopeless = false;
            } else {
              Optional<NewRoutes> newRoute = relocateNode(route1, route2);
              if (newRoute.isPresent()) {
                alreadyComputed.put(new Pair<Route, Route>(route1, route2), newRoute.get());
                savings.put(new Pair<Route, Route>(route1, route2), newRoute.get());
                hopeless = false;
              }
            }
            if (hopeless) {
              hopeLessRelocate.put(new Pair<Route, Route>(route1.copyRoute(), route2.copyRoute()), true);
            }
          }
        }
      }
    });
    return savings;
  }

  private Optional<NewRoutes> relocateNode(Route route1, Route route2) {
    List<AbstractNode> fromRoute = route1.getRoute();
    List<AbstractNode> toRoute = route2.getRoute();

    Optional<NewRoutes> bestRoutes = Optional.empty();

    for (int i=1; i<fromRoute.size()-1; i++) {
      for (int j=1; j<toRoute.size(); j++) {
        List<AbstractNode> from = new ArrayList<>(fromRoute);
        List<AbstractNode> to = new ArrayList<>(toRoute);
        AbstractNode node = from.remove(i);
        to.add(j, node);

        if (route2.getDemandOfRoute() + node.getDemand() > problemInstance.getLoadCapacity()) {
          continue;
        }

        Car car1 = new Car(problemInstance, distanceHolder);
        Car car2 = new Car(problemInstance, distanceHolder);
        try {
          car2.driveRoute(to);
          car1.driveRoute(from);

          if ((car1.getCurrentDistance() + car2.getCurrentDistance()) < (route1.getDistance() + route2.getDistance())) {
            double saving = route1.getDistance() + route2.getDistance() - car1.getCurrentDistance() - car2.getCurrentDistance();
            Route routeFrom = new Route();
            routeFrom.setRoute(from);
            routeFrom.setDistance(car1.getCurrentDistance());
            Route routeTo = new Route();
            routeTo.setRoute(to);
            routeTo.setDistance(car2.getCurrentDistance());
            NewRoutes newRoutes = new NewRoutes(saving, routeFrom, routeTo);

            if (!bestRoutes.isPresent()) {
              bestRoutes = Optional.of(newRoutes);
            } else if(bestRoutes.get().saving > newRoutes.saving) {
              bestRoutes = Optional.of(newRoutes);
            }
          }
        } catch (BatteryViolationException | TimewindowViolationException e) {}
      }
    }
    return bestRoutes;
  }
}
