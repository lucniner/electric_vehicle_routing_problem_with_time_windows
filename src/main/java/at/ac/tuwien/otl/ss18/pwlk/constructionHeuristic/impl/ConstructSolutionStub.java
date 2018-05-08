package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.impl;

import at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic.IConstructSolution;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.AbstractNode;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.Route;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;

public class ConstructSolutionStub implements IConstructSolution {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public SolutionInstance constructSolution(ProblemInstance problemInstance) {
        logger.info("Construct solution with algorithm 'Stub'");
        //TODO delete this stub code and implement algorithm
        SolutionInstance solutionInstance = new SolutionInstance();
        ArrayList<Route> list = new ArrayList<>();

        LinkedList<AbstractNode> routeList = new LinkedList<>();
        routeList.add(problemInstance.getDepot());
        routeList.add(problemInstance.getCustomers().get(0));
        routeList.add(problemInstance.getDepot());
        Route route = new Route();
        route.setDistance(12.122);
        route.setRoute(routeList);

        LinkedList<AbstractNode> routeList2 = new LinkedList<>();
        routeList2.add(problemInstance.getDepot());
        routeList2.add(problemInstance.getCustomers().get(0));
        routeList2.add(problemInstance.getDepot());
        Route route2 = new Route();
        route2.setDistance(14.1228);
        route2.setRoute(routeList2);

        list.add(route);
        list.add(route2);
        solutionInstance.setRoutes(list);

        return solutionInstance;
    }
}
