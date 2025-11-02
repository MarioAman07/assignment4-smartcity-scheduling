package com.example.assignment4.graph.dagsp;

import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.graph.model.DirectedGraph;
import com.example.assignment4.graph.model.Edge;
import com.example.assignment4.graph.scc.CondensationGraphBuilder;
import com.example.assignment4.graph.scc.TarjanSCC;
import com.example.assignment4.graph.topo.TopologicalSort;
import com.example.assignment4.metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShortestLongestPathTest {

    private static final double EPSILON = 1e-9;

    private ComponentGraph buildAndSortGraph(DirectedGraph graph, List<Integer> topoOrderHolder) {
        PerformanceTracker tracker = new PerformanceTracker();

        TarjanSCC sccFinder = new TarjanSCC(graph, tracker);
        List<List<Integer>> sccs = sccFinder.findSCCs();

        ComponentGraph cGraph = CondensationGraphBuilder.build(graph, sccs);

        TopologicalSort sorter = new TopologicalSort(cGraph, tracker);
        List<Integer> componentOrder = sorter.computeComponentOrder();

        topoOrderHolder.addAll(componentOrder);

        return cGraph;
    }

    @Test
    void testShortestPath() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 10), new Edge(0, 2, 1), new Edge(2, 1, 1)
        );
        DirectedGraph graph = new DirectedGraph(3, edges, 0);
        List<Integer> topoOrder = new java.util.ArrayList<>();

        ComponentGraph cGraph = buildAndSortGraph(graph, topoOrder);

        ShortestPathDAG spFinder = new ShortestPathDAG(cGraph, topoOrder, 0, new PerformanceTracker());
        spFinder.computeShortestPaths();

        int targetComponent = spFinder.findComponentId(1);
        assertEquals(2.0, spFinder.getShortestDistance(targetComponent), EPSILON,
                "Shortest distance to node 1 should be 2.0");

        List<Integer> path = spFinder.reconstructPath(targetComponent);

        assertEquals(3, path.size(), "Shortest path should have 3 components.");
    }

    @Test
    void testLongestPath() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1), new Edge(0, 2, 10), new Edge(1, 3, 1), new Edge(2, 3, 1)
        );
        DirectedGraph graph = new DirectedGraph(4, edges, 0);
        List<Integer> topoOrder = new java.util.ArrayList<>();

        ComponentGraph cGraph = buildAndSortGraph(graph, topoOrder);

        LongestPathDAG lpFinder = new LongestPathDAG(cGraph, topoOrder, new PerformanceTracker());
        lpFinder.computeLongestPaths();

        assertEquals(11.0, lpFinder.getCriticalPathLength(), EPSILON,
                "Critical path length should be 11.0");

        List<Integer> criticalPath = lpFinder.getCriticalPath();

        assertEquals(3, criticalPath.size(), "Critical path should have 3 components.");

        int comp0 = lpFinder.findComponentId(0);
        int comp3 = lpFinder.findComponentId(3);

        assertEquals(comp0, criticalPath.get(0), "Critical path must start at component 0.");
        assertEquals(comp3, criticalPath.get(criticalPath.size() - 1), "Critical path must end at component 3.");
    }

    @Test
    void testCriticalPathWithCycle() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1),
                new Edge(1, 2, 10), new Edge(2, 1, 5),
                new Edge(2, 3, 1)
        );
        DirectedGraph graph = new DirectedGraph(4, edges, 0);
        List<Integer> topoOrder = new java.util.ArrayList<>();

        ComponentGraph cGraph = buildAndSortGraph(graph, topoOrder);

        LongestPathDAG lpFinder = new LongestPathDAG(cGraph, topoOrder, new PerformanceTracker());
        lpFinder.computeLongestPaths();

        assertEquals(2.0, lpFinder.getCriticalPathLength(), EPSILON,
                "Critical path length in condensation graph should be 2.0");

        List<Integer> criticalPath = lpFinder.getCriticalPath();
        assertEquals(3, criticalPath.size(), "Critical path must be 3 components long.");
    }
}