package at.ac.tuwien.otl.ss18.pwlk.valueobjects;


public class Depot extends AbstractNode {


  public Depot(final int index, final String id, final Location location, final double demand, final TimeWindow timeWindow, final double serviceTime) {
    super(index, id, location, demand, timeWindow, serviceTime);
  }
}
