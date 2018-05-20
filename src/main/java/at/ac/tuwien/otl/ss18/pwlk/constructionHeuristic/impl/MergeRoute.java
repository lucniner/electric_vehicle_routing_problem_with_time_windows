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
      routeList.remove(bestSaving.getKey().getKey().copyInverseRoute());
      routeList.remove(bestSaving.getKey().getValue().copyInverseRoute());

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
              for(int i=0; i<4; i++) { // try all different possibilities of two routes (normal, reverse => 4 combs)
                Route route1p;
                Route route2p;
                if(i == 0) {
                  route1p = route1.copyRoute();
                  route2p = route2.copyRoute();
                }else if (i == 1) {
                  route1p = route1.copyRoute();
                  route2p = route2.copyInverseRoute();
                }else if (i == 2) {
                  route1p = route1.copyInverseRoute();
                  route2p = route2.copyRoute();
                }else {
                  route1p = route1.copyInverseRoute();
                  route2p = route2.copyInverseRoute();
                }
                Optional<Pair<Route, Double>> newRoute = mergeTwoRoutes(route1p, route2p);
                if (newRoute.isPresent()) {
                  savings.put(new Pair(route1p, route2p), new Pair(newRoute.get().getKey(), newRoute.get().getValue()));
                  isHopeLess = false;
                }
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

    // drive route1
    try {
      car.driveRoute(route1.getRoute().subList(0, route1.getRoute().size() - 1));
    } catch (TimewindowViolationException t) {
      // can happen if route1 is inversed
      //logger.error("The exception should not happen on the first route to travel: " + t.getLocalizedMessage());
      return Optional.empty();
    } catch (BatteryViolationException b) {
      // can happen if route1 is inversed
      //logger.error("The exception should not happen on the first route to travel: " + b.getLocalizedMessage());
      return Optional.empty();
    }

    //drive distance from route1 to route2 and drive route 2
    Car newCar = null;
    Route remainingRoute = null;
    //TODO vllt alle ausführen und das beste nehmen?
    //TODO inject route in remainingroute einarbeiten?
    for(int i=0; i<4; i++) {
      remainingRoute = route2.copyRoute();
      newCar = car.cloneCar();

      // delete start depot from remaining route
      remainingRoute.setRoute(remainingRoute.getRoute().subList(1, remainingRoute.getRoute().size()));

      try {
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

        // if i==2 try without modification

        // try with possible charging station between route 1 and route 2
        if (i == 3) {
          List<Pair<AbstractNode, Double>> list = distanceHolder.getNearestRechargingStationsForCustomerInDistance(
                  remainingRoute.getRoute().get(0),
                  route1.getRoute().get(route1.getRoute().size() - 2)
          );
          if (!list.isEmpty()) {
            AbstractNode abstractNode = list.get(0).getKey();
            remainingRoute.getRoute().add(0, abstractNode);
          }
        }

        // now add line between route1 and rout2
        remainingRoute.getRoute().add(0, route1.getRoute().get(route1.getRoute().size()-2));
        newCar.driveRoute(remainingRoute.getRoute());

        break;
      } catch (BatteryViolationException b) {
        if (i == 2) {
          return Optional.empty();
        }
        //logger.error("BatteryViolation occured");

      } catch (TimewindowViolationException t) {
        if (i == 2) {
          return Optional.empty();
        }
        //logger.error("TimeViolation occured");
      }
    }

    if (newCar.getCurrentDistance() > (route1.getDistance() + route2.getDistance())) {
      return Optional.empty();
    }
    List<AbstractNode> newRouteList = Stream.concat(
            route1.getRoute().subList(0, route1.getRoute().size()-2).stream(),
            remainingRoute.getRoute().stream()
    ).collect(Collectors.toList());

    Route route = new Route();
    route.setDistance(newCar.getCurrentDistance());
    route.setRoute(newRouteList);

    double distanceSaving = (route1.getDistance() + route2.getDistance()) - newCar.getCurrentDistance();

    return Optional.of(new Pair(route, distanceSaving));
  }

}
