# Measured results

### 5.2 Workload 2: search

| n | Structure | Mean ms | Comparisons | Total theory |
| --- | --- | --- | --- | --- |
| 100 | Array | 0.170587 | 75,912 | Θ(mn), expected |
| 100 | List | 0.248330 | 75,912 | Θ(mn), expected |
| 1,000 | Array | 0.902965 | 740,571 | Θ(mn), expected |
| 1,000 | List | 1.866798 | 740,571 | Θ(mn), expected |
| 10,000 | Array | 8.574618 | 7,478,562 | Θ(mn), expected |
| 10,000 | List | 18.969168 | 7,478,562 | Θ(mn), expected |
| 100,000 | Array | 62.911661 | 74,864,907 | Θ(mn), expected |
| 100,000 | List | 182.591980 | 74,864,907 | Θ(mn), expected |

For a successful query chosen uniformly among n distinct stored values, the expected comparisons are (n+1)/2. An unsuccessful query costs n. The 50/50 mixture therefore has expected total comparisons m(3n+1)/4. The observed counts closely follow this expression and are exactly equal between the two structures because both scan the same sequence and stop at the same first match.

At n=100,000 both structures perform 74,864,907 comparisons, but the array takes 62.911661 ms and the list takes 182.591980 ms. The latter is about 2.90 times the array time in this run. The equality of comparison counts, alongside different times, shows why the same Θ(n) search bound does not imply identical physical cost. Traversing next references and reading consecutive array slots are different implementations; cache and compiler effects are plausible contributors but were not measured separately.

### 5.3 Workload 3A: insertion

m=1,000. The logical metric column is movements for Array and node accesses for List. Array growth copies are included; all list movements are zero. Middle uses the fixed initial index n/2.

| n | Position | Type | Mean ms | Logical metric | Total theory |
| --- | --- | --- | --- | --- | --- |
| 100 | Front | Array | 0.493324 | 601,420 | Θ(mn+m²) |
| 100 | Front | List | 0.210397 | 1,000 | Θ(m) |
| 100 | Middle | Array | 0.473881 | 551,420 | Θ(mn+m²) |
| 100 | Middle | List | 0.291857 | 51,000 | Θ(mn) |
| 1,000 | Front | Array | 0.989374 | 1,500,524 | Θ(mn+m²) |
| 1,000 | Front | List | 0.203464 | 1,000 | Θ(m) |
| 1,000 | Middle | Array | 0.657026 | 1,000,524 | Θ(mn+m²) |
| 1,000 | Middle | List | 1.314906 | 501,000 | Θ(mn) |
| 10,000 | Front | Array | 5.885498 | 10,499,500 | Θ(mn+m²) |
| 10,000 | Front | List | 0.258653 | 1,000 | Θ(m) |
| 10,000 | Middle | Array | 3.106089 | 5,499,500 | Θ(mn+m²) |
| 10,000 | Middle | List | 8.382661 | 5,001,000 | Θ(mn) |
| 100,000 | Front | Array | 43.360750 | 100,499,500 | Θ(mn+m²) |
| 100,000 | Front | List | 0.064630 | 1,000 | Θ(m) |
| 100,000 | Middle | Array | 21.265650 | 50,499,500 | Θ(mn+m²) |
| 100,000 | Middle | List | 91.791539 | 50,001,000 | Θ(mn) |

At n=100,000, front insertion shifts 100,499,500 array elements, while list insertion touches only 1,000 new nodes. In the middle, the list must perform 50,001,000 node accesses to locate the fixed position repeatedly. Its 91.791539 ms is slower here than the array's 21.265650 ms, even though array insertion moves data and both have linear dependence on n for fixed m.

### 5.4 Workload 3B: removal

Each row still represents exactly 1,000 successful removals. The batch count identifies the documented restoration extension. Direct restoration time and restoration counters are excluded. The logical metric has the same meaning as in the insertion table.

