package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceCalculator;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class ConstructSolutionStub extends AbstractConstructSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  Optional<SolutionInstance> runAlgorithm(ProblemInstance problemInstance) {
    logger.info("Construct solution with algorithm 'Stub'");
    SolutionInstance solutionInstance = new SolutionInstance();
    ArrayList<Route> list = new ArrayList<>();

    //TODO löschen und einen richtigen algorithmus implementieren
    for(Customer customer : problemInstance.getCustomers()) {
      LinkedList<AbstractNode> routeList = new LinkedList<>();
      routeList.add(problemInstance.getDepot());
      routeList.add(customer);
      routeList.add(problemInstance.getDepot());
      Route route = new Route();
      route.setDistance(DistanceCalculator.calculateDistanceBetweenNodes(customer, problemInstance.getDepot()) *2 );
      route.setRoute(routeList);

      list.add(route);
    }
    solutionInstance.setRoutes(list);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException i) {}

    return Optional.of(solutionInstance);
  }
}
