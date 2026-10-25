import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class Algorithms {
    private Algorithms() {
    }

    private static int find(int[] parent, int x) {
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    private static final class Graph {
        final int[] start;
        final int[] target;
        final int[] edgeId;

        Graph(int n, int[][] edges, boolean directed, boolean reverse) {
            start = new int[n + 1];
            for (int[] e : edges) {
                int a = reverse ? e[1] : e[0];
                int b = reverse ? e[0] : e[1];
                if (directed) {
                    start[a + 1]++;
                } else if (a != b) {
                    start[a + 1]++;
                    start[b + 1]++;
                }
            }
            for (int i = 0; i < n; i++) {
                start[i + 1] += start[i];
            }
            target = new int[start[n]];
            edgeId = new int[start[n]];
            int[] fill = Arrays.copyOf(start, n);
            for (int id = 0; id < edges.length; id++) {
                int a = reverse ? edges[id][1] : edges[id][0];
                int b = reverse ? edges[id][0] : edges[id][1];
                if (directed) {
                    target[fill[a]] = b;
                    edgeId[fill[a]++] = id;
                } else if (a != b) {
                    target[fill[a]] = b;
                    edgeId[fill[a]++] = id;
                    target[fill[b]] = a;
                    edgeId[fill[b]++] = id;
                }
            }
        }
    }

    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        int components = n;
        int largest = n > 0 ? 1 : 0;

        List<int[]> states = new ArrayList<>(operations.length);
        for (int[] op : operations) {
            int a = find(parent, op[0]);
            int b = find(parent, op[1]);
            if (a != b) {
                if (size[a] < size[b]) {
                    int t = a;
                    a = b;
                    b = t;
                }
                parent[b] = a;
                size[a] += size[b];
                components--;
                largest = Math.max(largest, size[a]);
            }
            states.add(new int[] {components, largest});
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("states", states);
        return result;
    }

    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);
        int m = edges.length;

        long[] keys = new long[m];
        for (int i = 0; i < m; i++) {
            keys[i] = (((long) edges[i][2] + 1_000_000_000L) << 20) | i;
        }
        Arrays.sort(keys);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        long cost = 0;
        int components = n;
        List<Integer> ids = new ArrayList<>();
        for (long key : keys) {
            int id = (int) (key & ((1 << 20) - 1));
            int a = find(parent, edges[id][0]);
            int b = find(parent, edges[id][1]);
            if (a == b) {
                continue;
            }
            if (size[a] < size[b]) {
                int t = a;
                a = b;
                b = t;
            }
            parent[b] = a;
            size[a] += size[b];
            cost += edges[id][2];
            ids.add(id);
            components--;
        }
        return forest(cost, ids, components);
    }

    private static Map<String, Object> forest(long cost, List<Integer> ids, int components) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", ids);
        result.put("components", components);
        return result;
    }

    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Graph g = new Graph(n, edges, false, false);
        boolean[] visited = new boolean[n];

        PriorityQueue<Long> heap = new PriorityQueue<>();
        long cost = 0;
        int components = 0;
        List<Integer> ids = new ArrayList<>();

        for (int s = 0; s < n; s++) {
            if (visited[s]) {
                continue;
            }
            components++;
            visited[s] = true;
            pushEdges(g, edges, s, heap);
            while (!heap.isEmpty()) {
                long key = heap.poll();
                int id = (int) (key & ((1 << 20) - 1));
                int u = edges[id][0];
                int v = edges[id][1];
                if (visited[u] && visited[v]) {
                    continue;
                }
                int next = visited[u] ? v : u;
                visited[next] = true;
                cost += edges[id][2];
                ids.add(id);
                pushEdges(g, edges, next, heap);
            }
        }
        return forest(cost, ids, components);
    }

    private static void pushEdges(Graph g, int[][] edges, int v, PriorityQueue<Long> heap) {
        for (int i = g.start[v]; i < g.start[v + 1]; i++) {
            int id = g.edgeId[i];
            heap.add((((long) edges[id][2] + 1_000_000_000L) << 20) | id);
        }
    }

    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Graph forward = new Graph(n, edges, true, false);
        Graph transpose = new Graph(n, edges, true, true);

        int[] finish = new int[n];
        int finished = 0;
        boolean[] seen = new boolean[n];
        int[] pos = Arrays.copyOf(forward.start, n);
        int[] stack = new int[n];
        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }
            int sp = 0;
            stack[sp++] = s;
            seen[s] = true;
            while (sp > 0) {
                int v = stack[sp - 1];
                if (pos[v] < forward.start[v + 1]) {
                    int w = forward.target[pos[v]++];
                    if (!seen[w]) {
                        seen[w] = true;
                        stack[sp++] = w;
                    }
                } else {
                    sp--;
                    finish[finished++] = v;
                }
            }
        }

        int[] component = new int[n];
        Arrays.fill(component, -1);
        int labels = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = finish[i];
            if (component[s] != -1) {
                continue;
            }
            int sp = 0;
            stack[sp++] = s;
            component[s] = labels;
            while (sp > 0) {
                int v = stack[--sp];
                for (int j = transpose.start[v]; j < transpose.start[v + 1]; j++) {
                    int w = transpose.target[j];
                    if (component[w] == -1) {
                        component[w] = labels;
                        stack[sp++] = w;
                    }
                }
            }
            labels++;
        }

        HashSet<Long> unique = new HashSet<>();
        List<int[]> condensation = new ArrayList<>();
        for (int[] e : edges) {
            int a = component[e[0]];
            int b = component[e[1]];
            if (a != b && unique.add((long) a * labels + b)) {
                condensation.add(new int[] {a, b});
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", component);
        result.put("condensation", condensation);
        return result;
    }

    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        int[] order = topologicalOrder(n, new Graph(n, edges, true, false));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acyclic", order != null);
        result.put("order", order != null ? order : new int[0]);
        return result;
    }

    private static int[] topologicalOrder(int n, Graph g) {
        byte[] state = new byte[n];
        int[] pos = Arrays.copyOf(g.start, n);
        int[] stack = new int[n];
        int[] order = new int[n];
        int filled = n;
        for (int s = 0; s < n; s++) {
            if (state[s] != 0) {
                continue;
            }
            int sp = 0;
            stack[sp++] = s;
            state[s] = 1;
            while (sp > 0) {
                int v = stack[sp - 1];
                if (pos[v] < g.start[v + 1]) {
                    int w = g.target[pos[v]++];
                    if (state[w] == 1) {
                        return null;
                    }
                    if (state[w] == 0) {
                        state[w] = 1;
                        stack[sp++] = w;
                    }
                } else {
                    state[v] = 2;
                    sp--;
                    order[--filled] = v;
                }
            }
        }
        return order;
    }

    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        Graph g = new Graph(n, edges, true, false);
        int[] order = topologicalOrder(n, g);

        Map<String, Object> result = new LinkedHashMap<>();
        if (order == null) {
            result.put("acyclic", false);
            result.put("distance", new int[0]);
            result.put("parent_edge", new int[0]);
            return result;
        }

        long[] dist = new long[n];
        boolean[] reached = new boolean[n];
        int[] parentEdge = new int[n];
        Arrays.fill(parentEdge, -1);
        reached[source] = true;
        dist[source] = 0;

        for (int v : order) {
            if (!reached[v]) {
                continue;
            }
            for (int i = g.start[v]; i < g.start[v + 1]; i++) {
                int id = g.edgeId[i];
                int w = g.target[i];
                long candidate = dist[v] + edges[id][2];
                if (!reached[w] || candidate < dist[w]) {
                    reached[w] = true;
                    dist[w] = candidate;
                    parentEdge[w] = id;
                }
            }
        }

        List<Long> distance = new ArrayList<>(n);
        for (int v = 0; v < n; v++) {
            distance.add(reached[v] ? dist[v] : null);
        }
        result.put("acyclic", true);
        result.put("distance", distance);
        result.put("parent_edge", parentEdge);
        return result;
    }

    static List<Integer> route(int[] parentEdge, int[][] edges, int source, int target) {
        List<Integer> path = new ArrayList<>();
        int current = target;
        int guard = parentEdge.length;
        while (current != source) {
            int id = parentEdge[current];
            if (id < 0 || guard-- <= 0) {
                return new ArrayList<>();
            }
            path.add(current);
            current = edges[id][0];
        }
        path.add(source);
        Collections.reverse(path);
        return path;
    }

    static List<Integer> routeEdges(int[] parentEdge, int[][] edges, int source, int target) {
        List<Integer> ids = new ArrayList<>();
        int current = target;
        int guard = parentEdge.length;
        while (current != source) {
            int id = parentEdge[current];
            if (id < 0 || guard-- <= 0) {
                return new ArrayList<>();
            }
            ids.add(id);
            current = edges[id][0];
        }
        Collections.reverse(ids);
        return ids;
    }
}