| n | Position | Type | Mean ms | Logical metric | Batches | Total theory |
| --- | --- | --- | --- | --- | --- | --- |
| 100 | Front | Array | 0.108571 | 49,500 | 10 | Θ(mn) |
| 100 | Front | List | 0.108977 | 1,000 | 10 | Θ(m) |
| 100 | Middle | Array | 0.089136 | 24,500 | 20 | Θ(mn) |
| 100 | Middle | List | 0.142911 | 51,000 | 20 | Θ(mn) |
| 1,000 | Front | Array | 0.344340 | 499,500 | 1 | Θ(mn) |
| 1,000 | Front | List | 0.062111 | 1,000 | 1 | Θ(m) |
| 1,000 | Middle | Array | 0.197550 | 249,500 | 2 | Θ(mn) |
| 1,000 | Middle | List | 0.785693 | 501,000 | 2 | Θ(mn) |
| 10,000 | Front | Array | 6.622496 | 9,499,500 | 1 | Θ(mn) |
| 10,000 | Front | List | 0.042886 | 1,000 | 1 | Θ(m) |
| 10,000 | Middle | Array | 3.031668 | 4,499,500 | 1 | Θ(mn) |
| 10,000 | Middle | List | 8.022517 | 5,001,000 | 1 | Θ(mn) |
| 100,000 | Front | Array | 56.414714 | 99,499,500 | 1 | Θ(mn) |
| 100,000 | Front | List | 0.006292 | 1,000 | 1 | Θ(m) |
| 100,000 | Middle | Array | 30.342877 | 49,499,500 | 1 | Θ(mn) |
| 100,000 | Middle | List | 88.564759 | 50,001,000 | 1 | Θ(mn) |

At n=100,000, front list removal touches 1,000 nodes and takes 0.006292 ms; front array removal moves 99,499,500 values and takes 56.414714 ms. Middle array removal moves about half as many elements as front removal, whereas middle list removal must repeatedly traverse from the head. At n=100, timer and batch overhead are large relative to the useful work, so tiny timing differences should not be overinterpreted.

### 5.5 Workload 4: priority processing

| n | Phase | Mean ms | Comparisons | Total theory |
| --- | --- | --- | --- | --- |
| 100 | Extract | 0.045777 | 855 | Θ(n log n) expected; O(n log n) worst |
| 100 | Insert | 0.018131 | 220 | Θ(n) expected; O(n log n) worst |
| 1,000 | Extract | 0.139247 | 15,018 | Θ(n log n) expected; O(n log n) worst |
| 1,000 | Insert | 0.215489 | 2,281 | Θ(n) expected; O(n log n) worst |
| 10,000 | Extract | 1.450214 | 216,624 | Θ(n log n) expected; O(n log n) worst |
| 10,000 | Insert | 1.045767 | 22,918 | Θ(n) expected; O(n log n) worst |
| 100,000 | Extract | 14.429577 | 2,831,730 | Θ(n log n) expected; O(n log n) worst |
| 100,000 | Insert | 4.729696 | 227,941 | Θ(n) expected; O(n log n) worst |

Insertion comparisons rise from 220 to 227,941; extraction comparisons rise from 855 to 2,831,730. At n=100,000, insertion takes 4.729696 ms and extraction takes 14.429577 ms. All extracted arrays are checked for non-decreasing order and exact equality with a separately sorted copy of the input, outside timing.

Random-order insertion usually climbs only a short part of the heap, whereas draining repeatedly sends replacement keys down the tree. This is compatible with the distinct expected and worst-case bounds in Section 2, not evidence that worst-case sift-up is constant time. peekMin is tested and analyzed as Θ(1), but there is no separate peekMin benchmark because Workload 4 requires only insertion and extraction timings.

### 5.6 Required metric-versus-n plots

The count plot removes much of the ambiguity visible in the small elapsed times. Exactly one array access occurs per get. List visits increase in proportion to the expected distance from the head.

Figures 1 and 6 meet the two required plot types in the brief: execution time versus n and operations/accesses versus n. The other plots add workload-specific evidence. Both axes use logarithmic scales; the original numeric values remain in the tables.

