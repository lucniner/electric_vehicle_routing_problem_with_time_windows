package at.ac.tuwien.otl.ss18.pwlk.scenario;

import at.ac.tuwien.otl.ss18.pwlk.dto.*;

public class NodeInstanceReader {

  private final String[] lineElements;

  private String id;
  private Location location;
  private double demand;
  private TimeWindow timeWindow;
  private double serviceTime;

  public NodeInstanceReader(final String[] lineElements) {
    this.lineElements = lineElements;
  }

  public Depot extractDepot() {
    parseLine();
    return new Depot(id, location, demand, timeWindow, serviceTime);
  }

  public FuelStation extractFuelStation() {
    parseLine();
    return new FuelStation(id, location, demand, timeWindow, serviceTime);
  }

  public Customer extractCustomer() {
    parseLine();
    return new Customer(id, location, demand, timeWindow, serviceTime);
  }

  private void parseLine() {
    id = extractId(lineElements);
    location = extractLocation(lineElements);
    demand = extractDemand(lineElements);
    timeWindow = extractTimeWindow(lineElements);
    serviceTime = extractServiceTime(lineElements);
  }

  private String extractId(final String[] lineElements) {
    return lineElements[0];
  }

  private Location extractLocation(final String[] lineElements) {
    final double xLocation = Double.parseDouble(lineElements[2]);
    final double yLocation = Double.parseDouble(lineElements[3]);
    return new Location(xLocation, yLocation);
  }

  private double extractDemand(final String[] lineElements) {
    return Double.parseDouble(lineElements[4]);
  }

  private TimeWindow extractTimeWindow(final String[] lineElements) {
    final double readyTime = Double.parseDouble(lineElements[5]);
    final double endTime = Double.parseDouble(lineElements[6]);
    return new TimeWindow(readyTime, endTime);
  }

  private double extractServiceTime(final String[] lineElements) {
    return Double.parseDouble(lineElements[7]);
  }
}
