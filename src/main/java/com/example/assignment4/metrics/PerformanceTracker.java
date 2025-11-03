package com.example.assignment4.metrics;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTracker {

    private long startTimeNano;
    private long totalTimeNano = 0;
    private final Map<String, Long> counters;

    public PerformanceTracker() {
        this.counters = new HashMap<>();
    }

    public void startTimer() {
        this.startTimeNano = System.nanoTime();
    }

    public void stopTimer() {
        if (this.startTimeNano > 0) {
            this.totalTimeNano = System.nanoTime() - this.startTimeNano;
            this.startTimeNano = 0;
        }
    }

    public void incrementCounter(String name) {
        this.counters.put(name, this.counters.getOrDefault(name, 0L) + 1);
    }

    public void addCounterValue(String name, long amount) {
        this.counters.put(name, this.counters.getOrDefault(name, 0L) + amount);
    }

    public Map<String, Object> getResults(String taskName, int N, int E) {
        Map<String, Object> results = new HashMap<>();

        results.put("TASK_NAME", taskName);
        results.put("N_NODES", N);
        results.put("E_EDGES", E);

        results.put("TIME_NANO", this.totalTimeNano);
        results.put("TIME_MS", this.totalTimeNano / 1_000_000.0);

        for (Map.Entry<String, Long> entry : counters.entrySet()) {
            results.put(entry.getKey(), entry.getValue());
        }

        return results;
    }
}