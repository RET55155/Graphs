import java.util.*;

public class Benchmark {
    static final long SEED = 20260;
    static final int WARMUP = 3, RUNS = 5;

    static Map<String, Object> graph(String task, int n, int m, long seed) {
        Random rnd = new Random(seed);
        List<Object> edges = new ArrayList<>(m);
        for (int i = 1; i < n; i++) {
            edges.add(Arrays.<Object>asList((long) rnd.nextInt(i), (long) i, (long) (rnd.nextInt(2_000_001) - 1_000_000)));
        }
        while (edges.size() < m) {
            edges.add(Arrays.<Object>asList((long) rnd.nextInt(n), (long) rnd.nextInt(n), (long) (rnd.nextInt(2_000_001) - 1_000_000)));
        }
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("task", task);
        q.put("n", (long) n);
        q.put("edges", edges);
        return q;
    }

    static double ms(long ns) { return ns / 1e6; }

    static double median(long[] xs) {
        long[] s = xs.clone();
        Arrays.sort(s);
        return ms(s[s.length / 2]);
    }

    public static void main(String[] args) {
        int[][] sizes = {{1_000, 5_000}, {10_000, 50_000}, {100_000, 250_000}};
        System.out.println("java=" + System.getProperty("java.version") + " os=" + System.getProperty("os.name")
                + " cpus=" + Runtime.getRuntime().availableProcessors() + " seed=" + SEED
                + " warmup=" + WARMUP + " runs=" + RUNS);
        System.out.println("n,m,method,run1_ms,run2_ms,run3_ms,run4_ms,run5_ms,median_ms,cost");
        for (int[] size : sizes) {
            int n = size[0], m = size[1];
            Map<String, Object> kq = graph("kruskal", n, m, SEED + n);
            Map<String, Object> pq = new LinkedHashMap<>(kq);
            pq.put("task", "prim");
            for (String method : new String[]{"kruskal", "prim"}) {
                Map<String, Object> q = method.equals("kruskal") ? kq : pq;
                Object cost = null;
                for (int i = 0; i < WARMUP; i++) cost = run(method, q).get("cost");
                long[] t = new long[RUNS];
                for (int i = 0; i < RUNS; i++) {
                    long a = System.nanoTime();
                    Map<String, Object> r = run(method, q);
                    t[i] = System.nanoTime() - a;
                    cost = r.get("cost");
                }
                StringBuilder sb = new StringBuilder(n + "," + m + "," + method);
                for (long x : t) sb.append(String.format(Locale.ROOT, ",%.2f", ms(x)));
                sb.append(String.format(Locale.ROOT, ",%.2f,%s", median(t), cost));
                System.out.println(sb);
            }
        }
    }

    static Map<String, Object> run(String method, Map<String, Object> q) {
        return method.equals("kruskal") ? Algorithms.kruskal(q) : Algorithms.prim(q);
    }
}
