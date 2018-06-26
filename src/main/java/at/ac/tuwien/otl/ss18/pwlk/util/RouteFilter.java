package at.ac.tuwien.otl.ss18.pwlk.util;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;

import java.util.ArrayList;
import java.util.List;

public class RouteFilter {

    public static List<Route> filterNodesInRoutes(final List<Route> routes) {
        final List<Route> routeSet = new ArrayList<>();
        for (final Route route : routes) {
            final List<AbstractNode> filteredNodes = filterNodes(route.getRoute());
            final double distance = route.getDistance();
            final Route filtered = new Route();
            filtered.setDistance(distance);
            filtered.setRoute(filteredNodes);
            routeSet.add(filtered);
        }
        return routeSet;
    }

    public static List<AbstractNode> filterNodes(final List<AbstractNode> route) {
        final List<AbstractNode> nodeSet = new ArrayList<>();
        AbstractNode previous = null;
        for (final AbstractNode node : route) {
            if (previous == null) {
                nodeSet.add(node);
            } else {
                if (!previous.getId().equalsIgnoreCase(node.getId())) {
                    nodeSet.add(node);
                }
            }
            previous = node;
        }
        return nodeSet;
    }

    public static void finalizeRoutes(final List<Route> routes) {
        int i = 0;
        for (final Route r : new ArrayList<>(routes)) {
            if (r.getRoute().size() == 2 || r.getRoute().size() == 1) {
                routes.remove(i--);
            }
            i++;
        }
    }
}
