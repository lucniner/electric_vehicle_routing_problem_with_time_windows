package at.ac.tuwien.otl.ss18.pwlk.dto;


import java.util.Objects;

public abstract class AbstractNode {

  protected final String id;
  protected final Location location;
  protected final double demand;
  protected final TimeWindow timeWindow;
  protected final double serviceTime;


  protected AbstractNode(final String id, final Location location, final double demand, final TimeWindow timeWindow, final double serviceTime) {
    this.id = id;
    this.location = location;
    this.demand = demand;
    this.timeWindow = timeWindow;
    this.serviceTime = serviceTime;
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
