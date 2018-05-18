package at.ac.tuwien.otl.ss18.pwlk.reader;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;

class NodeInstanceReader {

  private final int index;
  private final String[] lineElements;

  private String id;
  private Location location;
  private double demand;
  private TimeWindow timeWindow;
  private double serviceTime;

  NodeInstanceReader(final int index, final String[] lineElements) {
    this.index = index;
    this.lineElements = lineElements;
  }

  Depot extractDepot() {
    parseLine();
    return new Depot(index, id, location, demand, timeWindow, serviceTime);
  }

  ChargingStations extractFuelStation() {
    parseLine();
    return new ChargingStations(index, id, location, demand, timeWindow, serviceTime);
  }

  Customer extractCustomer() {
    parseLine();
    return new Customer(index, id, location, demand, timeWindow, serviceTime);
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
