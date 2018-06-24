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

  private Map hopeLessMerge;
  private Map<Pair<Route, Route>, Pair<Route,Double>> alreadyComputed;
  private double maxDistanceToDepot;

  public MergeRoute(ProblemInstance problemInstance, DistanceHolder distanceHolder, SolutionInstance solutionInstance) {
    this.problemInstance = problemInstance;
    this.distanceHolder = distanceHolder;
    this.solutionInstance = solutionInstance;
    this.maxDistanceToDepot = distanceHolder.getMaxDistanceToDepot();
  }

  public SolutionInstance mergeRoutes() {
    SolutionInstance bestSolutionInstance = solutionInstance;

    for (int i = 0; i<1; i++) {
      // means that merges far away from depot are better (0 is off)
      double benefit_factor = i/3;
      SolutionInstance currSoluctionInstance = solutionInstance.copy();
      hopeLessMerge = new ConcurrentHashMap<Pair<Route, Route>, Boolean>();
      alreadyComputed = new ConcurrentHashMap<>();

      Map<Pair<Route, Route>, Pair<Route, Double>> savings = calculateSavingsValue(currSoluctionInstance);
      while (!savings.isEmpty()) {
        Map.Entry<Pair<Route, Route>, Pair<Route, Double>> bestSaving = null;

        for (Map.Entry<Pair<Route, Route>, Pair<Route, Double>> saving : savings.entrySet()) {
          if (bestSaving == null) {
            bestSaving = saving;
          } else {
            double distance_to_depot = distanceHolder.getInterNodeDistance(saving.getKey().getValue().getRoute().get(1), problemInstance.getDepot());
            double distance_to_depot_best = distanceHolder.getInterNodeDistance(bestSaving.getKey().getValue().getRoute().get(1), problemInstance.getDepot());
            double factor = 1 + benefit_factor * (distance_to_depot / maxDistanceToDepot);
            double factor_best = 1 + benefit_factor * (distance_to_depot_best / maxDistanceToDepot);

            double value_saving = factor * saving.getValue().getValue();
            double value_beset = factor_best * bestSaving.getValue().getValue();

            if (value_beset < value_saving) {
              bestSaving = saving;
            }
          }
        }

        logger.debug("Merge route "
                + bestSaving.getKey().getKey().toString()
                + " with route "
                + bestSaving.getKey().getValue().toString());

        List<Route> routeList = currSoluctionInstance.getRoutes();
        routeList.add(bestSaving.getValue().getKey());
        routeList.remove(bestSaving.getKey().getKey());
        routeList.remove(bestSaving.getKey().getValue());
        routeList.remove(bestSaving.getKey().getKey().copyInverseRoute());
        routeList.remove(bestSaving.getKey().getValue().copyInverseRoute());


        savings = calculateSavingsValue(currSoluctionInstance);
      }

      if (currSoluctionInstance.getDistanceSum() < bestSolutionInstance.getDistanceSum()) {
        bestSolutionInstance = currSoluctionInstance;
      }
    }
    return bestSolutionInstance;
  }

  private Map<Pair<Route, Route>, Pair<Route, Double>> calculateSavingsValue(SolutionInstance currSolution) {
    Map<Pair<Route, Route>, Pair<Route,Double>> savings = new ConcurrentHashMap<>();

    // vllt statt check von allen routen mit allen schon die infeasible routes weggeben?
    // die schon weggefiltert worden sind mit den constraints vom paper
    currSolution.getRoutes().parallelStream().forEach((route1) -> {
      for (final Route route2 : currSolution.getRoutes()) {
        if ((!route1.equals(route2))) { // route1 und route2 sollen unterschiedlich sein, man kann nicht zwei gleiche mergen
          if (!hopeLessMerge.containsKey(new Pair(route1, route2))) {
            boolean hopeless = true;
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
              if (alreadyComputed.containsKey(new Pair(route1p, route2p))) {
                Pair<Route, Double> newRoute = alreadyComputed.get(new Pair(route1p, route2p));
                savings.put(new Pair(route1p, route2p), new Pair(newRoute.getKey(), newRoute.getValue()));
                hopeless = false;
              } else {
                Optional<Pair<Route, Double>> newRoute = mergeTwoRoutes(route1p, route2p);
                if (newRoute.isPresent()) {
                  alreadyComputed.put(new Pair(route1p, route2p), new Pair(newRoute.get().getKey(), newRoute.get().getValue()));
                  savings.put(new Pair(route1p, route2p), new Pair(newRoute.get().getKey(), newRoute.get().getValue()));
                  hopeless = false;
                }
              }
            }
            if (hopeless) {
              hopeLessMerge.put(new Pair(route1.copyRoute(), route2.copyRoute()), true);
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
      return Optional.empty();
    }

    Car car = new Car(problemInstance, distanceHolder);
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

      // if (i == 0) do no special treatment

      // try without possible chargingstation on route2 end
      if (i == 1) {
        if (remainingRoute.getRoute().get(remainingRoute.getRoute().size()-2) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(remainingRoute.getRoute().size()-2);
        }
      }

      // try without possible chargingstation on route2 end and insert possible charging station between route1 and route2
      // new charging station near route 1
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

      // try without possible chargingstation on route2 end and insert possible charging station between route1 and route2
      // new charging station near route 2
      if (i == 3) {
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

      // try without possible chargingstation on route2 start
      if (i == 4) {
        if (remainingRoute.getRoute().get(0) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(0);
        }
      }

      // try without possible chargingstation on route2 start
      // new charging station near route 1
      if (i == 5) {
        if (remainingRoute.getRoute().get(0) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(0);
        }

        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1),
                remainingRoute.getRoute().get(0)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }

      }

      // try without possible chargingstation on route2 start
      // new charging station near route 2
      if (i == 6) {
        if (remainingRoute.getRoute().get(0) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(0);
        }

        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                remainingRoute.getRoute().get(0),
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }

      }

      // try with possible charging station between route 1 and route 2
      // new charging station near route 1
      if (i == 7) {
        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1),
                remainingRoute.getRoute().get(0)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }
      }

      // try with possible charging station between route 1 and route 2
      // new charging station near route 2
      if (i == 8) {
        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                remainingRoute.getRoute().get(0),
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }
      }

      // try two possible charging station between route 1 and route 2
      if (i == 9) {
        if (remainingRoute.getRoute().get(0) instanceof ChargingStations) {
          remainingRoute.getRoute().remove(0);
        }
        List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                remainingRoute.getRoute().get(0),
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }

        list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                firstRoute.getRoute().get(firstRoute.getRoute().size() - 1),
                remainingRoute.getRoute().get(0)
        );
        if (!list.isEmpty()) {
          AbstractNode abstractNode = list.get(0).getKey();
          remainingRoute.getRoute().add(0, abstractNode);
        }
      }

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
