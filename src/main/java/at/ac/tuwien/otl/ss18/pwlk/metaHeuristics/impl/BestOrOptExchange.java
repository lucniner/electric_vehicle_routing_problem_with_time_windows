package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Car;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class BestOrOptExchange {

    private final Logger logger = LoggerFactory.getLogger(getClass());


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

        singleOptExchange();
        twoOptExchange();
        threeOptExchange();

        if (routes.peek() != null) {
            return Optional.of(routes.peek());
        } else {
            return Optional.empty();
        }
    }

    private void singleOptExchange() {
        //starting at one because of depot and also ignoring depot at the end
        for (int i = 1; i < route.getRoute().size() - 1; i++) {
            for (int j = 1; j < route.getRoute().size() - 1; j++) {
                final Route opt = route.copyRoute();
                final List<AbstractNode> nodes = opt.getRoute();

                final AbstractNode exchangeNode = nodes.remove(i);
                nodes.add(j, exchangeNode);
                driveCar(nodes, opt);
            }
        }
    }

    private void twoOptExchange() {
        for (int i = 1; i < route.getRoute().size() - 2; i++) {
            for (int j = 1; j < route.getRoute().size() - 2; j++) {
                final Route opt = route.copyRoute();
                final List<AbstractNode> nodes = opt.getRoute();
                final AbstractNode exchangeNode = nodes.remove(i);
                final AbstractNode nextExchange = nodes.remove(i);
                nodes.add(j, exchangeNode);
                nodes.add(j + 1, nextExchange);
                driveCar(nodes, opt);
            }
        }
    }

    private void threeOptExchange() {
        for (int i = 1; i < route.getRoute().size() - 3; i++) {
            for (int j = 1; j < route.getRoute().size() - 3; j++) {
                final Route opt = route.copyRoute();
                final List<AbstractNode> nodes = opt.getRoute();

                final AbstractNode exchangeNode = nodes.remove(i);
                final AbstractNode nextExchange = nodes.remove(i);
                final AbstractNode furtherExchange = nodes.remove(i);
                nodes.add(j, exchangeNode);
                nodes.add(j + 1, nextExchange);
                nodes.add(j + 2, furtherExchange);
                driveCar(nodes, opt);
            }
        }
    }


    private void driveCar(final List<AbstractNode> nodes, final Route opt) {
        Car car = new Car(problemInstance, distanceHolder);
        try {
            car.driveRoute(nodes);
            opt.setDistance(car.getCurrentDistance());
            opt.setRoute(nodes);
            if (opt.getDistance() < route.getDistance()) {
                routes.add(opt);
            }
        } catch (BatteryViolationException e) {

        } catch (TimewindowViolationException e) {

        }
    }
}
