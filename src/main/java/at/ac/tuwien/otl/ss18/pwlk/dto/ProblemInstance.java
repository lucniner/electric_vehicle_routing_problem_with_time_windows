package at.ac.tuwien.otl.ss18.pwlk.dto;

import java.util.List;
import java.util.Objects;

public class ProblemInstance {

  private final Depot depot;
  private final List<FuelStation> fuelStations;
  private final List<Customer> customers;
  private final double fuelTankCapacity;
  private final double loadCapacity;
  private final double fuelConsumptionRate;
  private final double inverseRefuelingRate;
  private final double averageVelocity;

  public ProblemInstance(final Depot depot, final List<FuelStation> fuelStations, final List<Customer> customers, final double fuelTankCapacity, final double loadCapacity, final double fuelConsumptionRate, final double inverseRefuelingRate, final double averageVelocity) {
    this.depot = depot;
    this.fuelStations = fuelStations;
    this.customers = customers;
    this.fuelTankCapacity = fuelTankCapacity;
    this.loadCapacity = loadCapacity;
    this.fuelConsumptionRate = fuelConsumptionRate;
    this.inverseRefuelingRate = inverseRefuelingRate;
    this.averageVelocity = averageVelocity;
  }


  public Depot getDepot() {
    return depot;
  }

  public List<FuelStation> getFuelStations() {
    return fuelStations;
  }

  public List<Customer> getCustomers() {
    return customers;
  }

  public double getFuelTankCapacity() {
    return fuelTankCapacity;
  }

  public double getLoadCapacity() {
    return loadCapacity;
  }

  public double getFuelConsumptionRate() {
    return fuelConsumptionRate;
  }

  public double getInverseRefuelingRate() {
    return inverseRefuelingRate;
  }

  public double getAverageVelocity() {
    return averageVelocity;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ProblemInstance that = (ProblemInstance) o;
    return Double.compare(that.fuelTankCapacity, fuelTankCapacity) == 0 &&
            Double.compare(that.loadCapacity, loadCapacity) == 0 &&
            Double.compare(that.fuelConsumptionRate, fuelConsumptionRate) == 0 &&
            Double.compare(that.inverseRefuelingRate, inverseRefuelingRate) == 0 &&
            Double.compare(that.averageVelocity, averageVelocity) == 0 &&
            Objects.equals(depot, that.depot) &&
            Objects.equals(fuelStations, that.fuelStations) &&
            Objects.equals(customers, that.customers);
  }

  @Override
  public int hashCode() {

    return Objects.hash(depot, fuelStations, customers, fuelTankCapacity, loadCapacity, fuelConsumptionRate, inverseRefuelingRate, averageVelocity);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("valueobjects.ProblemInstance{");
    sb.append("depot=").append(depot);
    sb.append(", fuelStations=").append(fuelStations);
    sb.append(", customers=").append(customers);
    sb.append(", fuelTankCapacity=").append(fuelTankCapacity);
    sb.append(", loadCapacity=").append(loadCapacity);
    sb.append(", fuelConsumptionRate=").append(fuelConsumptionRate);
    sb.append(", inverseRefuelingRate=").append(inverseRefuelingRate);
    sb.append(", averageVelocity=").append(averageVelocity);
    sb.append('}');
    return sb.toString();
  }
}
