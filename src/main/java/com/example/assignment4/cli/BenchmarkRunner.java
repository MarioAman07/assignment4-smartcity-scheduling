package com.example.assignment4.cli;

import com.example.assignment4.graph.dagsp.LongestPathDAG;
import com.example.assignment4.graph.model.ComponentGraph;
import com.example.assignment4.graph.model.DirectedGraph;
import com.example.assignment4.graph.scc.CondensationGraphBuilder;
import com.example.assignment4.graph.scc.TarjanSCC;
import com.example.assignment4.graph.topo.TopologicalSort;
import com.example.assignment4.metrics.CsvExporter;
import com.example.assignment4.metrics.PerformanceTracker;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BenchmarkRunner {

    private static final String DATA_DIR = "data";

    private static final String[] HEADER_ORDER = {
            "FILENAME", "N_NODES", "E_EDGES", "SCC_TIME_MS", "SCC_DFS_VISITS", "SCC_DFS_EDGES",
            "TOPO_TIME_MS", "TOPO_DFS_VISITS", "DAGSP_TIME_MS", "DAGSP_RELAXATIONS",
            "CRITICAL_PATH_LENGTH"
    };

    public static void main(String[] args) {
        System.out.println("--- Starting Assignment 4 Benchmark Runner ---");

        File dataFolder = new File(DATA_DIR);
        File[] jsonFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.err.println("Error: No JSON files found in the 'data' directory.");
            return;
        }

        List<File> sortedFiles = Arrays.asList(jsonFiles);
        sortedFiles.sort(Comparator.comparing(File::getName));

        List<Map<String, Object>> allResults = new ArrayList<>();

        for (File file : sortedFiles) {
            System.out.println("\nProcessing file: " + file.getName());

            try {
                DirectedGraph graph = DirectedGraph.loadFromJson(file.getAbsolutePath());
                Map<String, Object> results = runFullPipeline(file.getName(), graph);
                allResults.add(results);

            } catch (IOException e) {
                System.err.println("Failed to read or parse " + file.getName() + ": " + e.getMessage());
            }
        }

        CsvExporter.writeResults(allResults, HEADER_ORDER);
        System.out.println("--- Benchmark Run Complete ---");
    }

    private static Map<String, Object> runFullPipeline(String filename, DirectedGraph graph) {
        int N = graph.getN();
        int E = graph.getE();

        // SCC (Tarjan)
        PerformanceTracker sccTracker = new PerformanceTracker();
        TarjanSCC sccFinder = new TarjanSCC(graph, sccTracker);
        sccFinder.findSCCs();
        Map<String, Object> sccMetrics = sccTracker.getResults("SCC", N, E);

        //Condensation Graph
        ComponentGraph cGraph = CondensationGraphBuilder.build(graph, sccFinder.getSccs());
        int C = cGraph.getNumComponents();

        //Topological Sort
        PerformanceTracker topoTracker = new PerformanceTracker();
        TopologicalSort sorter = new TopologicalSort(cGraph, topoTracker);
        List<Integer> topoOrder = sorter.computeComponentOrder();
        Map<String, Object> topoMetrics = topoTracker.getResults("TOPO", C, cGraph.getComponentEdges().size());

        //Longest Path (Critical Path)
        PerformanceTracker lpTracker = new PerformanceTracker();
        LongestPathDAG lpFinder = new LongestPathDAG(cGraph, topoOrder, lpTracker);
        lpFinder.computeLongestPaths();
        Map<String, Object> lpMetrics = lpTracker.getResults("DAGSP", C, cGraph.getComponentEdges().size());


        Map<String, Object> finalResults = new HashMap<>();

        finalResults.put("FILENAME", filename);
        finalResults.put("N_NODES", N);
        finalResults.put("E_EDGES", E);

        finalResults.put("SCC_TIME_MS", sccMetrics.get("TIME_MS"));
        finalResults.put("SCC_DFS_VISITS", sccMetrics.getOrDefault("SCC_DFS_VISITS", 0L)); // Исправление null
        finalResults.put("SCC_DFS_EDGES", sccMetrics.getOrDefault("SCC_DFS_EDGES", 0L)); // Исправление null

        finalResults.put("TOPO_TIME_MS", topoMetrics.get("TIME_MS"));
        finalResults.put("TOPO_DFS_VISITS", topoMetrics.getOrDefault("TOPO_DFS_VISITS", 0L)); // Исправление null

        finalResults.put("DAGSP_TIME_MS", lpMetrics.get("TIME_MS"));
        finalResults.put("DAGSP_RELAXATIONS", lpMetrics.getOrDefault("DAGSP_RELAXATIONS", 0L)); // <-- ГЛАВНОЕ ИСПРАВЛЕНИЕ (null -> 0L)

        finalResults.put("CRITICAL_PATH_LENGTH", lpFinder.getCriticalPathLength());

        return finalResults;
    }
}