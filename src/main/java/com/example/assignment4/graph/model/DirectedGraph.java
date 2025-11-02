package com.example.assignment4.graph.model;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectedGraph {

    private final int N;
    private final List<Edge> allEdges;
    private final Map<Integer, List<Edge>> adj;
    private final int sourceNode;

    public DirectedGraph(int n, List<Edge> edges, int sourceNode) {
        this.N = n;
        this.allEdges = edges;
        this.adj = buildAdjacencyList(n, edges);
        this.sourceNode = sourceNode;
    }

    public int getN() { return N; }
    public int getE() { return allEdges.size(); }
    public int getSourceNode() { return sourceNode; }
    public List<Edge> getEdges() { return allEdges; }

    public List<Edge> getAdj(int u) {
        return adj.getOrDefault(u, new ArrayList<>());
    }

    private Map<Integer, List<Edge>> buildAdjacencyList(int n, List<Edge> edges) {
        Map<Integer, List<Edge>> adj = new HashMap<>();
        for (int i = 0; i < n; i++) {
            adj.put(i, new ArrayList<>());
        }
        for (Edge edge : edges) {
            if (edge.getU() >= 0 && edge.getU() < n) {
                adj.get(edge.getU()).add(edge);
            }
        }
        return adj;
    }

    public static DirectedGraph loadFromJson(String filePath) throws IOException {
        Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(filePath))) {

            class JsonGraphStructure {
                int n;
                List<Edge> edges;
                int source;
                String weight_model;
            }

            JsonGraphStructure jsonGraph = gson.fromJson(reader, JsonGraphStructure.class);

            if (jsonGraph.n <= 0) {
                throw new IllegalArgumentException("Graph must have N > 0 nodes.");
            }

            return new DirectedGraph(jsonGraph.n, jsonGraph.edges, jsonGraph.source);
        }
    }
}