package at.ac.tuwien.otl.ss18.pwlk.valueobjects;

import java.util.Objects;

public class Location {

  private final double x;
  private final double y;

  public Location(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Location location = (Location) o;
    return Double.compare(location.x, x) == 0 &&
            Double.compare(location.y, y) == 0;
  }

  @Override
  public int hashCode() {

    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Location{");
    sb.append("x=").append(x);
    sb.append(", y=").append(y);
    sb.append('}');
    return sb.toString();
  }
}
