package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// joint bei jeder iteration immer die besten routen zusammen und berechnet anschließend nochmal die verkürzungen
public class MergeRoute {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private ProblemInstance problemInstance;
  private DistanceHolder distanceHolder;
  private SolutionInstance solutionInstance;

  public MergeRoute(ProblemInstance problemInstance, DistanceHolder distanceHolder, SolutionInstance solutionInstance) {
    this.problemInstance = problemInstance;
    this.distanceHolder = distanceHolder;
    this.solutionInstance = solutionInstance;
  }

  public SolutionInstance mergeRoutes() {
    Map<Pair<Route, Route>, Pair<Route, Double>> savings = calculateSavingsValue();

    while(!savings.isEmpty()) {
      Map.Entry<Pair<Route, Route>, Pair<Route, Double>> bestSaving = null;

      for(Map.Entry<Pair<Route, Route>, Pair<Route, Double>> saving : savings.entrySet()) {
        if (bestSaving == null) {
          bestSaving = saving;
        } else {
          if (bestSaving.getValue().getValue() < saving.getValue().getValue()) {
            bestSaving = saving;
          }
        }
      }

      logger.info("Merge route "
              + bestSaving.getKey().getKey().toString()
              + " with route "
              + bestSaving.getKey().getValue().toString());

      List<Route> routeList = solutionInstance.getRoutes();
      routeList.add(bestSaving.getValue().getKey());
      routeList.remove(bestSaving.getKey().getKey());
      routeList.remove(bestSaving.getKey().getValue());
      routeList.remove(bestSaving.getKey().getKey().copyInverseRoute());
      routeList.remove(bestSaving.getKey().getValue().copyInverseRoute());

      savings = calculateSavingsValue();
    }
    return solutionInstance;
  }

  private Map<Pair<Route, Route>, Pair<Route, Double>> calculateSavingsValue() {
    Map<Pair<Route, Route>, Pair<Route,Double>> savings = new ConcurrentHashMap<>();

    // vllt statt check von allen routen mit allen schon die infeasible routes weggeben?
    // die schon weggefiltert worden sind mit den constraints vom paper
    // -> man braucht aber eine method um zu checken ob die distanz möglich ist (nicht nur zwischen 2 customer)
    solutionInstance.getRoutes().parallelStream().forEach((route1) -> {
      for (final Route route2 : solutionInstance.getRoutes()) {
        if ((!route1.equals(route2))) { // route1 und route2 sollen unterschiedlich sein, man kann nicht zwei gleiche mergen
          for (int i = 0; i < 4; i++) { // try all different possibilities of two routes (normal, reverse => 4 combs)
            Route route1p;
            Route route2p;
            if (i == 0) {
              route1p = route1.copyRoute();
              route2p = route2.copyRoute();
            } else if (i == 1) {
              route1p = route1.copyRoute();
              route2p = route2.copyInverseRoute();
            } else if (i == 2) {
              route1p = route1.copyInverseRoute();
              route2p = route2.copyRoute();
            } else {
              route1p = route1.copyInverseRoute();
              route2p = route2.copyInverseRoute();
            }
            Optional<Pair<Route, Double>> newRoute = mergeTwoRoutes(route1p, route2p);
            if (newRoute.isPresent()) {
              savings.put(new Pair(route1p, route2p), new Pair(newRoute.get().getKey(), newRoute.get().getValue()));
            }
          }
        }
      }
    });
    return savings;
  }

