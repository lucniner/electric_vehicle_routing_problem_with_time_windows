package at.ac.tuwien.otl.ss18.pwlk.valueobjects;


import java.util.Objects;

public abstract class AbstractNode {

  private final int index;
  private final String id;
  private final Location location;
  private final double demand;
  private final TimeWindow timeWindow;
  private final double serviceTime;


  AbstractNode(final int index, final String id, final Location location, final double demand, final TimeWindow timeWindow, final double serviceTime) {
    this.index = index;
    this.id = id;
    this.location = location;
    this.demand = demand;
    this.timeWindow = timeWindow;
    this.serviceTime = serviceTime;
  }

  public String getId() {
    return id;
  }

  public Location getLocation() {
    return location;
  }

  public double getDemand() {
    return demand;
  }

  public TimeWindow getTimeWindow() {
    return timeWindow;
  }

  public double getServiceTime() {
    return serviceTime;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final AbstractNode that = (AbstractNode) o;
    return Double.compare(that.demand, demand) == 0 &&
            Double.compare(that.serviceTime, serviceTime) == 0 &&
            Objects.equals(id, that.id) &&
            Objects.equals(location, that.location) &&
            Objects.equals(timeWindow, that.timeWindow);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, location, demand, timeWindow, serviceTime);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("valueobjects.AbstractNode{");
    sb.append("id='").append(id).append('\'');
    sb.append(", location=").append(location);
    sb.append(", demand=").append(demand);
    sb.append(", timeWindow=").append(timeWindow);
    sb.append(", serviceTime=").append(serviceTime);
    sb.append('}');
    return sb.toString();
  }
}
