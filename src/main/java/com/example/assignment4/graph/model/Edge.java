package com.example.assignment4.graph.model;

public class Edge {
    private final int u;
    private final int v;
    private final int w;

    public Edge(int u, int v, int w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public int getU() { return u; }
    public int getV() { return v; }
    public int getWeight() { return w; }

    @Override
    public String toString() {
        return "(" + u + "->" + v + ", w=" + w + ")";
    }
}