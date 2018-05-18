package at.ac.tuwien.otl.ss18.pwlk.valueobjects;

import java.util.List;
import java.util.Objects;

public class ProblemInstance {

  private final Depot depot;
  private final List<ChargingStations> chargingStations;
  private final List<Customer> customers;
  private final double batteryCapacity;
  private final double loadCapacity;
  private final double chargingCapacityRate;
  private final double inverseRechargingRate;
  private final double averageVelocity;

  public ProblemInstance(final Depot depot, final List<ChargingStations> chargingStations, final List<Customer> customers, final double batteryCapacity, final double loadCapacity, final double chargingCapacityRate, final double inverseRechargingRate, final double averageVelocity) {
    this.depot = depot;
    this.chargingStations = chargingStations;
    this.customers = customers;
    this.batteryCapacity = batteryCapacity;
    this.loadCapacity = loadCapacity;
    this.chargingCapacityRate = chargingCapacityRate;
    this.inverseRechargingRate = inverseRechargingRate;
    this.averageVelocity = averageVelocity;
  }


  public Depot getDepot() {
    return depot;
  }

  public List<ChargingStations> getChargingStations() {
    return chargingStations;
  }

  public List<Customer> getCustomers() {
    return customers;
  }

  public double getBatteryCapacity() {
    return batteryCapacity;
  }

  public double getLoadCapacity() {
    return loadCapacity;
  }

  public double getChargeConsumptionRate() {
    return chargingCapacityRate;
  }

  public double getInverseRechargingRate() {
    return inverseRechargingRate;
  }

  public double getAverageVelocity() {
    return averageVelocity;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ProblemInstance that = (ProblemInstance) o;
    return Double.compare(that.batteryCapacity, batteryCapacity) == 0 &&
            Double.compare(that.loadCapacity, loadCapacity) == 0 &&
            Double.compare(that.chargingCapacityRate, chargingCapacityRate) == 0 &&
            Double.compare(that.inverseRechargingRate, inverseRechargingRate) == 0 &&
            Double.compare(that.averageVelocity, averageVelocity) == 0 &&
            Objects.equals(depot, that.depot) &&
            Objects.equals(chargingStations, that.chargingStations) &&
            Objects.equals(customers, that.customers);
  }

  @Override
  public int hashCode() {

    return Objects.hash(depot, chargingStations, customers, batteryCapacity, loadCapacity, chargingCapacityRate, inverseRechargingRate, averageVelocity);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("valueobjects.ProblemInstance{");
    sb.append("depot=").append(depot);
    sb.append(", chargingStations=").append(chargingStations);
    sb.append(", customers=").append(customers);
    sb.append(", batteryCapacity=").append(batteryCapacity);
    sb.append(", loadCapacity=").append(loadCapacity);
    sb.append(", chargingCapacityRate=").append(chargingCapacityRate);
    sb.append(", inverseRechargingRate=").append(inverseRechargingRate);
    sb.append(", averageVelocity=").append(averageVelocity);
    sb.append('}');
    return sb.toString();
  }
}
