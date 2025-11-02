package com.example.assignment4.graph.scc;

import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.graph.model.DirectedGraph;
import com.example.assignment4.graph.model.Edge;

import java.util.*;

public class CondensationGraphBuilder {

    public static ComponentGraph build(DirectedGraph originalGraph, List<List<Integer>> sccs) {

        int numSCCs = sccs.size();

        Map<Integer, Integer> nodeToComponentId = new HashMap<>();
        Map<Integer, List<Integer>> componentToNodes = new HashMap<>();

        for (int id = 0; id < numSCCs; id++) {
            List<Integer> scc = sccs.get(id);
            componentToNodes.put(id, scc);
            for (int node : scc) {
                nodeToComponentId.put(node, id);
            }
        }

        Set<String> uniqueComponentEdges = new HashSet<>();
        List<Edge> componentEdges = new ArrayList<>();

        for (Edge originalEdge : originalGraph.getEdges()) {
            int u = originalEdge.getU();
            int v = originalEdge.getV();

            int compU = nodeToComponentId.get(u);
            int compV = nodeToComponentId.get(v);

            if (compU != compV) {
                String edgeKey = compU + "->" + compV;

                if (!uniqueComponentEdges.contains(edgeKey)) {
                    componentEdges.add(new Edge(compU, compV, originalEdge.getWeight()));
                    uniqueComponentEdges.add(edgeKey);
                }
            }
        }

        return new ComponentGraph(numSCCs, componentEdges, componentToNodes);
    }
}