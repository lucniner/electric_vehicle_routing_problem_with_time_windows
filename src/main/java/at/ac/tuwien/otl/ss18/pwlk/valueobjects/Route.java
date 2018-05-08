package at.ac.tuwien.otl.ss18.pwlk.valueobjects;

import java.util.LinkedList;
import java.util.List;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(AbstractNode abstractNode : route) {
            sb.append(abstractNode.getId());
            sb.append(", ");
        }
        return sb.substring(0, sb.length()-2);
    }
}
