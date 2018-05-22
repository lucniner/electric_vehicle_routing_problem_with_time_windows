package at.ac.tuwien.otl.ss18.pwlk.valueobjects;

import java.util.*;

public class Route {
  private double distance;
  private List<AbstractNode> route = new LinkedList<>();

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public List<AbstractNode> getRoute() {
    return route;
  }

  public void setRoute(List<AbstractNode> route) {
    this.route = route;
  }

  public double getDemandOfRoute() {
    return route.stream().mapToDouble(AbstractNode::getDemand).sum();
  }

  public Optional<AbstractNode> getFirstCustomerInRoute() {
    return getFirstCustomer(route);
  }

  public Optional<AbstractNode> getLastCustomerInRoute() {
    final List<AbstractNode> copy = new ArrayList<>(route);
    Collections.reverse(copy);
    return getFirstCustomer(copy);
  }

  private Optional<AbstractNode> getFirstCustomer(final List<AbstractNode> route) {
    return route.stream().filter(n -> n instanceof Customer).findFirst();
  }

  public boolean routeContainsNode(final AbstractNode node) {
    return route.contains(node);
  }

  public Route copyInverseRoute() {
    Route route = new Route();
    route.distance = this.distance;
    route.route = new LinkedList<>(this.route);
    Collections.reverse(route.route);
    return route;
  }

  public Route copyRoute() {
    Route route = new Route();
    route.distance = this.distance;
    route.route = new LinkedList<>(this.route);
    return route;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Route route = (Route) o;
    return //Objects.equals(this.distance, route.distance) &&
            Objects.equals(this.route, route.route);
  }

  @Override
  public int hashCode() {
    return Objects.hash(route);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (AbstractNode abstractNode : route) {
      sb.append(abstractNode.getId());
      sb.append(", ");
    }
    return sb.substring(0, sb.length() - 2);
  }
}
