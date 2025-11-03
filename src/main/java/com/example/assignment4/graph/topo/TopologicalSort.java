package com.example.assignment4.graph.topo;

import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.metrics.PerformanceTracker;

import java.util.*;

public class TopologicalSort {

    private final ComponentGraph componentGraph;
    private final PerformanceTracker tracker;
    private final Deque<Integer> topologicalOrder;
    private final boolean[] visited;

    public TopologicalSort(ComponentGraph componentGraph, PerformanceTracker tracker) {
        this.componentGraph = componentGraph;
        this.tracker = tracker;
        int N = componentGraph.getNumComponents();

        this.topologicalOrder = new ArrayDeque<>();
        this.visited = new boolean[N];
    }

    public List<Integer> computeComponentOrder() {
        tracker.startTimer();

        Map<Integer, List<Integer>> adj = componentGraph.getAdjList();
        int N = componentGraph.getNumComponents();

        for (int i = 0; i < N; i++) {
            if (!visited[i]) {
                dfs(i, adj);
            }
        }

        tracker.stopTimer();

        return new ArrayList<>(topologicalOrder);
    }

    private void dfs(int u, Map<Integer, List<Integer>> adj) {
        visited[u] = true;
        tracker.incrementCounter("TOPO_DFS_VISITS");

        for (int v : adj.getOrDefault(u, Collections.emptyList())) {
            if (!visited[v]) {
                dfs(v, adj);
            }
        }

        topologicalOrder.push(u);
    }

    public List<Integer> deriveTaskOrder(List<Integer> componentOrder) {
        List<Integer> taskOrder = new ArrayList<>();

        for (int componentId : componentOrder) {
            List<Integer> originalNodes = componentGraph.getOriginalNodes(componentId);
            taskOrder.addAll(originalNodes);
        }

        return taskOrder;
    }
}