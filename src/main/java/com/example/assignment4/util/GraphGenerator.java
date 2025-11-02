package com.example.assignment4.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.assignment4.graph.model.Edge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphGenerator {

    private static final String DATA_DIR = "data/";
    private final Random rand = new Random();
    private final StringBuilder summaryBuffer = new StringBuilder();

    private static class GraphJson {
        boolean directed = true;
        int n;
        List<Edge> edges;
        int source;
        String weight_model = "edge";
        public GraphJson(int n, List<Edge> edges, int source) {
            this.n = n;
            this.edges = edges;
            this.source = source;
        }
    }

    private enum Density { SPARSE, DENSE }
    private enum GraphType { DAG, CYCLIC_SIMPLE, CYCLIC_MULTI_SCC }

    private static final int MAX_WEIGHT = 20;

    public void generateAllDatasets() {
        new File(DATA_DIR).mkdirs();

        summaryBuffer.append("=== DATASET SUMMARY (9 datasets) ===\n");
        summaryBuffer.append("Filename\t\tn\tedges\tdensity\tcycles\n");

        System.out.println("--- Regenerating 9 Datasets ---");

        // SMALL (n=6-10, 3 variants)
        generateGraph(8, "small_dag_sparse.json", GraphType.DAG, Density.SPARSE, 0);
        generateGraph(7, "small_dag_dense.json", GraphType.DAG, Density.DENSE, 0);
        generateGraph(9, "small_cyclic_mixed.json", GraphType.CYCLIC_SIMPLE, Density.SPARSE, 0);

        // MEDIUM (n=10-20, 3 variants)
        generateGraph(15, "medium_dag_sparse.json", GraphType.DAG, Density.SPARSE, 0);
        generateGraph(12, "medium_cyclic_multi_scc.json", GraphType.CYCLIC_MULTI_SCC, Density.DENSE, 0);
        generateGraph(20, "medium_dag_dense.json", GraphType.DAG, Density.DENSE, 0);

        // LARGE (n=20-50, 3 variants)
        generateGraph(30, "large_cyclic_sparse.json", GraphType.CYCLIC_SIMPLE, Density.SPARSE, 0);
        generateGraph(45, "large_dag_dense.json", GraphType.DAG, Density.DENSE, 0);
        generateGraph(50, "large_cyclic_multi_scc.json", GraphType.CYCLIC_MULTI_SCC, Density.DENSE, 0);

        writeSummaryFile();
        System.out.println("--- Generation Complete. Summary written to data/DATASET_SUMMARY.txt ---");
    }

    private void generateGraph(int n, String filename, GraphType type, Density density, int source) {
        int maxEdges = n * (n - 1);
        int targetEdges;

        if (density == Density.SPARSE) {
            targetEdges = (int) (1.5 * n) + 2;
        } else {
            targetEdges = Math.min(maxEdges, n * n / 4);
        }

        List<Edge> edges = createEdges(n, targetEdges, type);
        GraphJson graph = new GraphJson(n, edges, source);
        writeJson(graph, filename);

        double actualDensity = (double) edges.size() / maxEdges;
        int cycleCount = (type == GraphType.DAG) ? 0 :
                (type == GraphType.CYCLIC_SIMPLE) ? 1 : 2;

        summaryBuffer.append(String.format("%s\tn=%d\tedges=%d\tdensity=%.2f\tcycles=%d\n",
                filename, n, edges.size(), actualDensity, cycleCount));
    }

    private List<Edge> createEdges(int n, int targetEdges, GraphType type) {
        List<Edge> edges = new ArrayList<>();
        Set<String> existingEdges = new HashSet<>();

        for (int i = 0; i < n - 1; i++) {
            addEdge(edges, existingEdges, i, i + 1, rand.nextInt(MAX_WEIGHT) + 1);
        }

        if (type == GraphType.CYCLIC_SIMPLE) {
            addEdge(edges, existingEdges, 0, 1, rand.nextInt(MAX_WEIGHT) + 1);
            addEdge(edges, existingEdges, 1, 0, rand.nextInt(MAX_WEIGHT) + 1);
        }
        else if (type == GraphType.CYCLIC_MULTI_SCC) {
            addEdge(edges, existingEdges, 0, 1, rand.nextInt(MAX_WEIGHT) + 1);
            addEdge(edges, existingEdges, 1, 0, rand.nextInt(MAX_WEIGHT) + 1);

            int mid = n/2;
            addEdge(edges, existingEdges, mid, mid + 1, rand.nextInt(MAX_WEIGHT) + 1);
            addEdge(edges, existingEdges, mid + 1, mid, rand.nextInt(MAX_WEIGHT) + 1);

            addEdge(edges, existingEdges, 2, mid, rand.nextInt(MAX_WEIGHT) + 1);
        }

        while (edges.size() < targetEdges) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);

            if (u == v) continue;

            if (type == GraphType.DAG) {
            }

            addEdge(edges, existingEdges, u, v, rand.nextInt(MAX_WEIGHT) + 1);
        }

        if (type == GraphType.DAG) {
            List<Edge> finalEdges = new ArrayList<>();
            for (Edge edge : edges) {
                if (edge.getU() != edge.getV()) {
                    finalEdges.add(edge);
                }
            }
            edges = finalEdges;
        }

        return edges;
    }

    private void addEdge(List<Edge> edges, Set<String> existingEdges, int u, int v, int w) {
        String edgeStr = u + "->" + v;
        if (!existingEdges.contains(edgeStr)) {
            edges.add(new Edge(u, v, w));
            existingEdges.add(edgeStr);
        }
    }

    private void writeJson(GraphJson graph, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String filePath = DATA_DIR + filename;
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(graph, writer);
            System.out.printf("Generated: %s (N=%d, E=%d)\n", filename, graph.n, graph.edges.size());
        } catch (IOException e) {
            System.err.println("Error writing JSON file " + filePath + ": " + e.getMessage());
        }
    }

    private void writeSummaryFile() {
        String filePath = DATA_DIR + "DATASET_SUMMARY.txt";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(summaryBuffer.toString());
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new GraphGenerator().generateAllDatasets();
    }
}