### 5.7 Heap counts and normalized growth

| n | Insertion comparisons / n | Extraction comparisons / n | Extraction comparisons / (n log2 n) |
| --- | --- | --- | --- |
| 100 | 2.2000 | 8.5500 | 1.2869 |
| 1,000 | 2.2810 | 15.0180 | 1.5070 |
| 10,000 | 2.2918 | 21.6624 | 1.6303 |
| 100,000 | 2.2794 | 28.3173 | 1.7049 |

Insertion comparisons per key stay near a constant across the tested range. Extraction comparisons per key increase with heap height, while normalization by n log2 n makes the growth much more stable. These observations support the workload-specific expectations without converting a finite experiment into a mathematical proof.

The observed n log n behavior refers to the full extraction phase, where the heap becomes smaller after every extraction. Its upper bound is the sum of O(log k) for k from 1 through n, which is O(n log n), not n repetitions on a heap that remains size n. Expected lower bounds and the role of distinct random keys are explained in Section 2.4.

Counter validation is separate from plotting: scripts/verify_results.py checks all 56 groups against raw.csv, verifies five repetitions, recomputes means and sample standard deviations, confirms search-hit counts, and checks exact list mutation formulas including resize copies and batch counts. Thus plot lines are derived from verified measurements rather than manually entered numbers.

## 6. Discussion

### 6.1 How does increasing n affect each workload?

For fixed m, array random access retains Θ(m) logical work. List random access increases as Θ(mn), and both search implementations increase as Θ(mn) under the fixed hit/miss mixture. Array insertion at a fixed front or middle index costs Θ(mn+m²), while list front insertion/removal stays Θ(m). Middle list mutation costs Θ(mn), because the index must be located repeatedly. Batched array removal has Θ(mn) work at the requested sizes. In Workload 4, m itself increases with n: random-order construction is expected Θ(n), while draining is expected Θ(n log n).

### 6.2 Which measurements agree with theory?

The strongest agreement is in logical counts: array random access is exactly 10,000 at every n; list access closely follows m(n+1)/2; both search counts agree exactly with each other and closely with m(3n+1)/4. Array mutation counts match the shift and capacity-growth formulas exactly. List head mutation always counts 1,000 touches, and middle mutation exactly 1,000(n/2+1). Heap comparisons show roughly constant insertion work per key and increasing extraction work per key. All of these observations are consistent with the derived bounds.

### 6.3 Where do measurements differ from a simple timing prediction?

Array random access is not flat in milliseconds: its mean is 0.180303 at n=100, 0.215466 at n=10,000, and 0.037760 at n=100,000 despite identical access counts. For n=10,000, individual runs range from 0.026866 to 0.651361 ms. Heap extraction at n=100 also has a wide range. These are retained observations, not corrected values.

Plausible explanations include ongoing just-in-time compilation, scheduling on the shared host, cache state, and the large relative cost of timing short loops. The available data do not isolate which cause dominates. The smaller runtime at a larger n therefore does not establish a better asymptotic bound. Warm-up reduces startup effects but does not prove steady state. The extra restoration intervals at small n are another known source of nonuniform timer overhead.

### 6.4 Why can equal Big-O algorithms have different times?

In search at n=100,000, both structures compare 74,864,907 values, yet their means differ by about 2.90 times. Big-O suppresses constant factors and does not specify memory layout, allocation, method dispatch, or the cost of following a dependent pointer. The experiment demonstrates different constants; the explanation based on representation is plausible, while a claim about exact cache misses would require additional measurements.

### 6.5 How do implementation details affect performance?

Keeping tail changes list append from a traversal to direct linking. Doubling makes repeated append efficient but creates occasional expensive operations. Not shrinking avoids shrink-copy costs while retaining capacity. Using primitive int arrays avoids per-element node allocation; list insertion allocates a node for each value. Early-stop conditions reduce heap work on already ordered keys and duplicates. Counter updates are inside the timed operations, so the numbers describe these instrumented implementations, not all possible Java implementations of the same abstract structures.

