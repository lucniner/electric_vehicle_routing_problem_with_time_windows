package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class InterRouteChanger {

  final List<Route> optimizedRoutes = new ArrayList<>();

  private final List<Route> routes;
  private final DistanceHolder holder;
  private final ProblemInstance problemInstance;

  public InterRouteChanger(
          final List<Route> routes,
          final DistanceHolder holder,
          final ProblemInstance problemInstance) {
    this.routes = routes;
    this.holder = holder;
    this.problemInstance = problemInstance;
  }

  public List<Route> optimizeRoutes() {

    final List<List<AbstractNode>> batteryInfeasible = new ArrayList<>();
    final List<List<AbstractNode>> timeWindowInfeasible = new ArrayList<>();

    for (final Route route : routes) {
      final List<AbstractNode> optimizedNodeRoute = new ArrayList<>();
      final Iterator<AbstractNode> iterator = route.getRoute().iterator();
      while (iterator.hasNext()) {
        final AbstractNode current = iterator.next();
        if (iterator.hasNext()) {
          final AbstractNode nextNode = iterator.next();
          if (nextNode instanceof ChargingStations || nextNode instanceof Depot) {
            optimizedNodeRoute.add(current);
            optimizedNodeRoute.add(nextNode);
          } else {
            optimizedNodeRoute.add(current);
            final List<Pair<AbstractNode, Double>> neighbours =
                    holder.getNearestCustomersForCustomer(current);
            for (final Pair<AbstractNode, Double> p : neighbours) {
              if (!neighbourIsOnSameRoute(route.getRoute(), p.getKey())) {
                if (customerCanBeInsertedOnRoute(new ArrayList<>(optimizedNodeRoute), p.getKey())) {
                  optimizedNodeRoute.add(p.getKey());
                  optimizedNodeRoute.add(nextNode);
                  Car car = new Car(problemInstance, holder);
                  try {
                    car.driveRoute(optimizedNodeRoute);
                    removeCustomerFromRoute(p.getKey());
                  } catch (BatteryViolationException | TimewindowViolationException e) {
                    optimizedNodeRoute.remove(optimizedNodeRoute.size() - 2);
                  }
                }
              }
            }

            optimizedNodeRoute.add(nextNode);
            Car car = new Car(problemInstance, holder);
            try {
              car.driveRoute(optimizedNodeRoute);
              final Route optimizedRoute = new Route();
              optimizedRoute.setRoute(optimizedNodeRoute);
              optimizedRoute.setDistance(car.getCurrentDistance());
            } catch (BatteryViolationException | TimewindowViolationException e) {
              optimizedNodeRoute.remove(nextNode);
            }
          }
        } else {
          optimizedNodeRoute.add(current);
        }
      }

      Car car = new Car(problemInstance, holder);
      try {
        car.driveRoute(optimizedNodeRoute);
        final Route optimizedRoute = new Route();
        optimizedRoute.setRoute(optimizedNodeRoute);
        optimizedRoute.setDistance(car.getCurrentDistance());
        optimizedRoutes.add(optimizedRoute);

      } catch (BatteryViolationException e) {
        batteryInfeasible.add(optimizedNodeRoute);
      } catch (TimewindowViolationException e) {
        timeWindowInfeasible.add(optimizedNodeRoute);
      }
    }


    final List<Route> x = optimizeInfeasibleTours(batteryInfeasible);
    optimizedRoutes.addAll(x);
    return optimizedRoutes;
  }

  private List<Route> optimizeInfeasibleTours(List<List<AbstractNode>> infeasible) {
    final List<Route> r = new ArrayList<>();
    for (final List<AbstractNode> route : infeasible) {
      final List<AbstractNode> intermediate = new LinkedList<>();
      AbstractNode last = null;
      for (final AbstractNode node : route) {

        intermediate.add(node);
        intermediate.add(intermediate.get(0));
        Car car = new Car(problemInstance, holder);
        try {
          car.driveRoute(intermediate);
          final Route optimizedRoute = new Route();
          optimizedRoute.setRoute(intermediate);
          optimizedRoute.setDistance(car.getCurrentDistance());
        } catch (BatteryViolationException | TimewindowViolationException e) {
          AbstractNode charging = holder.getNearestRechargingStationsForCustomer((Customer) last).get(0).getKey();
          intermediate.add(intermediate.size() - 1, charging);
        }
        last = node;
      }
      Route s = new Route();
      s.setRoute(intermediate);
      r.add(s);
    }
    return r;
  }

  private boolean customerCanBeInsertedOnRoute(
          final List<AbstractNode> route, final AbstractNode node) {
    route.add(node);
    Car car = new Car(problemInstance, holder);
    try {
      car.driveRoute(route);
      return true;
    } catch (TimewindowViolationException e) {
      return false;
    } catch (BatteryViolationException e) {
      return false;
    }
  }

  private void removeCustomerFromRoute(final AbstractNode node) {
    for (final Route route : routes) {
      for (final AbstractNode c : new ArrayList<>(route.getRoute())) {
        if (c.equals(node)) {
          route.getRoute().remove(c);
        }
      }
    }
  }

  private boolean neighbourIsOnSameRoute(
          final List<AbstractNode> route, final AbstractNode neighbour) {
    return route.contains(neighbour);
  }
}
