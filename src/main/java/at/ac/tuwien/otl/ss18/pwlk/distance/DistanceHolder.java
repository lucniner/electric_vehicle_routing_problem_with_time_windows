package at.ac.tuwien.otl.ss18.pwlk.distance;

import at.ac.tuwien.otl.ss18.pwlk.constraints.ConstraintsChecker;
import at.ac.tuwien.otl.ss18.pwlk.util.Pair;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ChargingStations;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Customer;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;

import java.util.*;
import java.util.stream.Collectors;

public class DistanceHolder {



  private final Map<Customer, Double> customerDepotDistances = new HashMap<>();
  private final Map<Customer, List<Pair<AbstractNode, Double>>> interCustomerDistances =
          new HashMap<>();
  private final Map<Customer, List<Pair<AbstractNode, Double>>> customerChargingStationDistances =
          new HashMap<>();

  private final ProblemInstance problemInstance;

  public DistanceHolder(final ProblemInstance problemInstance) {
    this.problemInstance = problemInstance;
    init();
  }

  private void init() {
    calculateCustomerToCustomerDistances();
    calculateCustomerToDepotDistances();
    calculateCustomerToRechargingDistances();
  }

  private void calculateCustomerToDepotDistances() {
    final AbstractNode depot = problemInstance.getDepot();
    for (final Customer c : problemInstance.getCustomers()) {
      final double distance = DistanceCalculator.calculateDistanceBetweenNodes(depot, c);
      customerDepotDistances.put(c, distance);
    }
  }

  private void calculateCustomerToCustomerDistances() {
    for (final Customer initialPoint : problemInstance.getCustomers()) {
      final List<Pair<AbstractNode, Double>> distances = new LinkedList<>();
      for (final Customer to : problemInstance.getCustomers()) {
        final ConstraintsChecker constraintsChecker = new ConstraintsChecker(problemInstance, initialPoint, to);
        if (!constraintsChecker.violatesPreprocessingConstraints()) {
          final double distance =
                  DistanceCalculator.calculateDistanceBetweenNodes(initialPoint, to);
          distances.add(new Pair<>(to, distance));
        }
      }
      interCustomerDistances.put(initialPoint, distances);
    }
  }

  private void calculateCustomerToRechargingDistances() {
    for (final Customer initialPoint : problemInstance.getCustomers()) {
      final List<Pair<AbstractNode, Double>> distances = new LinkedList<>();
      for (final ChargingStations to : problemInstance.getChargingStations()) {
        final double distance = DistanceCalculator.calculateDistanceBetweenNodes(initialPoint, to);
        distances.add(new Pair<>(to, distance));
      }
      customerChargingStationDistances.put(initialPoint, distances);
    }
  }

  public double getDistanceToDepotForCustomer(final Customer customer) {
    return customerDepotDistances.get(customer);
  }

  public double calculateDistanceBetweenCustomers(final Customer from, final Customer to) {
    return interCustomerDistances
            .get(from)
            .stream()
            .filter(
                    pair -> pair.getKey().getId().equalsIgnoreCase(to.getId())) // only one pair will remain
            .mapToDouble(Pair::getValue) // mapping to the distance
            .sum(); // using sum here because it returns a double and does not affect the distance
  }

  public List<Pair<AbstractNode, Double>> getNearestCustomersForCustomer(final AbstractNode from) {
    final OptionalDouble minDistance = calculateMinDistanceInList(interCustomerDistances.get(from));
    return calculateMinNodeBasedOnDistance(interCustomerDistances.get(from), minDistance);
  }

  public List<Pair<AbstractNode, Double>> getNearestRechargingStationsForCustomer(
          final Customer from) {
    final OptionalDouble minDistance =
            calculateMinDistanceInList(customerChargingStationDistances.get(from));
    return calculateMinNodeBasedOnDistance(customerChargingStationDistances.get(from), minDistance);
  }

  private OptionalDouble calculateMinDistanceInList(
          final List<Pair<AbstractNode, Double>> distances) {
    return distances.stream().mapToDouble(Pair::getValue).min();
  }

  private List<Pair<AbstractNode, Double>> calculateMinNodeBasedOnDistance(
          final List<Pair<AbstractNode, Double>> distances, final OptionalDouble minDistance) {
    if (minDistance.isPresent()) {
      return distances
              .stream()
              .filter(p -> p.getValue() == minDistance.getAsDouble())
              .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  public Map<Customer, List<Pair<AbstractNode, Double>>> getInterCustomerDistances() {
    return interCustomerDistances;
  }
}