  // es wird eine neue Route getestet, die die Richtung Route1 zu Route 2 aufrecht erhaltet
  private Optional<Pair<Route, Double>> mergeTwoRoutes(Route route1, Route route2) {
    if (route1.getDemandOfRoute() + route2.getDemandOfRoute() > problemInstance.getLoadCapacity()) {
      Optional.empty();
    }

    Car car = new Car(problemInstance);
    Route firstRoute;
    firstRoute = route1.copyRoute();
    // delete end depot from first route
    firstRoute.getRoute().remove(firstRoute.getRoute().size()-1);
    // delete possible charging station on route1
    if (firstRoute.getRoute().get(firstRoute.getRoute().size()-1) instanceof ChargingStations) {
      firstRoute.getRoute().remove(firstRoute.getRoute().size()-1);
    }

    // drive firstroute
    try {
        car.driveRoute(firstRoute.getRoute());
    } catch (TimewindowViolationException t) {
      return Optional.empty();
    } catch (BatteryViolationException b) {
      return Optional.empty();
    }

    //drive distance from route1 to route2 and drive route 2
    Car newCar;
    Route remainingRoute;
    List<Pair<Car, List<AbstractNode>>> possibleSolutions = new ArrayList<>();

    int maxIteration = 7;
    for(int i=0; i<maxIteration; i++) {
      remainingRoute = route2.copyRoute();
      newCar = car.cloneCar();

      // delete start depot from remaining route
      remainingRoute.setRoute(remainingRoute.getRoute().subList(1, remainingRoute.getRoute().size()));

      // try without possible chargingstation on route2 end
      if (i == 0) {
        if (remainingRoute.getRoute().get(remainingRoute.getRoute().size()-2) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(remainingRoute.getRoute().size()-2);
        }
      }

      // try without possible chargingstation between route 1 and route 2
      if (i == 1) {
        if (remainingRoute.getRoute().get(0) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(0);
        }
      }

      // try without possible chargingstation on route2 end and insert possible charging station between route1 and route2
      if (i == 2) {
        if (remainingRoute.getRoute().get(remainingRoute.getRoute().size() - 2) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(remainingRoute.getRoute().size() - 2);

          List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                  firstRoute.getRoute().get(firstRoute.getRoute().size() - 1),
                  remainingRoute.getRoute().get(0)
          );
          if (!list.isEmpty()) {
            AbstractNode abstractNode = list.get(0).getKey();
            remainingRoute.getRoute().add(0, abstractNode);
          }

        }
      }

      // try with possible charging station between route 1 and route 2 - near route1
      if (i == 3) {
        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1),
                remainingRoute.getRoute().get(0)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }
      }

      // try with possible charging station between route 1 and route 2 - near route2
      if (i == 4) {
        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                remainingRoute.getRoute().get(0),
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }
      }

      // try without possible chargingstation on route2 end and insert possible charging station between route1 and route2
      if (i == 5) {
        if (remainingRoute.getRoute().get(remainingRoute.getRoute().size() - 2) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(remainingRoute.getRoute().size() - 2);

          List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                  remainingRoute.getRoute().get(0),
                  firstRoute.getRoute().get(firstRoute.getRoute().size() - 1)
          );
          if (!list.isEmpty()) {
            AbstractNode abstractNode = list.get(0).getKey();
            remainingRoute.getRoute().add(0, abstractNode);
          }

        }
      }


      // if (i == 6) do no special treatment

      // now connect route 1 and remaining route
      remainingRoute.getRoute().add(0, firstRoute.getRoute().get(firstRoute.getRoute().size() - 1));

      try {
        newCar.driveRoute(remainingRoute.getRoute());
        possibleSolutions.add(new Pair(newCar, remainingRoute.getRoute()));
      } catch (BatteryViolationException b) {
      } catch (TimewindowViolationException t) {
      }
    }

    if (possibleSolutions.size() == 0) {
      return Optional.empty();
    } else {
      Car bestCar = null;
      List<AbstractNode> bestRemainingRoute = null;

      for (Pair pair : possibleSolutions) {
        if (bestCar == null) {
          bestCar = (Car)pair.getKey();
          bestRemainingRoute = (List<AbstractNode>)pair.getValue();
        } else {
          if (((Car)pair.getKey()).getCurrentDistance() < bestCar.getCurrentDistance()) {
            bestCar = (Car)pair.getKey();
            bestRemainingRoute = (List<AbstractNode>)pair.getValue();
          }
        }
      }

      if (bestCar.getCurrentDistance() > (route1.getDistance() + route2.getDistance())) {
        return Optional.empty();
      }
      List<AbstractNode> newRouteList = Stream.concat(
              firstRoute.getRoute().subList(0, firstRoute.getRoute().size() - 1).stream(),
              bestRemainingRoute.stream()
      ).collect(Collectors.toList());

      Route route = new Route();
      route.setDistance(bestCar.getCurrentDistance());
      route.setRoute(newRouteList);

      double distanceSaving = (route1.getDistance() + route2.getDistance()) - bestCar.getCurrentDistance();

      return Optional.of(new Pair(route, distanceSaving));
    }
  }

}
