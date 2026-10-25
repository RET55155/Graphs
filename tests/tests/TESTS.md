# Own tests: inputs, justified expected answers, commands

Commands (repository root):

```sh
javac --release 17 -encoding UTF-8 -d .build/tests src/Java/*.java tests/*.java
java -cp .build/tests AlgorithmsTests
python tests/make_cases.py
python tools/run.py --input tests/my_cases.jsonl
python tools/run.py --input tests/limit_cases.jsonl
```

`AlgorithmsTests` prints PASS or FAIL for every check and exits with code 1 if any check fails.
`my_cases.expected.json` contains the expected answer and the reason for every case of `my_cases.jsonl`.

| Test | Input | Expected answer and justification |
|---|---|---|
| dsu repeats / self union | n=4, unions (0,1) (2,3) (1,0) (0,3) (2,2) | states [3,2] [2,2] [2,2] [1,4] [1,4]: a repeated or self union changes nothing |
| dsu n=0 | n=0, no operations | no states |
| dsu chain | 99 999 unions of neighbours, n=100 000 | last state [1,100000] |
| kruskal / prim mixed | n=4, edges (0,1,-2) (0,1,-2) (1,2,3) (0,2,3) (3,3,-99) | cost 1, 2 components, 2 edges: loop ignored, one of the parallel -2 edges, one edge of weight 3, vertex 3 isolated |
| kruskal / prim 64 bit | path of 4 vertices, three edges of 10^9 | cost 3 000 000 000 (does not fit into int) |
| kruskal / prim forest | two triangles with weights 1,2,3 and 4,5,6, one isolated vertex | cost 1+2+4+5 = 12, 3 components (Prim must restart) |
| kruskal == prim random | 200 random graphs, weights -20..20 | equal cost and components for both methods |
| scc | 0<->1, 1->2 twice, loop 2->2 | {0,1} and {2}, exactly one condensation pair |
| scc deep chain | 0->1->...->99999 | 100 000 components, 99 999 condensation pairs |
| scc one cycle | the chain plus edge 99999->0 | one component, empty condensation |
| topo | edges 0->2, 1->2 | acyclic; orders [0,1,2] and [1,0,2] are both valid, [2,0,1] is invalid |
| topo disconnected cycle | edges 2->3, 3->2, n=4 | acyclic=false, order [] |
| topo self loop | edge 0->0 | acyclic=false |
| topo deep chain | edges i->i+1 added in reverse, n=100 000 | order starts with 0 and ends with 99999 |
| dag tie / parallel / unreachable | n=5, source 0, edges 0:(0,1,2) 1:(0,1,2) 2:(0,2,1) 3:(1,3,-3) 4:(2,3,-2) | distance [0,2,1,-1,null]; vertex 3 is reached with -1 via 1 or via 2, so parent edge 3 or 4 is valid; wrong parent arrays must be rejected |
| route helper | same graph | target 3 gives a route 0,(1 or 2),3 with weight sum -1; target 0 gives [0]; unreachable target 4 gives []; parallel edges 0 and 1 give one of them |
| dag unreachable cycle | n=4, source 0, edges 0->1, 2->3, 3->2 | acyclic=false, two empty arrays |
| dag 64 bit | path with three edges of 10^9 | distance[3] = 3 000 000 000 |
| dag deep chain | n=100 000, all weights -1 | distance[last] = -99 999 |
| state reset | cyclic, acyclic and scc requests alternate three times | identical answers every time |
| limit cases | all six tasks, n=100 000, m=250 000, weights up to 10^9 | finish within the 20 second limit; kruskal and prim report equal cost |
