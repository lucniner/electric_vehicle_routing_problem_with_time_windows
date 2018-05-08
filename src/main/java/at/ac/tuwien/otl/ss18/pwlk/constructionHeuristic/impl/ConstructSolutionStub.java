package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceCalculator;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;

public class ConstructSolutionStub implements IConstructSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());


  @Override
  public SolutionInstance constructSolution(ProblemInstance problemInstance) {
    logger.info("Construct solution with algorithm 'Stub'");
    //TODO delete this stub code and implement algorithm
    //Currently it create one route per customer and doesn't use the fuel station
    SolutionInstance solutionInstance = new SolutionInstance();
    ArrayList<Route> list = new ArrayList<>();

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

    return solutionInstance;
  }
}
