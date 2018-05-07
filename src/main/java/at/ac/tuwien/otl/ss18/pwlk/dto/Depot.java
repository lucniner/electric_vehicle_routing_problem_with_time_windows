package at.ac.tuwien.otl.ss18.pwlk.dto;


public class Depot extends AbstractNode {


  public Depot(final String id, final Location location, final double demand, final TimeWindow timeWindow, final double serviceTime) {
    super(id, location, demand, timeWindow, serviceTime);
  }
}
