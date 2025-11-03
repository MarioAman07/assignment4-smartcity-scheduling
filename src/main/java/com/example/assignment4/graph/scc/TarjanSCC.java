package com.example.assignment4.graph.scc;

import com.example.assignment4.graph.model.DirectedGraph;
import com.example.assignment4.graph.model.Edge;
import com.example.assignment4.metrics.PerformanceTracker;

import java.util.*;

public class TarjanSCC {

    private final DirectedGraph graph;
    private final PerformanceTracker tracker;
    private final List<List<Integer>> sccs;

    private int time;
    private final int[] disc;
    private final int[] low;
    private final Stack<Integer> stack;
    private final boolean[] onStack;

    public TarjanSCC(DirectedGraph graph, PerformanceTracker tracker) {
        this.graph = graph;
        this.tracker = tracker;
        this.sccs = new ArrayList<>();
        int n = graph.getN();

        this.disc = new int[n];
        this.low = new int[n];
        this.stack = new Stack<>();
        this.onStack = new boolean[n];

        Arrays.fill(disc, -1);
        Arrays.fill(low, -1);
    }

    public List<List<Integer>> findSCCs() {
        tracker.startTimer();

        for (int i = 0; i < graph.getN(); i++) {
            if (disc[i] == -1) {
                dfs(i);
            }
        }

        tracker.stopTimer();
        return sccs;
    }

    private void dfs(int u) {
        tracker.incrementCounter("SCC_DFS_VISITS");

        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;

        for (Edge edge : graph.getAdj(u)) {
            int v = edge.getV();

            tracker.incrementCounter("SCC_DFS_EDGES");

            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                scc.add(v);
            } while (u != v);
            sccs.add(scc);
        }
    }

    public List<List<Integer>> getSccs() {
        return sccs;
    }

    public List<Integer> getSccSizes() {
        List<Integer> sizes = new ArrayList<>();
        for (List<Integer> scc : sccs) {
            sizes.add(scc.size());
        }
        return sizes;
    }
}