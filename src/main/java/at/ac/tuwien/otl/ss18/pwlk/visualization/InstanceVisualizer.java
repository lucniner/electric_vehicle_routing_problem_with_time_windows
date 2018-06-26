package at.ac.tuwien.otl.ss18.pwlk.visualization;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InstanceVisualizer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<String> colors = new ArrayList<>();
    private final ProblemInstance problem;
    private final SolutionInstance solution;
    private Map<AbstractNode, Node> nodeMap = new HashMap<>();


    public InstanceVisualizer(ProblemInstance problem, SolutionInstance solution) {
        this.problem = problem;
        this.solution = solution;
        colors.add("green");
        colors.add("red");
        colors.add("blue");
        colors.add("orange");
        colors.add("yellow");
        colors.add("brown");
        colors.add("black");
        colors.add("violette");
    }

    public void visualize() {
        Graph graph = new DefaultGraph("graph");

        final Node depotNode = graph.addNode("depo");
        depotNode.addAttribute("ui.style", "fill-color: blue;size: 15px;");
        depotNode.addAttribute("ui.label", "depot");
        depotNode.setAttribute("x", problem.getDepot().getLocation().getX());
        depotNode.setAttribute("y", problem.getDepot().getLocation().getY());
        nodeMap.put(problem.getDepot(), depotNode);

        for (final Customer c : problem.getCustomers()) {
            final Node customer = graph.addNode(c.getId());
            customer.addAttribute("ui.style", "fill-color: black;size: 15px;");
            customer.addAttribute("ui.label", c.getId() + " " + c.getTimeWindow());
            customer.setAttribute("x", c.getLocation().getX());
            customer.setAttribute("y", c.getLocation().getY());
            nodeMap.put(c, customer);
        }

        final List<ChargingStations> chargings = problem.getChargingStations();
        for (final ChargingStations f : chargings) {
            final Node fuel = graph.addNode(f.getId());
            fuel.addAttribute("ui.style", "fill-color: red;size: 15px;");
            fuel.addAttribute("ui.label", fuel.getId());
            fuel.setAttribute("x", f.getLocation().getX());
            fuel.setAttribute("y", f.getLocation().getY());
            nodeMap.put(f, fuel);
        }

        int colorCounter = 0;
        for (final Route route : solution.getRoutes()) {
            final String color = colors.get(colorCounter++ % colors.size());
            for (int i = 0; i < route.getRoute().size() - 1; i++) {
                AbstractNode current = route.getRoute().get(i);
                AbstractNode next = route.getRoute().get(i + 1);
                Node currentNode = nodeMap.get(current);
                Node nextNode = nodeMap.get(next);
                try {
                    if (currentNode != null && nextNode != null) {
                        Edge edge = graph.addEdge(UUID.randomUUID().toString(), currentNode, nextNode, true);
                        edge.addAttribute("ui.style", "fill-color: " + color + ";size: 4px;");
                    }

                } catch (EdgeRejectedException e) {
                    //ignore
                }
            }

        }
        graph.display(false);
    }
}
