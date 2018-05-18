package at.ac.tuwien.otl.ss18.pwlk.constraints;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceCalculator;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ChargingStations;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;

import java.util.List;

public class ConstraintsChecker {

  private final ProblemInstance problemInstance;

  private final AbstractNode first;
  private final AbstractNode second;

  public ConstraintsChecker(
          final ProblemInstance problemInstance, final AbstractNode first, final AbstractNode second) {
    this.problemInstance = problemInstance;
    this.first = first;
    this.second = second;
  }

  public boolean violatesPreprocessingConstraints() {
    return isSelf()
            || violatesCapacity()
            || violatesLatestStartOfServiceBetweenNodes()
            || violatesLatestArrivalAtDepot()
            || violatesBatteryCapacityConstraint();
  }

  public boolean isSelf() {
    return first.equals(second);
  }

  public boolean violatesCapacity() {
    final double maxCapacity = problemInstance.getLoadCapacity();
    return first.getDemand() + second.getDemand() > maxCapacity;
  }

  public boolean violatesLatestStartOfServiceBetweenNodes() {
    final double latestArrival = noteSatisfactionTime();
    return second.getTimeWindow().isBeforeDueTime(latestArrival);
  }

  public boolean violatesLatestArrivalAtDepot() {
    final double serviceTimeSecondCustomer = second.getServiceTime();
    final double travelTimeDepot =
            DistanceCalculator.calculateDistanceBetweenNodes(second, problemInstance.getDepot());
    final double latestArrival =
            noteSatisfactionTime() + serviceTimeSecondCustomer + travelTimeDepot;
    return problemInstance.getDepot().getTimeWindow().isBeforeDueTime(latestArrival);
  }

  private double noteSatisfactionTime() {
    final double readyTime = first.getTimeWindow().getReadyTime();
    final double serviceTime = first.getServiceTime();
    final double travelTime = DistanceCalculator.calculateDistanceBetweenNodes(first, second);
    return readyTime + serviceTime + travelTime;
  }

  public boolean violatesBatteryCapacityConstraint() {
    final List<ChargingStations> firstSet = problemInstance.getChargingStations();
    final List<ChargingStations> secondSet = problemInstance.getChargingStations();
    for (final ChargingStations firstStation : firstSet) {
      for (final ChargingStations secondStation : secondSet) {
        final double stationFirstCustomerDistance =
                DistanceCalculator.calculateDistanceBetweenNodes(firstStation, first);
        final double customerDistance =
                DistanceCalculator.calculateDistanceBetweenNodes(first, second);
        final double secondCustomerStationDistance =
                DistanceCalculator.calculateDistanceBetweenNodes(second, secondStation);
        final double totalDistance =
                stationFirstCustomerDistance + customerDistance + secondCustomerStationDistance;
        final double discharging = problemInstance.getChargeConsumptionRate() * totalDistance;
        if (discharging > problemInstance.getBatteryCapacity()) {
          return true;
        }
      }
    }
    return false;
  }
}
