import java.util.*;

public class AlgorithmsTests {
    static int failures = 0;

    static List<Object> row(long... v) {
        List<Object> r = new ArrayList<>();
        for (long x : v) r.add(x);
        return r;
    }

    static Map<String, Object> req(String task, int n, List<Object> edgesOrOps, Integer source, boolean ops) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("task", task);
        m.put("n", (long) n);
        if (source != null) m.put("source", (long) source);
        m.put(ops ? "operations" : "edges", edgesOrOps);
        return m;
    }

    static List<Object> rows(long[]... rs) {
        List<Object> l = new ArrayList<>();
        for (long[] r : rs) l.add(row(r));
        return l;
    }

    static void check(String name, boolean ok) {
        if (!ok) failures++;
        System.out.println((ok ? "PASS " : "FAIL ") + name);
    }

    @SuppressWarnings("unchecked")
    static <T> T get(Map<String, Object> m, String k) {
        return (T) m.get(k);
    }

    static boolean validOrder(int n, int[][] edges, int[] order) {
        if (order.length != n) return false;
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            if (order[i] < 0 || order[i] >= n || pos[order[i]] != -1) return false;
            pos[order[i]] = i;
        }
        for (int[] e : edges) if (pos[e[0]] >= pos[e[1]]) return false;
        return true;
    }

    static boolean validWitness(int[][] edges, int source, List<Long> dist, int[] parent) {
        for (int v = 0; v < parent.length; v++) {
            if (v == source || dist.get(v) == null) {
                if (parent[v] != -1) return false;
                continue;
            }
            int[] e = edges[parent[v]];
            if (e[1] != v || dist.get(e[0]) == null || dist.get(e[0]) + e[2] != dist.get(v)) return false;
        }
        return true;
    }

    static void dsuTests() {

        Map<String, Object> r = Algorithms.dsu(req("dsu", 4,
                rows(new long[]{0, 1}, new long[]{2, 3}, new long[]{1, 0}, new long[]{0, 3}, new long[]{2, 2}), null, true));
        List<int[]> s = get(r, "states");
        check("dsu: repeats, self union, final merge",
                Arrays.equals(s.get(0), new int[]{3, 2}) && Arrays.equals(s.get(1), new int[]{2, 2})
                        && Arrays.equals(s.get(2), new int[]{2, 2}) && Arrays.equals(s.get(3), new int[]{1, 4})
                        && Arrays.equals(s.get(4), new int[]{1, 4}));

        r = Algorithms.dsu(req("dsu", 0, new ArrayList<>(), null, true));
        check("dsu: n=0 gives empty states", ((List<?>) r.get("states")).isEmpty());

        int n = 100000;
        List<Object> ops = new ArrayList<>();
        for (int i = 0; i + 1 < n; i++) ops.add(row(i, i + 1));
        r = Algorithms.dsu(req("dsu", n, ops, null, true));
        List<int[]> st = get(r, "states");
        check("dsu: chain n=100000 ends with [1,100000]", Arrays.equals(st.get(st.size() - 1), new int[]{1, n}));
    }

    static void mstTests() {
        for (String task : new String[]{"kruskal", "prim"}) {

            Map<String, Object> q = req(task, 4, rows(new long[]{0, 1, -2}, new long[]{0, 1, -2},
                    new long[]{1, 2, 3}, new long[]{0, 2, 3}, new long[]{3, 3, -99}), null, false);
            Map<String, Object> r = task.equals("kruskal") ? Algorithms.kruskal(q) : Algorithms.prim(q);
            List<Integer> ids = get(r, "edge_ids");
            boolean one = ids.size() == 2 && (ids.contains(0) ^ ids.contains(1)) && (ids.contains(2) ^ ids.contains(3));
            check(task + ": negative loop + parallel + tie + isolate",
                    (long) get(r, "cost") == 1L && (int) get(r, "components") == 2 && one);

            q = req(task, 4, rows(new long[]{0, 1, 1_000_000_000}, new long[]{1, 2, 1_000_000_000},
                    new long[]{2, 3, 1_000_000_000}), null, false);
            r = task.equals("kruskal") ? Algorithms.kruskal(q) : Algorithms.prim(q);
            check(task + ": 64-bit total 3e9", (long) get(r, "cost") == 3_000_000_000L);

            q = req(task, 7, rows(new long[]{0, 1, 1}, new long[]{1, 2, 2}, new long[]{0, 2, 3},
                    new long[]{3, 4, 4}, new long[]{4, 5, 5}, new long[]{3, 5, 6}), null, false);
            r = task.equals("kruskal") ? Algorithms.kruskal(q) : Algorithms.prim(q);
            check(task + ": two triangles + isolate (1+2+4+5=12, 3 components)",
                    (long) get(r, "cost") == 12L && (int) get(r, "components") == 3);

            q = req(task, 0, new ArrayList<>(), null, false);
            r = task.equals("kruskal") ? Algorithms.kruskal(q) : Algorithms.prim(q);
            check(task + ": n=0", (long) get(r, "cost") == 0 && (int) get(r, "components") == 0);
        }

        Random rnd = new Random(7);
        boolean same = true;
        for (int t = 0; t < 200 && same; t++) {
            int n = 1 + rnd.nextInt(30), m = rnd.nextInt(80);
            List<Object> es = new ArrayList<>();
            for (int i = 0; i < m; i++) es.add(row(rnd.nextInt(n), rnd.nextInt(n), rnd.nextInt(41) - 20));
            Map<String, Object> a = Algorithms.kruskal(req("kruskal", n, es, null, false));
            Map<String, Object> b = Algorithms.prim(req("prim", n, es, null, false));
            same = a.get("cost").equals(b.get("cost")) && a.get("components").equals(b.get("components"));
        }
        check("kruskal == prim on 200 random graphs (cost, components)", same);
    }

    static void sccTests() {

        Map<String, Object> r = Algorithms.scc(req("scc", 3, rows(new long[]{0, 1}, new long[]{1, 0},
                new long[]{1, 2}, new long[]{1, 2}, new long[]{2, 2}), null, false));
        int[] c = get(r, "component");
        List<int[]> cond = get(r, "condensation");
        check("scc: labels, duplicate cross edge counted once, self loop ignored",
                c[0] == c[1] && c[1] != c[2] && cond.size() == 1 && cond.get(0)[0] == c[1] && cond.get(0)[1] == c[2]);

        int n = 100000;
        List<Object> es = new ArrayList<>();
        for (int i = 0; i + 1 < n; i++) es.add(row(i, i + 1));
        r = Algorithms.scc(req("scc", n, es, null, false));
        c = get(r, "component");
        cond = get(r, "condensation");
        check("scc: deep chain n=100000 (no stack overflow)", new HashSet<>(Arrays.asList(Arrays.stream(c).boxed().toArray())).size() == n && cond.size() == n - 1);

        es.add(row(n - 1, 0));
        r = Algorithms.scc(req("scc", n, es, null, false));
        c = get(r, "component");
        check("scc: one cycle of 100000 vertices", Arrays.stream(c).distinct().count() == 1 && ((List<?>) r.get("condensation")).isEmpty());
    }

    static void topoDagTests() {
        int[][] e = {{0, 2}, {1, 2}};
        Map<String, Object> r = Algorithms.topo(req("topo", 3, rows(new long[]{0, 2}, new long[]{1, 2}), null, false));
        check("topo: valid order accepted by checker (several valid answers exist)",
                (boolean) get(r, "acyclic") && validOrder(3, e, get(r, "order"))
                        && validOrder(3, e, new int[]{1, 0, 2}));
        check("topo checker rejects invalid order", !validOrder(3, e, new int[]{2, 0, 1}));
        r = Algorithms.topo(req("topo", 4, rows(new long[]{2, 3}, new long[]{3, 2}), null, false));
        check("topo: cycle in a part disconnected from vertex 0 => false + empty order",
                !(boolean) get(r, "acyclic") && ((int[]) get(r, "order")).length == 0);
        r = Algorithms.topo(req("topo", 1, rows(new long[]{0, 0}), null, false));
        check("topo: self loop is a cycle", !(boolean) get(r, "acyclic"));
        int n = 100000;
        List<Object> es = new ArrayList<>();
        for (int i = n - 2; i >= 0; i--) es.add(row(i, i + 1));
        r = Algorithms.topo(req("topo", n, es, null, false));
        int[] order = get(r, "order");
        check("topo: deep chain n=100000", (boolean) get(r, "acyclic") && order[0] == 0 && order[n - 1] == n - 1);

        long[][] raw = {{0, 1, 2}, {0, 1, 2}, {0, 2, 1}, {1, 3, -3}, {2, 3, -2}};
        int[][] edges = new int[raw.length][];
        for (int i = 0; i < raw.length; i++) edges[i] = new int[]{(int) raw[i][0], (int) raw[i][1], (int) raw[i][2]};
        r = Algorithms.dag(req("dag", 5, rows(raw), 0, false));
        List<Long> dist = get(r, "distance");
        int[] parent = get(r, "parent_edge");
        check("dag: distances [0,2,1,-1,null]", dist.equals(Arrays.asList(0L, 2L, 1L, -1L, null)));
        check("dag: parent edges form a valid witness", validWitness(edges, 0, dist, parent));
        check("dag witness checker rejects wrong parent (edge not entering vertex)",
                !validWitness(edges, 0, dist, new int[]{-1, 0, 2, 0, -1}));
        check("dag witness checker accepts alternative tie/parallel witness",
                validWitness(edges, 0, dist, new int[]{-1, 1, 2, 4, -1}));

        List<Integer> route3 = Algorithms.route(parent, edges, 0, 3);
        check("route: reachable target 3 is a valid 3-vertex route 0 -> (1|2) -> 3",
                route3.size() == 3 && route3.get(0) == 0 && route3.get(2) == 3
                        && (route3.get(1) == 1 || route3.get(1) == 2));
        check("route: target == source gives [0]", Algorithms.route(parent, edges, 0, 0).equals(List.of(0)));
        check("route: unreachable target 4 gives []", Algorithms.route(parent, edges, 0, 4).isEmpty());
        List<Integer> viaEdges = Algorithms.routeEdges(parent, edges, 0, 1);
        check("route: parallel edges 0/1 both cost 2, helper returns exactly one of them",
                viaEdges.size() == 1 && (viaEdges.get(0) == 0 || viaEdges.get(0) == 1));

        long sum = 0;
        for (int id : Algorithms.routeEdges(parent, edges, 0, 3)) sum += edges[id][2];
        check("route: sum of route edge weights == distance[3]", sum == dist.get(3));

        r = Algorithms.dag(req("dag", 4, rows(new long[]{0, 1, 5}, new long[]{2, 3, 1}, new long[]{3, 2, 1}), 0, false));
        check("dag: cycle unreachable from source => false, two empty arrays",
                !(boolean) get(r, "acyclic") && ((int[]) get(r, "distance")).length == 0
                        && ((int[]) get(r, "parent_edge")).length == 0);

        r = Algorithms.dag(req("dag", 4, rows(new long[]{0, 1, 1_000_000_000}, new long[]{1, 2, 1_000_000_000},
                new long[]{2, 3, 1_000_000_000}), 0, false));
        check("dag: 64-bit distance 3e9", ((List<Long>) get(r, "distance")).get(3) == 3_000_000_000L);

        es = new ArrayList<>();
        for (int i = 0; i + 1 < n; i++) es.add(row(i, i + 1, -1));
        r = Algorithms.dag(req("dag", n, es, 0, false));
        dist = get(r, "distance");
        check("dag: deep chain n=100000, dist[last] = -99999", dist.get(n - 1) == -(n - 1));

        r = Algorithms.dag(req("dag", 1, new ArrayList<>(), 0, false));
        check("dag: n=1 singleton", ((List<Long>) get(r, "distance")).equals(List.of(0L)));
    }

    static void stateResetTest() {
        Map<String, Object> cyc = req("topo", 2, rows(new long[]{0, 1}, new long[]{1, 0}), null, false);
        Map<String, Object> ok = req("topo", 2, rows(new long[]{0, 1}), null, false);
        boolean good = true;
        for (int i = 0; i < 3; i++) {
            good &= !(boolean) get(Algorithms.topo(cyc), "acyclic");
            good &= (boolean) get(Algorithms.topo(ok), "acyclic");
            good &= ((List<?>) get(Algorithms.scc(req("scc", 2, rows(new long[]{0, 1}), null, false)), "condensation")).size() == 1;
        }
        check("state reset: alternating cyclic/acyclic/scc requests give identical answers", good);
    }

    public static void main(String[] args) {
        dsuTests();
        mstTests();
        sccTests();
        topoDagTests();
        stateResetTest();
        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " TEST(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
