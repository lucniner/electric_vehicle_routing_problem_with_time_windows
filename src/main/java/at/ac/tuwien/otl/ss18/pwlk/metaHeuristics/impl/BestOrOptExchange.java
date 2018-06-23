package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Car;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class BestOrOptExchange {

    private final Route route;
    private final ProblemInstance problemInstance;
    private final DistanceHolder distanceHolder;
    private final PriorityQueue<Route> routes = new PriorityQueue<>();

    public BestOrOptExchange(Route route, ProblemInstance problemInstance, DistanceHolder distanceHolder) {
        this.route = route;
        this.problemInstance = problemInstance;
        this.distanceHolder = distanceHolder;
    }


    public Optional<Route> optimizeRoute() {

        //starting at one because of depot and also ignoring depot at the end
        for (int i = 1; i < route.getRoute().size() - 1; i++) {
            for (int j = 1; j < route.getRoute().size() - 1; j++) {
                final Route opt = route.copyRoute();
                final List<AbstractNode> nodes = opt.getRoute();

                final AbstractNode exchangeNode = nodes.remove(i);
                nodes.add(j, exchangeNode);

                Car car = new Car(problemInstance, distanceHolder);
                try {
                    car.driveRoute(nodes);
                    opt.setDistance(car.getCurrentDistance());

                    if (opt.getDistance() < route.getDistance()) {
                        routes.add(opt);
                    }
                } catch (BatteryViolationException | TimewindowViolationException e) {

                }
            }
        }


        if (routes.peek() != null) {
            return Optional.of(routes.peek());
        } else {
            return Optional.empty();
        }
    }
}
