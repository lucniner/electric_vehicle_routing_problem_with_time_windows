package at.ac.tuwien.otl.ss18.pwlk.reader;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Customer;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Depot;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.FuelStation;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class ProblemReader {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String instanceFilePath;
  private Depot depot;
  private List<FuelStation> fuelStations = new LinkedList<>();
  private List<Customer> customers = new LinkedList<>();
  private double fuelTankCapacity;
  private double loadCapacity;
  private double fuelConsumptionRate;
  private double inverseRefuelingRate;
  private double averageVelocity;

  public ProblemReader(final String instanceFilePath) {
    this.instanceFilePath = instanceFilePath;
  }

  public ProblemInstance retrieveProblemInstance() throws IOException {
    logger.info("Read instance from path: " + instanceFilePath);
    try (BufferedReader reader =
                 new BufferedReader(
                         new InputStreamReader(
                                 this.getClass().getClassLoader().getResourceAsStream(instanceFilePath)))) {
      reader.readLine(); // skip header
      parseNodes(reader);
      parseConstraints(reader);
    }
    return new ProblemInstance(
            depot,
            fuelStations,
            customers,
            fuelTankCapacity,
            loadCapacity,
            fuelConsumptionRate,
            inverseRefuelingRate,
            averageVelocity);
  }

  private void parseNodes(final BufferedReader reader) throws IOException {
    String line = reader.readLine();
    int index = 0;
    while (line != null && !line.startsWith("Q") && !line.trim().isEmpty()) {
      handleNode(line, index);
      index++;
      line = reader.readLine();
    }
  }

  private void parseConstraints(final BufferedReader reader) throws IOException {
    String line = reader.readLine();
    while (line != null) {
      handleProblemInstanceConstraints(line);
      line = reader.readLine();
    }
  }

  private void handleNode(final String line, final int index) {
    final String formattedLine = line.replaceAll("\\s+", " ");
    final String[] lineElements = formattedLine.split(" ");
    final String firstElementInLine = lineElements[0];
    final NodeInstanceReader reader = new NodeInstanceReader(index, lineElements);

    if (firstElementInLine.startsWith("D")) {
      depot = reader.extractDepot();
    } else if (firstElementInLine.startsWith("S")) {
      fuelStations.add(reader.extractFuelStation());
    } else {
      customers.add(reader.extractCustomer());
    }
  }

  private void handleProblemInstanceConstraints(final String line) {
    if (line.startsWith("Q")) {
      fuelTankCapacity = extractConstraintValue(line);
    } else if (line.startsWith("C")) {
      loadCapacity = extractConstraintValue(line);
    } else if (line.startsWith("r")) {
      fuelConsumptionRate = extractConstraintValue(line);
    } else if (line.startsWith("g")) {
      inverseRefuelingRate = extractConstraintValue(line);
    } else {
      averageVelocity = extractConstraintValue(line);
    }
  }

  private double extractConstraintValue(final String line) {
    final String embeddedValue = line.substring(line.indexOf('/') + 1, line.lastIndexOf('/'));
    return Double.parseDouble(embeddedValue);
  }
}
