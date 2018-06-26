package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Car;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class BestTwoOptExchagne {

  private final Logger logger = LoggerFactory.getLogger(getClass());


  private final Route route;
  private final ProblemInstance problemInstance;
  private final DistanceHolder distanceHolder;
  private final PriorityQueue<Route> routes = new PriorityQueue<>();

  public BestTwoOptExchagne(Route route, ProblemInstance problemInstance, DistanceHolder distanceHolder) {
    this.route = route;
    this.problemInstance = problemInstance;
    this.distanceHolder = distanceHolder;
  }


  public Optional<Route> optimizeRoute() {

    twoExchange();

    if (routes.peek() != null) {
      return Optional.of(routes.peek());
    } else {
      return Optional.empty();
    }
  }

  private void twoExchange() {
    for (int i = 1; i < route.getRoute().size() - 2; i++) {
      final Route opt = route.copyRoute();
      final List<AbstractNode> nodes = opt.getRoute();
      final AbstractNode first = nodes.remove(i);
      final AbstractNode second = nodes.remove(i);
      nodes.add(i, second);
      nodes.add(i, first);
      driveCar(nodes, opt);
    }
  }


  private void driveCar(final List<AbstractNode> nodes, final Route opt) {
    Car car = new Car(problemInstance, distanceHolder);
    if (car.driveRoute(nodes)) {
      opt.setDistance(car.getCurrentDistance());
      opt.setRoute(nodes);
      if (opt.getDistance() < route.getDistance()) {
        routes.add(opt);
      }
    }
  }

}
