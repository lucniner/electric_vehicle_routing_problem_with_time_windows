package at.ac.tuwien.otl.ss18.pwlk.valueobjects;


public class Customer extends AbstractNode {


  public Customer(final int index, final String id, final Location location, final double demand, final TimeWindow timeWindow, final double serviceTime) {
    super(index, id, location, demand, timeWindow, serviceTime);
  }
}
