package com.example.assignment4.graph.dagsp;

import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.graph.model.Edge;
import com.example.assignment4.metrics.PerformanceTracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements Single-Source Shortest Path (SSSP) algorithm optimized for Directed Acyclic Graphs (DAGs).
 * The algorithm relies on processing nodes in topological order.
 */
public class ShortestPathDAG {

    private final ComponentGraph componentGraph;
    private final PerformanceTracker tracker;
    private final List<Integer> topologicalOrder;
    private final int sourceComponent;

    private final double[] dist;
    private final int[] parent;

    private static final double INF = Double.MAX_VALUE;

    public ShortestPathDAG(ComponentGraph graph, List<Integer> topoOrder, int originalSourceNode, PerformanceTracker tracker) {
        this.componentGraph = graph;
        this.tracker = tracker;
        this.topologicalOrder = topoOrder;

        int N = componentGraph.getNumComponents();
        this.dist = new double[N];
        this.parent = new int[N];

        this.sourceComponent = findComponentId(originalSourceNode);

        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
    }

    /**
     * Finds the Component ID containing the original source node.
     */
    public int findComponentId(int originalNode) {
        for (int id = 0; id < componentGraph.getNumComponents(); id++) {
            if (componentGraph.getOriginalNodes(id).contains(originalNode)) {
                return id;
            }
        }
        return -1;
    }

    /**
     * Executes the Shortest Path algorithm on the Component DAG.
     */
    public void computeShortestPaths() {
        if (sourceComponent == -1) {
            System.err.println("Error: Source node is not part of any component.");
            return;
        }

        tracker.startTimer();

        dist[sourceComponent] = 0;

        for (int u : topologicalOrder) {
            if (dist[u] != INF) {
                for (Edge edge : componentGraph.getComponentEdges()) {
                    if (edge.getU() == u) {
                        int v = edge.getV();
                        double weight = edge.getWeight();

                        tracker.incrementCounter("DAGSP_RELAXATIONS");

                        if (dist[u] + weight < dist[v]) {
                            dist[v] = dist[u] + weight;
                            parent[v] = u;
                        }
                    }
                }
            }
        }

        tracker.stopTimer();
    }

    /**
     * Reconstructs one optimal shortest path from the source to the target component.
     */
    public List<Integer> reconstructPath(int targetComponent) {
        List<Integer> path = new LinkedList<>();
        if (dist[targetComponent] == INF) {
            return Collections.emptyList();
        }

        int current = targetComponent;
        while (current != -1) {
            path.add(0, current);
            if (current == sourceComponent) break;
            current = parent[current];
        }

        if (path.isEmpty() || path.get(0) != sourceComponent) {
            return Collections.emptyList();
        }
        return path;
    }

    public double[] getDistances() { return dist; }
    public int getSourceComponent() { return sourceComponent; }
    public double getShortestDistance(int target) {
        return (target >= 0 && target < dist.length) ? dist[target] : INF;
    }
}