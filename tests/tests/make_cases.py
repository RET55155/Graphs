import json, random

def w(path, cases, expected=True):
    with open(path, 'w', encoding='utf-8') as f:
        for c in cases:
            f.write(json.dumps(c['input'], separators=(',', ':')) + '\n')
    if expected:
        json.dump(cases, open(path.replace('.jsonl', '.expected.json'), 'w', encoding='utf-8'),
                  ensure_ascii=False, indent=1)

small = [
 {'name': 'ping', 'why': 'protocol', 'input': {'task': 'ping'}, 'output': {'status': 'ready'}},
 {'name': 'dag cycle unreachable from source',
  'why': 'cycle 2<->3 is not reachable from source 0, but the WHOLE graph must be checked',
  'input': {'task': 'dag', 'n': 4, 'source': 0, 'edges': [[0, 1, 5], [2, 3, 1], [3, 2, 1]]},
  'output': {'acyclic': False, 'distance': [], 'parent_edge': []}},
 {'name': 'dag tie + parallel + unreachable',
  'why': 'dist[3] = -1 via 1 or via 2 (tie); edges 0 and 1 are parallel; vertex 4 unreachable',
  'input': {'task': 'dag', 'n': 5, 'source': 0,
            'edges': [[0, 1, 2], [0, 1, 2], [0, 2, 1], [1, 3, -3], [2, 3, -2]]},
  'output': {'acyclic': True, 'distance': [0, 2, 1, -1, None], 'parent_edge': [-1, 0, 2, 3, -1]}},
 {'name': 'kruskal negative loop/parallel/isolate',
  'why': 'loop -99 ignored, one of the two -2 edges, one 3-edge, vertex 3 isolated: cost 1, 2 components',
  'input': {'task': 'kruskal', 'n': 4, 'edges': [[0, 1, -2], [0, 1, -2], [1, 2, 3], [0, 2, 3], [3, 3, -99]]},
  'output': {'cost': 1, 'components': 2, 'edge_ids': [0, 2]}},
 {'name': 'scc two cycles + bridge',
  'why': '{0,1} and {2,3} are cycles, one bridge 1->2 (duplicate counted once)',
  'input': {'task': 'scc', 'n': 4, 'edges': [[0, 1], [1, 0], [2, 3], [3, 2], [1, 2], [1, 2]]},
  'output': {'component': [0, 0, 1, 1], 'condensation': [[0, 1]]}},
]
w('tests/my_cases.jsonl', small)

rnd = random.Random(12345)
n, m = 100000, 250000
edges = [[rnd.randrange(n), rnd.randrange(n), rnd.randint(-10**9, 10**9)] for _ in range(m)]
dag_edges = []
for _ in range(m):
    a, b = rnd.randrange(n), rnd.randrange(n)
    if a != b:
        dag_edges.append([min(a, b), max(a, b), rnd.randint(-10**9, 10**9)])
plain = [[a, b] for a, b, _ in edges]
limit = [
 {'name': 'limit dsu', 'input': {'task': 'dsu', 'n': n, 'operations': [[a, b] for a, b, _ in edges]}},
 {'name': 'limit kruskal', 'input': {'task': 'kruskal', 'n': n, 'edges': edges}},
 {'name': 'limit prim', 'input': {'task': 'prim', 'n': n, 'edges': edges}},
 {'name': 'limit scc', 'input': {'task': 'scc', 'n': n, 'edges': plain}},
 {'name': 'limit topo (cyclic)', 'input': {'task': 'topo', 'n': n, 'edges': plain}},
 {'name': 'limit dag (acyclic)', 'input': {'task': 'dag', 'n': n, 'source': 0, 'edges': dag_edges}},
]
for c in limit:
    c['output'] = None
w('tests/limit_cases.jsonl', limit, expected=False)
print('ok')
