package com.example.assignment4.graph.dagsp;

import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.graph.model.Edge;
import com.example.assignment4.metrics.PerformanceTracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LongestPathDAG {

    private final ComponentGraph componentGraph;
    private final PerformanceTracker tracker;
    private final List<Integer> topologicalOrder;

    private final double[] dist;
    private final int[] parent;

    private static final double N_INF = Double.NEGATIVE_INFINITY;

    private int criticalPathStartNode = -1;
    private int criticalPathEndNode = -1;
    private double criticalPathLength = N_INF;

    public LongestPathDAG(ComponentGraph graph, List<Integer> topoOrder, PerformanceTracker tracker) {
        this.componentGraph = graph;
        this.tracker = tracker;
        this.topologicalOrder = topoOrder;

        int N = componentGraph.getNumComponents();
        this.dist = new double[N];
        this.parent = new int[N];

        Arrays.fill(dist, N_INF);
        Arrays.fill(parent, -1);
    }

    public void computeLongestPaths() {
        tracker.startTimer();

        Map<Integer, List<Integer>> adj = componentGraph.getAdjList();
        int[] inDegree = new int[componentGraph.getNumComponents()];
        for (List<Integer> neighbors : adj.values()) {
            for (int v : neighbors) {
                inDegree[v]++;
            }
        }

        for (int i = 0; i < componentGraph.getNumComponents(); i++) {
            if (inDegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int u : topologicalOrder) {
            if (dist[u] != N_INF) {
                for (Edge edge : componentGraph.getComponentEdges()) {
                    if (edge.getU() == u) {
                        int v = edge.getV();
                        double weight = edge.getWeight();

                        tracker.incrementCounter("DAGSP_RELAXATIONS");

                        if (dist[u] + weight > dist[v]) {
                            dist[v] = dist[u] + weight;
                            parent[v] = u;
                        }
                    }
                }
            }
        }

        tracker.stopTimer();

        findCriticalPathMax();
    }

    private void findCriticalPathMax() {
        double maxDist = N_INF;
        int endNode = -1;

        for (int i = 0; i < dist.length; i++) {
            if (dist[i] > maxDist) {
                maxDist = dist[i];
                endNode = i;
            }
        }

        this.criticalPathLength = maxDist;
        this.criticalPathEndNode = endNode;

        if (endNode != -1) {
            List<Integer> path = reconstructPath(endNode);
            if (!path.isEmpty()) {
                this.criticalPathStartNode = path.get(0);
            }
        }
    }

    public List<Integer> reconstructPath(int targetComponent) {
        List<Integer> path = new LinkedList<>();
        if (targetComponent == -1 || dist[targetComponent] == N_INF) {
            return Collections.emptyList();
        }

        int current = targetComponent;
        while (current != -1 && parent[current] != 0) {
            path.add(0, current);
            if (dist[current] == 0) break;
            current = parent[current];
        }

        if (current != -1) {
            path.add(0, current);
        }

        return path;
    }

    public double getCriticalPathLength() { return criticalPathLength; }
    public List<Integer> getCriticalPath() { return reconstructPath(criticalPathEndNode); }
}