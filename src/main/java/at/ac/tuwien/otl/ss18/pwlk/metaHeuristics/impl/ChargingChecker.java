package at.ac.tuwien.otl.ss18.pwlk.metaHeuristics.impl;

import at.ac.tuwien.otl.ss18.pwlk.distance.DistanceHolder;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.BatteryViolationException;
import at.ac.tuwien.otl.ss18.pwlk.exceptions.TimewindowViolationException;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChargingChecker {


    private final ProblemInstance problemInstance;
    private final DistanceHolder distanceHolder;
    private final SolutionInstance solutionInstance;

    public ChargingChecker(ProblemInstance problemInstance, DistanceHolder distanceHolder, SolutionInstance solutionInstance) {
        this.problemInstance = problemInstance;
        this.distanceHolder = distanceHolder;
        this.solutionInstance = solutionInstance;
    }

    public SolutionInstance optimize() {
        final List<Route> routes = solutionInstance.getRoutes();
        routes.parallelStream().forEach(this::optimizeNodeSet);
        return solutionInstance;
    }

    private void optimizeNodeSet(final Route route) {
        final List<AbstractNode> nodes = new ArrayList<>(route.getRoute());
        if (routeHasChargingSation(nodes)) {
            removeChargingStationBetweenDepotAndFirstCustomer(nodes);
            try {
                driveCar(nodes);
            } catch (BatteryViolationException e) {
                insertChargingStationBetweenDepotAndFirstCustomer(nodes);
            } catch (TimewindowViolationException e) {

            }

            removeChargingStationBetweenLastCustomerAndDepot(nodes);
            try {
                driveCar(nodes);
            } catch (BatteryViolationException e) {
                insertChargingStationBetweenLastCustomerAndDepot(nodes);
            } catch (TimewindowViolationException e) {

            }
        }

        Car car = new Car(problemInstance, distanceHolder);
        try {
            car.driveRoute(nodes);
            if (car.getCurrentDistance() < route.getDistance()) {
                route.setRoute(nodes);
                route.setDistance(car.getCurrentDistance());
            }
        } catch (BatteryViolationException | TimewindowViolationException e) {

        }
    }

    private boolean routeHasChargingSation(final List<AbstractNode> route) {
        return route.stream().filter(n -> n instanceof ChargingStations).findFirst().isPresent();
    }

    private void removeChargingStationBetweenDepotAndFirstCustomer(final List<AbstractNode> route) {
        removeChargingStationsOnList(route);
    }

    private void removeChargingStationBetweenLastCustomerAndDepot(final List<AbstractNode> route) {
        final List<AbstractNode> copy = new ArrayList<>(route);
        Collections.reverse(copy);
        removeChargingStationsOnList(copy);
        Collections.reverse(copy);
        route.removeAll(route);
        route.addAll(copy);
    }

    private void removeChargingStationsOnList(final List<AbstractNode> route) {
        final Iterator<AbstractNode> iterator = route.iterator();
        while (iterator.hasNext()) {
            final AbstractNode node = iterator.next();
            if (node instanceof ChargingStations) {
                iterator.remove();
            } else if (node instanceof Customer) {
                break;
            }
        }
    }


    private double driveCar(final List<AbstractNode> route) throws BatteryViolationException, TimewindowViolationException {
        final Car car = new Car(problemInstance, distanceHolder);
        car.driveRoute(route);
        return car.getCurrentDistance();

    }

    private void insertChargingStationBetweenDepotAndFirstCustomer(final List<AbstractNode> route) {
        final AbstractNode chargingStation = distanceHolder.getNearestRechargingStationsForCustomerInDistance(route.get(1), route.get(0)).get(0).getKey();
        route.add(1, chargingStation);
    }

    private void insertChargingStationBetweenLastCustomerAndDepot(final List<AbstractNode> route) {
        final int depotIndex = route.size() - 1;
        final int lastCustomer = route.size() - 2;
        final AbstractNode chargingStation = distanceHolder.getNearestRechargingStationsForCustomerInDistance(route.get(lastCustomer), route.get(depotIndex)).get(0).getKey();
        route.add(depotIndex, chargingStation);
    }


    private int getIndexOfFirstCustomer(final List<AbstractNode> route) {
        int index = 0;
        for (final AbstractNode n : route) {
            if (n instanceof Customer) {
                return index;
            }
            index++;
        }
        return index;
    }
}
