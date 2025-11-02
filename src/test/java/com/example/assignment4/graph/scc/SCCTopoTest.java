package com.example.assignment4.graph.scc;

import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.graph.model.DirectedGraph;
import com.example.assignment4.graph.model.Edge;
import com.example.assignment4.graph.topo.TopologicalSort;
import com.example.assignment4.metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SCCTopoTest {

    private DirectedGraph createGraph(int n, List<Edge> edges) {
        return new DirectedGraph(n, edges, 0);
    }

    // --- TESTS FOR SCC ---

    @Test
    void testPureDAG() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1), new Edge(1, 2, 1), new Edge(2, 3, 1)
        );
        DirectedGraph graph = createGraph(4, edges);

        TarjanSCC sccFinder = new TarjanSCC(graph, new PerformanceTracker());
        List<List<Integer>> sccs = sccFinder.findSCCs();

        assertEquals(4, sccs.size());

        assertTrue(sccs.stream().allMatch(scc -> scc.size() == 1));
    }

    @Test
    void testFullCycle() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1), new Edge(1, 2, 1), new Edge(2, 0, 1)
        );
        DirectedGraph graph = createGraph(3, edges);

        TarjanSCC sccFinder = new TarjanSCC(graph, new PerformanceTracker());
        List<List<Integer>> sccs = sccFinder.findSCCs();

        assertEquals(1, sccs.size());

        assertEquals(3, sccs.get(0).size());
    }

    @Test
    void testMultipleSCCs() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1), new Edge(1, 0, 1),
                new Edge(1, 2, 1),
                new Edge(2, 3, 1),
                new Edge(3, 4, 1), new Edge(4, 3, 1)  // SCC 2: {3, 4}
        );
        DirectedGraph graph = createGraph(5, edges);

        TarjanSCC sccFinder = new TarjanSCC(graph, new PerformanceTracker());
        List<List<Integer>> sccs = sccFinder.findSCCs();

        assertEquals(3, sccs.size());

        List<Integer> sizes = sccs.stream().map(List::size).collect(Collectors.toList());
        assertTrue(sizes.containsAll(Arrays.asList(2, 1, 2)));
    }

    // --- TESTS FOR CONDENSATION GRAPHS AND TOPO SORT ---

    @Test
    void testCondensationGraphAndTopoOrder() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1),
                new Edge(1, 2, 1), new Edge(2, 1, 1), // SCC {1, 2}
                new Edge(2, 3, 1),
                new Edge(4, 5, 1)
        );
        DirectedGraph graph = createGraph(6, edges);

        TarjanSCC sccFinder = new TarjanSCC(graph, new PerformanceTracker());
        List<List<Integer>> sccs = sccFinder.findSCCs();

        ComponentGraph cGraph = CondensationGraphBuilder.build(graph, sccs);

        assertEquals(5, cGraph.getNumComponents());

        TopologicalSort sorter = new TopologicalSort(cGraph, new PerformanceTracker());
        List<Integer> componentOrder = sorter.computeComponentOrder();

        Map<Integer, List<Integer>> componentMap = cGraph.getOriginalNodesMap();

        // --- VALIDITY CHECK OF THE ORDER ---

        assertEquals(5, componentOrder.size()); // 5 компонентов


        Map<Integer, Integer> orderIndex = new HashMap<>();
        for (int i = 0; i < componentOrder.size(); i++) {
            orderIndex.put(componentOrder.get(i), i);
        }

        for (Edge edge : cGraph.getComponentEdges()) {
            int compU = edge.getU();
            int compV = edge.getV();

            assertTrue(orderIndex.get(compU) < orderIndex.get(compV),
                    "Topological order failed: Component " + compU + " must come before " + compV);
        }
    }

    @Test
    void testDerivedTaskOrder() {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 1), new Edge(1, 2, 1), new Edge(2, 1, 1),
                new Edge(2, 3, 1), new Edge(4, 5, 1)
        );
        DirectedGraph graph = createGraph(6, edges);

        TarjanSCC sccFinder = new TarjanSCC(graph, new PerformanceTracker());
        List<List<Integer>> sccs = sccFinder.findSCCs();
        ComponentGraph cGraph = CondensationGraphBuilder.build(graph, sccs);
        TopologicalSort sorter = new TopologicalSort(cGraph, new PerformanceTracker());
        List<Integer> componentOrder = sorter.computeComponentOrder();

        List<Integer> taskOrder = sorter.deriveTaskOrder(componentOrder);

        assertEquals(6, taskOrder.size());

        assertEquals(6, taskOrder.stream().distinct().count());

        int index0 = taskOrder.indexOf(0);
        int index3 = taskOrder.indexOf(3);
        assertTrue(index0 < index3, "Task 0 must be scheduled before task 3.");
    }

    @Test
    void testEmptyGraph() {
        DirectedGraph graph = createGraph(0, Collections.emptyList());
        TarjanSCC sccFinder = new TarjanSCC(graph, new PerformanceTracker());
        List<List<Integer>> sccs = sccFinder.findSCCs();

        assertTrue(sccs.isEmpty());
    }
}