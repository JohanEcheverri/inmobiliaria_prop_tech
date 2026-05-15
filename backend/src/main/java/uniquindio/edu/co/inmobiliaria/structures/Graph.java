package uniquindio.edu.co.inmobiliaria.structures;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Weighted directed/undirected graph using an adjacency list.
 * <p>
 * Internally uses {@link HashTable} and {@link DynamicArrayList} from this
 * same package — no {@code java.util} collections are needed for the core
 * representation.
 * <p>
 * Useful in the real estate project for:
 * <ul>
 *   <li>Modelling geographic adjacency between {@code Barrio} or {@code Zona}.</li>
 *   <li>Mapping distances/routes between properties for closest-match searches.</li>
 *   <li>Advisor–client relationship networks.</li>
 *   <li>Dependency graphs of operations (e.g. a Renovación depends on an Arriendo).</li>
 * </ul>
 *
 * @param <T> vertex type (must implement {@code equals} and {@code hashCode})
 */
public class Graph<T> {

    /**
     * Represents a directed edge to a neighbour with a weight.
     */
    public static final class Edge<T> {
        private final T target;
        private final double weight;

        public Edge(T target, double weight) {
            this.target = target;
            this.weight = weight;
        }

        public T getTarget() {
            return target;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Edge<?> edge)) return false;
            return Double.compare(edge.weight, weight) == 0
                    && Objects.equals(target, edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, weight);
        }
    }

    private final HashTable<T, DynamicArrayList<Edge<T>>> adjacency;
    private final boolean directed;
    private int vertexCount;
    private int edgeCount;

    /**
     * Creates a new graph.
     *
     * @param directed {@code true} for a directed graph, {@code false} for undirected
     */
    public Graph(boolean directed) {
        this.adjacency = new HashTable<>();
        this.directed = directed;
        this.vertexCount = 0;
        this.edgeCount = 0;
    }

    // ──────────────────────────── capacity ────────────────────────────

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edgeCount;
    }

    public boolean isDirected() {
        return directed;
    }

    // ──────────────────────────── vertices ────────────────────────────

    /**
     * Adds a vertex.  Has no effect if the vertex already exists.
     */
    public void addVertex(T vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("vertex must not be null");
        }
        if (!adjacency.containsKey(vertex)) {
            adjacency.put(vertex, new DynamicArrayList<>());
            vertexCount++;
        }
    }

    /**
     * Removes a vertex and all its incident edges.
     *
     * @throws NoSuchElementException if the vertex does not exist
     */
    public void removeVertex(T vertex) {
        if (!adjacency.containsKey(vertex)) {
            throw new NoSuchElementException("Vertex not found: " + vertex);
        }
        // Remove edges pointing *to* this vertex from all other adjacency lists
        DynamicArrayList<T> allVertices = adjacency.keys();
        for (int i = 0; i < allVertices.size(); i++) {
            T v = allVertices.get(i);
            if (!v.equals(vertex)) {
                DynamicArrayList<Edge<T>> edges = adjacency.get(v);
                for (int j = edges.size() - 1; j >= 0; j--) {
                    if (edges.get(j).target.equals(vertex)) {
                        edges.removeAt(j);
                        edgeCount--;
                    }
                }
            }
        }
        // Remove outgoing edges count
        edgeCount -= adjacency.get(vertex).size();
        adjacency.remove(vertex);
        vertexCount--;
    }

    public boolean containsVertex(T vertex) {
        return adjacency.containsKey(vertex);
    }

    /**
     * Returns all vertices in the graph.
     */
    public DynamicArrayList<T> getVertices() {
        return adjacency.keys();
    }

    // ──────────────────────────── edges ───────────────────────────────

    /**
     * Adds a weighted edge.  For undirected graphs the reverse edge is
     * also added automatically.
     */
    public void addEdge(T from, T to, double weight) {
        addVertex(from);
        addVertex(to);
        adjacency.get(from).add(new Edge<>(to, weight));
        edgeCount++;
        if (!directed) {
            adjacency.get(to).add(new Edge<>(from, weight));
            edgeCount++;
        }
    }

    /**
     * Adds an unweighted edge (weight = 1.0).
     */
    public void addEdge(T from, T to) {
        addEdge(from, to, 1.0);
    }

    /**
     * Removes a single edge from {@code from} to {@code to}.
     *
     * @return {@code true} if the edge was found and removed
     */
    public boolean removeEdge(T from, T to) {
        if (!adjacency.containsKey(from)) return false;
        boolean removed = removeFirstEdge(adjacency.get(from), to);
        if (removed) {
            edgeCount--;
            if (!directed && adjacency.containsKey(to)) {
                if (removeFirstEdge(adjacency.get(to), from)) {
                    edgeCount--;
                }
            }
        }
        return removed;
    }

    /**
     * Returns the neighbours (adjacent edges) of a vertex.
     *
     * @throws NoSuchElementException if the vertex does not exist
     */
    public DynamicArrayList<Edge<T>> getNeighbors(T vertex) {
        if (!adjacency.containsKey(vertex)) {
            throw new NoSuchElementException("Vertex not found: " + vertex);
        }
        return adjacency.get(vertex);
    }

    // ──────────────────────────── traversals ──────────────────────────

    /**
     * Breadth-first search starting from {@code start}.
     *
     * @return vertices in BFS order
     */
    public DynamicArrayList<T> bfs(T start) {
        if (!adjacency.containsKey(start)) {
            throw new NoSuchElementException("Vertex not found: " + start);
        }
        DynamicArrayList<T> result = new DynamicArrayList<>();
        HashTable<T, Boolean> visited = new HashTable<>();
        Queue<T> queue = new Queue<>();

        visited.put(start, true);
        queue.enqueue(start);

        while (!queue.isEmpty()) {
            T current = queue.dequeue();
            result.add(current);
            DynamicArrayList<Edge<T>> edges = adjacency.get(current);
            for (int i = 0; i < edges.size(); i++) {
                T neighbor = edges.get(i).target;
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, true);
                    queue.enqueue(neighbor);
                }
            }
        }
        return result;
    }

    /**
     * Depth-first search starting from {@code start}.
     *
     * @return vertices in DFS order
     */
    public DynamicArrayList<T> dfs(T start) {
        if (!adjacency.containsKey(start)) {
            throw new NoSuchElementException("Vertex not found: " + start);
        }
        DynamicArrayList<T> result = new DynamicArrayList<>();
        HashTable<T, Boolean> visited = new HashTable<>();
        Stack<T> stack = new Stack<>();

        stack.push(start);

        while (!stack.isEmpty()) {
            T current = stack.pop();
            if (visited.containsKey(current)) {
                continue;
            }
            visited.put(current, true);
            result.add(current);
            DynamicArrayList<Edge<T>> edges = adjacency.get(current);
            for (int i = edges.size() - 1; i >= 0; i--) {
                T neighbor = edges.get(i).target;
                if (!visited.containsKey(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
        return result;
    }

    /**
     * Dijkstra's shortest-path from {@code start} to {@code end}.
     *
     * @return ordered list of vertices on the shortest path, or an empty list
     *         if no path exists
     */
    public DynamicArrayList<T> shortestPath(T start, T end) {
        if (!adjacency.containsKey(start) || !adjacency.containsKey(end)) {
            return new DynamicArrayList<>();
        }

        HashTable<T, Double> dist = new HashTable<>();
        HashTable<T, T> prev = new HashTable<>();
        HashTable<T, Boolean> visited = new HashTable<>();

        // Initialise distances
        DynamicArrayList<T> allVertices = adjacency.keys();
        for (int i = 0; i < allVertices.size(); i++) {
            dist.put(allVertices.get(i), Double.MAX_VALUE);
        }
        dist.put(start, 0.0);

        // Simple priority queue over (distance, vertex) pairs
        PriorityQueue<T> pq = new PriorityQueue<>(
                Comparator.comparingDouble(v -> dist.getOrDefault(v, Double.MAX_VALUE))
        );
        pq.enqueue(start);

        while (!pq.isEmpty()) {
            T current = pq.dequeue();
            if (visited.containsKey(current)) continue;
            visited.put(current, true);
            if (current.equals(end)) break;

            DynamicArrayList<Edge<T>> edges = adjacency.get(current);
            for (int i = 0; i < edges.size(); i++) {
                Edge<T> e = edges.get(i);
                if (visited.containsKey(e.target)) continue;
                double alt = dist.get(current) + e.weight;
                if (alt < dist.get(e.target)) {
                    dist.put(e.target, alt);
                    prev.put(e.target, current);
                    pq.enqueue(e.target);
                }
            }
        }

        // Reconstruct path
        DynamicArrayList<T> path = new DynamicArrayList<>();
        if (!prev.containsKey(end) && !start.equals(end)) {
            return path; // no path
        }
        Stack<T> stack = new Stack<>();
        T cur = end;
        while (cur != null) {
            stack.push(cur);
            cur = prev.containsKey(cur) ? prev.get(cur) : null;
        }
        while (!stack.isEmpty()) {
            path.add(stack.pop());
        }
        return path;
    }

    // ──────────────────────────── helpers ─────────────────────────────

    private boolean removeFirstEdge(DynamicArrayList<Edge<T>> edges, T target) {
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).target.equals(target)) {
                edges.removeAt(i);
                return true;
            }
        }
        return false;
    }
}
