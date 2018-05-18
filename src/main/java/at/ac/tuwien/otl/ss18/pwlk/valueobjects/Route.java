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
