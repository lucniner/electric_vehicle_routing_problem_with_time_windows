package at.ac.tuwien.otl.ss18.pwlk.distance;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;

public class DistanceCalculator {

  private DistanceCalculator() {
  }

  public static double calculateDistanceBetweenNodes(final AbstractNode from, final AbstractNode to) {
    final double xDistance = Math.pow(from.getLocation().getX() - to.getLocation().getX(), 2);
    final double yDistance = Math.pow(from.getLocation().getY() - to.getLocation().getY(), 2);
    final double totalDistance = xDistance + yDistance;
    return Math.sqrt(totalDistance);
  }
}
