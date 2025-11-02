package com.example.assignment4.graph.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class ComponentGraph {
    private final int numComponents;
    private final List<Edge> componentEdges;
    private final Map<Integer, List<Integer>> componentToNodes;

    public ComponentGraph(int numComponents, List<Edge> componentEdges, Map<Integer, List<Integer>> componentToNodes) {
        this.numComponents = numComponents;
        this.componentEdges = componentEdges;
        this.componentToNodes = componentToNodes;
    }

    public int getNumComponents() { return numComponents; }
    public List<Edge> getComponentEdges() { return componentEdges; }
    public List<Integer> getOriginalNodes(int componentId) { return componentToNodes.get(componentId); }

    public Map<Integer, List<Integer>> getOriginalNodesMap() {
        return componentToNodes;
    }

    public Map<Integer, List<Integer>> getAdjList() {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int i = 0; i < numComponents; i++) {
            adj.put(i, new ArrayList<>());
        }
        for (Edge edge : componentEdges) {
            adj.get(edge.getU()).add(edge.getV());
        }
        return adj;
    }
}