package com.example.assignment4.util; // Изменен пакет на util

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson; // Используем Gson из pom.xml
import com.google.gson.GsonBuilder;

import java.io.File;

/**
 * Generates graph datasets for Assignment 4.
 * Generates 9 files (Small, Medium, Large) with varied density and cycle presence.
 */
public class GraphGenerator {

    private static final String DATA_DIR = "data/";
    private final Random rand = new Random();

    // --- Вспомогательные классы для JSON-структуры ---
    private static class Edge {
        int u;
        int v;
        int w;
        public Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    private static class Graph {
        boolean directed = true;
        int n;
        List<Edge> edges;
        int source;
        String weight_model = "edge";
        public Graph(int n, List<Edge> edges, int source) {
            this.n = n;
            this.edges = edges;
            this.source = source;
        }
    }
    // --------------------------------------------------

    private enum Density { SPARSE, DENSE }
    private enum GraphType { DAG, CYCLIC_SIMPLE, CYCLIC_MULTI_SCC }

    /**
     * Основной метод для генерации всех 9 датасетов.
     */
    public void generateAllDatasets() {
        // Создание папки data, если ее нет
        new File(DATA_DIR).mkdirs();

        System.out.println("--- Generating 9 Datasets ---");

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
        generateGraph(50, "large_cyclic_multi_scc.json", GraphType.CYCLIC_MULTI_SCC, Density.SPARSE, 0);

        System.out.println("--- Generation Complete ---");
    }

    private void generateGraph(int n, String filename, GraphType type, Density density, int source) {
        int maxEdges = n * (n - 1);
        int targetEdges;

        if (density == Density.SPARSE) {
            // Разреженный: E ≈ 1.5 * n (линейно)
            targetEdges = (int) (1.5 * n) + 2;
        } else {
            // Плотный: E ≈ n^2 / 4 (квадратично)
            targetEdges = Math.min(maxEdges, n * n / 4);
        }

        List<Edge> edges = createEdges(n, targetEdges, type);
        Graph graph = new Graph(n, edges, source);
        writeJson(graph, filename);
    }

    private List<Edge> createEdges(int n, int targetEdges, GraphType type) {
        List<Edge> edges = new ArrayList<>();
        Set<String> existingEdges = new HashSet<>();

        // 1. Создаем минимально необходимый набор ребер для заданного типа
        if (type == GraphType.DAG) {
            // Для DAG: гарантируем, что u < v, создавая базовую структуру
            for (int i = 0; i < n - 1; i++) {
                addEdge(edges, existingEdges, i, i + 1, rand.nextInt(10) + 1);
            }
        }

        // 2. Добавляем циклы/множественные SCC, если требуется
        if (type == GraphType.CYCLIC_SIMPLE) {
            // Создаем цикл 4 узлов
            int n_minus_1 = n - 1;
            addEdge(edges, existingEdges, 0, 1, rand.nextInt(10) + 1);
            addEdge(edges, existingEdges, 1, 2, rand.nextInt(10) + 1);
            addEdge(edges, existingEdges, 2, 0, rand.nextInt(10) + 1); // Цикл
        }
        else if (type == GraphType.CYCLIC_MULTI_SCC) {
            // Цикл 1
            addEdge(edges, existingEdges, 0, 1, rand.nextInt(10) + 1);
            addEdge(edges, existingEdges, 1, 0, rand.nextInt(10) + 1);

            // Цикл 2
            int mid = n/2;
            addEdge(edges, existingEdges, mid, mid + 1, rand.nextInt(10) + 1);
            addEdge(edges, existingEdges, mid + 1, mid, rand.nextInt(10) + 1);

            // Связываем их, чтобы граф не был полностью разобщенным (0 -> mid)
            addEdge(edges, existingEdges, 2, mid, rand.nextInt(10) + 1);
        }

        // 3. Заполняем оставшиеся ребра до targetEdges
        while (edges.size() < targetEdges) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);

            if (u == v) continue;

            if (type == GraphType.DAG) {
                // Строгое условие для DAG
                if (u >= v) continue;
            }

            addEdge(edges, existingEdges, u, v, rand.nextInt(10) + 1);
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

    private void writeJson(Graph graph, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String filePath = DATA_DIR + filename;
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(graph, writer);
            System.out.printf("Generated: %s (N=%d, E=%d)\n", filename, graph.n, graph.edges.size());
        } catch (IOException e) {
            System.err.println("Error writing JSON file " + filePath + ": " + e.getMessage());
        }
    }

    // Пример вызова для удобства тестирования
    public static void main(String[] args) {
        new GraphGenerator().generateAllDatasets();
    }
}