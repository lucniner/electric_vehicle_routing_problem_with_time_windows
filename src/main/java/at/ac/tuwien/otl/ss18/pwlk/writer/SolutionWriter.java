package at.ac.tuwien.otl.ss18.pwlk.writer;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SolutionWriter {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private String path;
  private String instanceName;


  public SolutionWriter(final String path, final String instanceName) {
    this.path = path;
    this.instanceName = instanceName;
  }

  public void write(SolutionInstance solutionInstance) {
    logger.info("Write solution to file with name: " + path);

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(path));
      writer.write("# solution for " + instanceName + "\n");
      writer.write(String.format("%.3f", solutionInstance.getDistanceSum()) + "\n");
      for(Route route : solutionInstance.getRoutes()) {
        writer.write(route.toString() + "\n");
      }
      writer.close();
    } catch (IOException e) {
      logger.error("Error occured while trying to write solution to file: " + e.getLocalizedMessage());
    }
  }
}
