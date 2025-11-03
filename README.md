# Assignment 4: Smart City / Smart Campus Scheduling

**Student:** Aman Baku  
**Group:** SE-2025

---

## 1. Purpose of Project

This project implements a scheduling pipeline for Smart City / Smart Campus task dependencies. It integrates:

1. **Tarjan's SCC algorithm** – detect and collapse cycles  
2. **Topological Sort** – order tasks  
3. **DAG Shortest/Longest Path (Critical Path)** – compute optimal scheduling time  

Graph data is loaded from JSON, processed, and evaluated for performance.

---

## 2. Project Structure
```markdown
src/
├─ main/java/com/example/assignment4/
│ ├─ cli/
│ │ └─ BenchmarkRunner.java # main runner
│ ├─ graph/
│ │ ├─ dagsp/ # shortest & longest path on DAG
│ │ ├─ model/ # graph models
│ │ ├─ scc/ # Tarjan + condensation graph
│ │ └─ topo/ # Topological sort
│ ├─ metrics/
│ │ ├─ CsvExporter.java
│ │ └─ PerformanceTracker.java
│ └─ util/
│ └─ GraphGenerator.java # generate 9 datasets
│
├─ test/java/com/example/assignment4/
│ ├─ graph/dagsp/ShortLongestPathTest.java
│ └─ graph/scc/SCCTopoTest.java
│
data/
├─ output/performance_metrics.csv # final results
├─ DATASET_SUMMARY.txt
└─ *.json # 9 generated datasets
README.md
pom.xml
```

---

## 3. Run Instructions

### Prerequisites
- Java 11+
- Maven 3.6+

### 1) Build
```bash
mvn clean compile
```
### 2) Run tests
```bash
mvn test
```
### 3) (Optional) Re-generate 9 datasets
```bash
mvn exec:java -Dexec.mainClass="com.example.assignment4.util.GraphGenerator"
```
### 4) Run full benchmark
```bash
mvn exec:java -Dexec.mainClass="com.example.assignment4.cli.BenchmarkRunner"
```
## 4. Report & Analysis

### 4.1 Data Summary & Weight Model

**Data Summary:**  
9 datasets were generated as required (small / medium / large × sparse / dense × cyclic / acyclic).  
A summary of nodes, edges, density is located in:

```markdown
./data/DATASET_SUMMARY.txt
```



**Weight Model:**  
This project uses the **Edge Weights** model (`weight_model: "edge"`).  
This matches the provided `tasks.json` format and simplifies pathfinding logic.

---

### 4.2 Results: Per-Task Performance Tables

The results below were obtained by running `BenchmarkRunner` on all 9 datasets.  
All times are in milliseconds (ms).

| FILENAME | N_NODES | E_EDGES | SCC_TIME_MS | SCC_DFS_VISITS | SCC_DFS_EDGES | TOPO_TIME_MS | TOPO_DFS_VISITS | DAGSP_TIME_MS | DAGSP_RELAXATIONS | CRITICAL_PATH_LENGTH |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| large_cyclic_multi_scc.json | 50 | 625 | 0.323 | 50 | 625 | 0.015 | 1 | 0.008 | 0 | 0.0 |
| large_cyclic_sparse.json | 30 | 47 | 0.044 | 30 | 47 | 0.002 | 1 | 0.003 | 0 | 0.0 |
| large_dag_dense.json | 45 | 506 | 0.154 | 45 | 506 | 0.318 | 45 | 1.446 | 506 | 502.0 |
| medium_cyclic_multi_scc.json | 12 | 36 | 0.021 | 12 | 36 | 0.002 | 1 | 0.002 | 0 | 0.0 |
| medium_dag_dense.json | 20 | 100 | 0.029 | 20 | 100 | 0.028 | 20 | 0.125 | 100 | 208.0 |
| medium_dag_sparse.json | 15 | 24 | 0.016 | 15 | 24 | 0.012 | 15 | 0.031 | 24 | 151.0 |
| small_cyclic_mixed.json | 9 | 15 | 0.015 | 9 | 15 | 0.003 | 1 | 0.002 | 0 | 0.0 |
| small_dag_dense.json | 7 | 12 | 0.008 | 7 | 12 | 0.008 | 7 | 0.015 | 12 | 102.0 |
| small_dag_sparse.json | 8 | 14 | 0.012 | 8 | 14 | 0.009 | 8 | 0.021 | 14 | 93.0 |

> **Table 1:** Performance metrics from `./data/output/performance_metrics.csv`

---

### 4.3 Analysis

#### Bottlenecks: SCC vs DAG-SP

**For DAGs → Bottleneck = DAG-SP**  
Example: `large_dag_dense.json`

- DAGSP time: **1.446 ms**  
- SCC + Topo = **0.472 ms combined**
- Relaxations = **506 = number of edges**
- Confirms **O(V + E)** behavior

**For Cyclic Graphs → Bottleneck = SCC detection**  
Example: `large_cyclic_multi_scc.json`

- SCC time: **0.323 ms (~90% of runtime)**
- Graph collapses to **1 node**
- Topo & DAG-SP trivial (0 edges left)

#### Effect of Density (Edges)

DAG relaxations scale directly with edges:

- 506 edges → 506 relaxations
- 100 edges → 100 relaxations
- 24 edges → 24 relaxations

 Perfect textbook confirmation of **O(V + E)** complexity.

#### Effect of SCCs (Cycles)

Large SCCs → collapse graph → DAG-SP cost eliminated.

- `DAGSP_RELAXATIONS = 0` for all cyclic cases
- Work shifts to Tarjan SCC and remains fast

---

### 4.4 Conclusions & Recommendations

| Algorithm | Theoretical Complexity | Max Observed Time | Key Insight |
|---|---|---:|---|
| Tarjan SCC | O(V + E) | 0.323 ms | Expensive only on dense cyclic graphs; very fast overall |
| DFS Topo Sort | O(V + E) | 0.318 ms | Trivial on collapsed graphs; required step for DAGs |
| DAG-SP (Longest Path) | O(V + E) | 1.446 ms | **True bottleneck** for acyclic workloads; scales 1:1 with edge count |

---

###  Takeaways

- Cyclic → **Tarjan SCC dominates**
- Acyclic → **DAG longest-path dominates**
- Results perfectly match theoretical complexity
- Pipeline is optimal for **task scheduling / dependency resolution**



---
