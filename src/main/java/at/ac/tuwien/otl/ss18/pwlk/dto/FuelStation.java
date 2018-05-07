package at.ac.tuwien.otl.ss18.pwlk.dto;


public class FuelStation extends AbstractNode {


  public FuelStation(final String id, final Location location, final double demand, final TimeWindow timeWindow, final double serviceTime) {
    super(id, location, demand, timeWindow, serviceTime);
  }
}
