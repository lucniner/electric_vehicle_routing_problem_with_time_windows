package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;

import java.util.ArrayList;
import java.util.List;

public class InterRouteChanger {

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

    final List<Route> optimizedRoutes = new ArrayList<>();
    for (final Route route : routes) {
      final List<AbstractNode> optimizedNodeRoute = new ArrayList<>();
      for (final AbstractNode node : route.getRoute()) {
        optimizedNodeRoute.add(node);
        if (node instanceof Customer) {
          final List<Pair<AbstractNode, Double>> neighbours =
                  holder.getNearestCustomersForCustomer(node);
          for (final Pair<AbstractNode, Double> p : neighbours) {
            if (!neighbourIsOnSameRoute(route.getRoute(), p.getKey())) {
              if (customerCanBeInsertedOnRoute(new ArrayList<>(optimizedNodeRoute), p.getKey())) {
                removeCustomerFromRoute(p.getKey());
                optimizedNodeRoute.add(p.getKey());
              }
            }
          }
        }
      }

      Car car = new Car(problemInstance, holder);
      final Route optimizedRoute = new Route();
      if (!car.driveRoute(optimizedNodeRoute)) {
        continue;
      }
      optimizedRoute.setDistance(car.getCurrentDistance());
      optimizedRoute.setRoute(optimizedNodeRoute);
      optimizedRoutes.add(optimizedRoute);
    }

    return optimizedRoutes;
  }

  private boolean customerCanBeInsertedOnRoute(
          final List<AbstractNode> route, final AbstractNode node) {
    route.add(node);
    Car car = new Car(problemInstance, holder);
    return car.driveRoute(route);
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
