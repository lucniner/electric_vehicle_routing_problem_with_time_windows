package at.ac.tuwien.otl.ss18.pwlk.valueobjects;

import java.util.ArrayList;
import java.util.List;

public class SolutionInstance {
  List<Route> routes = new ArrayList<>();

  public List<Route> getRoutes() {
    return routes;
  }

  public void setRoutes(List<Route> routes) {
    this.routes = routes;
  }

  public double getDistanceSum() {
    return routes.stream().mapToDouble(Route::getDistance).sum();
  }

  public SolutionInstance copy() {
    SolutionInstance solutionInstance = new SolutionInstance();
    solutionInstance.routes = new ArrayList<>();

    for (Route route : routes) {
      solutionInstance.routes.add(route.copyRoute());
    }
    return solutionInstance;
  }
}
