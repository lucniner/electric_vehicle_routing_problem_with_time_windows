package at.ac.tuwien.otl.ss18.pwlk.valueobjects;

import java.util.Objects;

public class TimeWindow {

  private final double readyTime;
  private final double dueTime;

  public TimeWindow(final double readyTime, final double dueTime) {
    this.readyTime = readyTime;
    this.dueTime = dueTime;
  }

  public boolean isInTimeWindow(final double time) {
    return readyTime <= time && isBeforeDueTime(time);
  }

  public boolean isBeforeDueTime(final double time) {
    return time <= dueTime;
  }

  public double getReadyTime() {
    return readyTime;
  }

  public double getDueTime() {
    return dueTime;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final TimeWindow that = (TimeWindow) o;
    return Double.compare(that.readyTime, readyTime) == 0 &&
            Double.compare(that.dueTime, dueTime) == 0;
  }

  @Override
  public int hashCode() {

    return Objects.hash(readyTime, dueTime);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("valueobjects.TimeWindow{");
    sb.append("readyTime=").append(readyTime);
    sb.append(", dueTime=").append(dueTime);
    sb.append('}');
    return sb.toString();
  }
}
