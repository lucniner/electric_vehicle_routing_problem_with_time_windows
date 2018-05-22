package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceCalculator;
import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.EvrptwRunException;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConstructSolutionStub extends AbstractConstructSolution {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private List<Route> pendelRoutes;
  private Depot depot;
  private double batteryCapacity;
  private ProblemInstance problemInstance;
  private DistanceHolder distanceHolder;

  @Override
  Optional<SolutionInstance> runAlgorithm(ProblemInstance problemInstance) throws EvrptwRunException {
    logger.info("Construct solution with algorithm 'Stub'");
    SolutionInstance solutionInstance = new SolutionInstance();
    this.pendelRoutes = new ArrayList<>();
    this.depot = problemInstance.getDepot();
    this.batteryCapacity = problemInstance.getBatteryCapacity();
    this.problemInstance = problemInstance;
    this.distanceHolder = new DistanceHolder(problemInstance);

    // 1. distance holder ausführen
    // 2. (13-16) constraints in eigene klasse -> DistanceHolder rein, DistanceHolder raus
    // 3. pendel routen auch mit battery constraints
    //    3.1 check constraints auch bei pendel route
    // 4. merging von routen (methode(route1, route2) -> Option<newRoute> vllt in neue klasse
    // auslagern)
    //    4.1 check battery constaint (auch in klasse von punkt 2)
    //        best charging station (wo halt alle constraints passen)
    //    4.2 check time window (in eigener klasse, auch in klasse von punkt 2)

    createPendelRoutes2();
    solutionInstance.setRoutes(pendelRoutes);
    solutionInstance = new MergeRoute(this.problemInstance, this.distanceHolder, solutionInstance).mergeRoutes();
    return Optional.of(solutionInstance);
  }

  private void createPendelRoutes2() throws EvrptwRunException {
    ModifyRoute modifyRoute = new ModifyRoute(distanceHolder, problemInstance);
    for (Customer customer : problemInstance.getCustomers()) {
      Car car = new Car(problemInstance, distanceHolder);
      LinkedList<AbstractNode> routeList = new LinkedList<>();
      routeList.add(problemInstance.getDepot());
      routeList.add(customer);

      Pair<Car, LinkedList<AbstractNode>> newRoute = modifyRoute.addChargingStation(car, routeList);
      routeList.add(problemInstance.getDepot());
      newRoute = modifyRoute.addChargingStation(newRoute.getKey(), newRoute.getValue());

      Route route = new Route();
      route.setDistance(newRoute.getKey().getCurrentDistance());
      route.setRoute(newRoute.getValue());
      pendelRoutes.add(route);
    }
  }

  private void createPendelRoutes() {
    for (Customer customer : problemInstance.getCustomers()) {
      double totalDistance = 0.0;
      LinkedList<AbstractNode> routeList = new LinkedList<>();
      routeList.add(problemInstance.getDepot());

      final double depotCustomerDistance =
              DistanceCalculator.calculateDistanceBetweenNodes(depot, customer);
      final double consumption = problemInstance.getChargeConsumptionRate();
      final double batteryConsumption = consumption * depotCustomerDistance;

      totalDistance += depotCustomerDistance;
      // drive from depot to charging station then to customer
      if (batteryConsumption > batteryCapacity) {
        logger.error(
                "the vehicle does not reach the customer and has to drive to the charging station first");
      }
      routeList.add(customer);

      // drive from customer to charging station
      if (batteryCapacity - batteryConsumption < batteryConsumption) {
        final List<Pair<AbstractNode, Double>> stations =
                distanceHolder.getNearestRechargingStationsForCustomer(customer);
        final AbstractNode station = stations.get(0).getKey();
        routeList.add(station);

        totalDistance += DistanceCalculator.calculateDistanceBetweenNodes(customer, station);
        totalDistance += DistanceCalculator.calculateDistanceBetweenNodes(station, depot);
      } else {
        totalDistance += depotCustomerDistance;
      }

      routeList.add(problemInstance.getDepot());
      Route route = new Route();
      route.setDistance(totalDistance);
      route.setRoute(routeList);

      pendelRoutes.add(route);
    }
  }

  private List<Route> calculateMergedRoutes() {
    final List<Route> routes = new ArrayList<>();
    final List<AbstractNode> usedCustomers = new ArrayList<>();

    final Map<Customer, List<Pair<AbstractNode, Double>>> map =
            distanceHolder.getInterCustomerDistances();
    for (Map.Entry<Customer, List<Pair<AbstractNode, Double>>> entry : map.entrySet()) {
      final List<AbstractNode> route = new ArrayList<>();
      final Customer customer = entry.getKey();
      if (!usedCustomers.contains(customer)) {
        usedCustomers.add(customer);
        route.add(depot);
        route.add(customer);
        double time =
                depot.getTimeWindow().getReadyTime()
                        + DistanceCalculator.calculateDistanceBetweenNodes(depot, customer);
        time += customer.getServiceTime();

        double remainingBattery =
                problemInstance.getBatteryCapacity()
                        - (problemInstance.getChargeConsumptionRate()
                        * DistanceCalculator.calculateDistanceBetweenNodes(depot, customer));
        if (!entry.getValue().isEmpty()) {

        } else {
          final List<Pair<AbstractNode, Double>> stations =
                  distanceHolder.getNearestRechargingStationsForCustomer(customer);
          final AbstractNode station = stations.get(0).getKey();
          route.add(station);
        }
        route.add(depot);

        final Route solution = new Route();
        solution.setDistance(0);
        solution.setRoute(route);
        routes.add(solution);
      }
    }

    return routes;
  }
}
