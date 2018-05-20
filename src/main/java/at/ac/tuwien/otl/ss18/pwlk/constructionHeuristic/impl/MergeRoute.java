package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// joint bei jeder iteration immer die besten routen zusammen und berechnet anschließend nochmal die verkürzungen
public class MergeRoute {
  private final Logger logger = LoggerFactory.getLogger(getClass());


  // die routen, die beim ersten mal mit keiner anderen route gemerged werden konnten, werden nicht nochmal probiert
  private List<Route> hopelessRoutes;

  private ProblemInstance problemInstance;
  private DistanceHolder distanceHolder;
  private SolutionInstance solutionInstance;

  public MergeRoute(ProblemInstance problemInstance, DistanceHolder distanceHolder, SolutionInstance solutionInstance) {
    this.problemInstance = problemInstance;
    this.distanceHolder = distanceHolder;
    this.solutionInstance = solutionInstance;
    this.hopelessRoutes = new ArrayList<>();
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

      savings = calculateSavingsValue();
    }
    return solutionInstance;
  }

  private Map<Pair<Route, Route>, Pair<Route, Double>> calculateSavingsValue() {
    Map<Pair<Route, Route>, Pair<Route,Double>> savings = new HashMap<>();

    //TODO vllt statt check von allen routen mit allen schon die infeasible routes weggeben?
    // die schon weggefiltert worden sind mit den constraints vom paper
    // -> man braucht aber eine method um zu checken ob die distanz möglich ist (nicht nur zwischen 2 customer)
    boolean isHopeLess = true;
    for (final Route route1: solutionInstance.getRoutes()) {
      if (!hopelessRoutes.contains(route1)) { // macht keinen sinn hoffnungslose routen nochmal zu probieren zu mergen
        for (final Route route2 : solutionInstance.getRoutes()) {
          if (!hopelessRoutes.contains(route2)) { //macht keinen sinn hoffnungslose routen nochmal zu probieren
            if ((!route1.equals(route2))) { // route1 und route2 sollen unterschiedlich sein, man kann nicht zwei gleiche mergen
              Optional<Pair<Route, Double>> newRoute = mergeTwoRoutes(route1, route2);
              if (newRoute.isPresent()) {
                savings.put(new Pair(route1, route2), new Pair(newRoute.get().getKey(), newRoute.get().getValue()));
                isHopeLess = false;
              }
            }
          }
        }
        if (isHopeLess) {
          hopelessRoutes.add(route1);
        }
      }
    }
    return savings;
  }

  // es wird eine neue Route getestet, die die Richtung Route1 zu Route 2 aufrecht erhaltet
  private Optional<Pair<Route, Double>> mergeTwoRoutes(Route route1, Route route2) {
    if (route1.getDemandOfRoute() + route2.getDemandOfRoute() > problemInstance.getLoadCapacity()) {
      Optional.empty();
    }

    Car car = new Car(problemInstance);

    try {
      car.driveRoute(route1.getRoute().subList(0, route1.getRoute().size() - 1));
    } catch (TimewindowViolationException t) {
      logger.error("The exception should not happen on the first route to travel: " + t.getLocalizedMessage());
      Optional.empty();
    } catch (BatteryViolationException b) {
      logger.error("The exception should not happen on the first route to travel: " + b.getLocalizedMessage());
      Optional.empty();
    }

    Car newCar = null;
    for(int i=0; i<2; i++) {
      newCar = car.cloneCar();
      try {
        List<AbstractNode> injectRoute = new ArrayList<>();
        injectRoute.add(route1.getRoute().get(route1.getRoute().size() - 2));
        injectRoute.add(route2.getRoute().get(1));
        newCar.driveRoute(injectRoute);

        newCar.driveRoute(route2.getRoute().subList(1, route2.getRoute().size()));
      } catch (BatteryViolationException b) {
        //logger.error("BatteryViolation occured");
        return Optional.empty();

      } catch (TimewindowViolationException t) {
        //logger.error("TimeViolation occured");
        return Optional.empty();
      }
    }

    if (newCar.getCurrentDistance() > (route1.getDistance() + route2.getDistance())) {
      return Optional.empty();
    }

    List<AbstractNode> newRouteList = Stream.concat(
            route1.getRoute().subList(0, route1.getRoute().size()-1).stream(),
            route2.getRoute().subList(1, route2.getRoute().size()).stream())
            .collect(Collectors.toList());
    Route route = new Route();
    route.setDistance(newCar.getCurrentDistance());
    route.setRoute(newRouteList);

    double distanceSaving = (route1.getDistance() + route2.getDistance()) - newCar.getCurrentDistance();

    return Optional.of(new Pair(route, distanceSaving));
  }

}
