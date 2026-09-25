# Assignment 2: Algorithmic Analysis, Correctness and Performance Trade-offs

Complete Java source, measured results, tests, and the English individual report. Start with the commands in the appendix or run `run.bat` / `sh run.sh`.

**Important:** the Workload 3 removal extension is explained in Section 4.3. A hosted GitHub repository link remains a separate publication step.

## 1. Overview

This project implements three integer data structures from scratch: a Dynamic Array, a singly Linked List with a tail reference, and an array-based binary Min-Heap. Its purpose is to connect algorithmic correctness and asymptotic analysis with reproducible measurements, following the supplied assignment brief [A]. All code comments and submission materials are in English.

| Structure | Implementation | Required operations |
| --- | --- | --- |
| DynamicArray | int[] with initial capacity 16; capacity doubles; no shrinking | add(x), add(index, x), remove(index), get(index), contains(x) |
| LinkedList | Nodes containing int value and Node next; head, tail, and size | add(x), add(index, x), remove(index), get(index), contains(x) |
| MinHeap | int[] with initial capacity 16; binary heap order; capacity doubles | insert(x), peekMin(), extractMin() |

The required structures do not use ArrayList, java.util.LinkedList, or PriorityQueue as internal storage. Standard collections are used only as test oracles and to store benchmark result records. IntList is a small shared interface; Metrics holds long counters. The source uses loops, arrays, classes, and ordinary methods, without streams, recursion, Maven, or a testing framework.

### 1.1 API and representation contracts

Indices start at zero. get and remove accept 0 <= index < size; indexed insertion accepts 0 <= index <= size. Invalid indices throw IndexOutOfBoundsException without changing the structure. remove and extractMin return the removed integer. Empty heap queries throw NoSuchElementException. Duplicate values and the full int range are supported. Unused array slots are not part of the logical structure.

The array invariant is 0 <= size <= capacity, with the active sequence in data[0..size-1]. The list invariant is that exactly size nodes are reachable from head, the last one is tail, and tail.next is null; both endpoints are null when empty. The heap invariant is data[parent] <= data[child] for every active non-root position. Shape is complete because active elements occupy a contiguous array prefix.

### 1.2 Correctness validation

Tests.java passed 188,698 checks. Coverage includes empty and one-element structures, multiple elements, duplicate and extreme values, valid boundary positions, invalid positions, repeated resizing, 100,000-element inputs, and appending after a list becomes empty. There are 3,000 seeded mixed operations per list and 10,000 mixed heap operations. Heap order is checked after every insertion/extraction in the mixed and ordered-input tests, not merely after the final operation.

DynamicArray is compared with ArrayList, the custom list with java.util.LinkedList, and the heap with PriorityQueue and Arrays.sort. Ascending and descending heap inputs exercise short and long sift paths. Counters have small exact-value tests. results/test-results.txt preserves the actual test output; results/verification.txt records independent CSV and formula checks.

## 2. Complexity Analysis

### 2.1 Notation and assumptions

n denotes the current number of stored elements when one operation begins. m denotes the number of operations in a workload. O(f(n)) is an asymptotic upper bound, Ω(f(n)) is a lower bound, and Θ(f(n)) gives both bounds for the specified case. O does not mean 'worst case', and Ω does not mean 'best case': for example, expected list access is both O(n) and Ω(n), hence Θ(n). Integer arithmetic, comparisons, and reference updates have unit cost in this analysis.

For index-dependent average cases, valid positions are uniformly distributed. Search uses a fixed mixture of 50% successful queries, uniformly selected among the stored distinct values, and 50% absent queries. Append and heap insertion explicitly distinguish amortized sequence cost from the cost of a single operation at a capacity boundary. Auxiliary space means temporary working space; the persistent representation is discussed separately.

| Dynamic Array operation | Best | Average / sequence | Worst | Auxiliary space |
| --- | --- | --- | --- | --- |
| add(x) | Θ(1) | Θ(1) amortized [1] | Θ(n) | Θ(n) on growth; otherwise Θ(1) |
| add(index, x) | Θ(1) | Θ(n) | Θ(n) | Θ(n) on growth; otherwise Θ(1) |
| remove(index) | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| get(index) | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| contains(x) | Θ(1) | Θ(n) | Θ(n) | Θ(1) |

| Linked List operation | Best | Average | Worst | Auxiliary space |
| --- | --- | --- | --- | --- |
| add(x) | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| add(index, x) | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| remove(index) | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| get(index) | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| contains(x) | Θ(1) | Θ(n) | Θ(n) | Θ(1) |

### 2.2 Justification for every list operation

Array append writes once unless capacity must grow. Indexed insertion shifts n-index existing values; removal shifts n-index-1. Therefore a uniformly chosen position costs Θ(n), while end operations can avoid shifting. get reads one array slot regardless of n. contains stops at the first matching value, but an absent value requires n comparisons; the fixed miss fraction makes the expected cost Θ(n), including an Ω(n) lower bound.

List append uses tail directly, so it is Θ(1), not Θ(n). Insertion at the head and insertion at size are also constant time. An interior insertion must first reach its predecessor. Removal at the head is constant time, but interior and tail removal need a predecessor traversal. get(i) visits i+1 nodes, including the requested node; its average is (n+1)/2. Search visits nodes in sequence until a match or the end. Link updates are constant time, but finding a location by numerical index is not.

### 2.3 Heap complexity and capacity growth

| Min-Heap operation | Best | Average / sequence | Worst | Auxiliary space |
| --- | --- | --- | --- | --- |
| insert(x) | Θ(1) | Θ(1) expected amortized under random order [2] | Θ(n) with growth; Θ(log n) without growth | Θ(n) on growth; otherwise Θ(1) |
| peekMin() | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| extractMin() | Θ(1) | Θ(log n) averaged over a random distinct-key drain [3] | Θ(log n) | Θ(1) |

insert appends a leaf and compares it with its parent while moving upward. At most floor(log2(n+1)) levels are visited. Without resizing, this gives O(log n), and inserting a new smallest key into a full-height position gives Ω(log n), hence a tight worst-case Θ(log n). The actual implementation may additionally copy the full backing array, making a single growing insertion Θ(n). A generic safe sequence bound is O(log n) amortized per insertion even for adversarial insertion orders.

peekMin checks non-emptiness and returns data[0], so all nonempty cases are Θ(1). extractMin replaces the root with the last active value and moves it downward. Each level uses at most two key comparisons: one to choose the smaller child, and one to compare the replacement with that child. The height gives O(log n); a value that must reach a leaf establishes the matching worst-case Ω(log n). A single element or an immediately ordered replacement gives Θ(1). Equal keys can stop the loop immediately.

### 2.4 Average is not the same as amortized

[1] For array append, a growth copies capacities 16, 32, 64, and so on. Their geometric sum is less than twice the largest copied capacity, hence O(N) over N appends. The N new writes give Ω(N), so total cost is Θ(N) and amortized append cost is Θ(1). This is a sequence guarantee, not a claim that a full-array append becomes constant time on average over input values. At a fixed full capacity, every append still costs Θ(n).

[2] The benchmark inserts a uniformly shuffled set of distinct keys into an empty heap. An additional theoretical source, Bollobás and Simon [B], proves a constant bound on the expected exchanges per insertion averaged over this random-order construction. At most one final unsuccessful parent comparison is added per insertion, and doubling contributes O(N) total copies. Thus the expected amortized cost is Θ(1), or Θ(N) for all N insertions. This is not a worst-case guarantee, and it does not apply to every possible mixture of insertions and removals.

[3] Inserting and then draining random distinct keys is a comparison-based sorting procedure. The expected comparison lower bound for sorting is Ω(N log N). Since the expected insertion phase is O(N), the extraction phase must supply Ω(N log N) work on average. Its height-based upper bound is O(N log N), so a full drain has expected Θ(N log N) cost, or Θ(log N) per extraction averaged over the drain. This statement is not a claim about every fixed heap configuration.

### 2.5 Persistent memory

A linked list keeps Θ(n) nodes. Each insertion adds one persistent node, while its temporary working space is Θ(1). The array and heap keep a backing array and do not shrink it: after deletions, retained capacity is Θ(max(16, historical maximum size)), not necessarily Θ(current n). During growth both old and new arrays temporarily coexist, explaining Θ(n) auxiliary space. The benchmark's pre-generated input and heap output arrays are harness storage, not auxiliary space of a single data-structure operation.

## 3. Correctness

### 3.1 Proof: DynamicArray.add(index, value)

Precondition: the array representation invariant holds, 0 <= index <= s, and s is the size before insertion. Let A[0..s-1] be the original logical sequence. ensureCapacity preserves those values and makes room for s+1 elements. It does not change size. The following proof concerns the backward-shift loop in the actual implementation.

```
for (int i = size; i > index; i--) {
    data[i] = data[i - 1];
    metrics.accesses += 2;
    metrics.movements++;
}
data[index] = value;
size++;
```

#### Loop invariant

At the beginning of an iteration with loop variable i: (a) index <= i <= s; (b) for every 0 <= k < i, data[k] = A[k], so all not-yet-read prefix values are intact; (c) for every i < k <= s, data[k] = A[k-1], so the already-processed suffix has been shifted right by exactly one position. The slot data[i] is not constrained. size remains s during the loop.

#### Initialization

Initially i=s. The entire original prefix data[0..s-1] still equals A after the capacity check. The processed suffix s<k<=s is empty, so condition (c) is vacuously true. The valid insertion position gives index<=s, establishing all parts of the invariant before the first iteration.

#### Maintenance

When i>index, the source index i-1 is in the intact prefix. Therefore data[i-1] is still A[i-1]. Assigning it to data[i] creates the correct right-shifted value at position i without overwriting an unread source. After i decreases by one, the new processed suffix contains this newly copied slot and all slots already correct by the invariant. The remaining smaller prefix is unchanged, so the invariant holds for the next iteration. Counter updates affect no sequence values.

#### Termination and progress

The nonnegative integer i-index decreases by one after each iteration and cannot decrease forever. The loop terminates with i=index. At that point positions below index equal A[k], and every position k from index+1 through s equals A[k-1]. There is exactly one available logical gap at index.

#### Postcondition and correctness

Writing value into data[index] fills that gap; incrementing size changes the active range to 0..s. The resulting logical sequence is A[0..index-1], followed by value, followed by A[index..s-1]. No original element is lost or reordered. Capacity is sufficient and size is valid, so both the abstract insertion contract and the representation invariant hold. The proof also covers insertion at the end: the loop then executes zero times.

### 3.2 Proof: MinHeap.extractMin() sift-down

Precondition: the heap is nonempty, its active array prefix is complete, and every parent key is no larger than its children. Consequently the root is no larger than any descendant, by following the ordered path from the root. The method saves this minimum, removes the last position, and, unless the heap becomes empty, places that last value at the root. Only the root's outgoing order relations can now be wrong.

#### Loop invariant

At the beginning of each sift-down iteration, with current position index: (a) the active keys form exactly the original multiset minus the saved minimum, and their positions still form a complete tree; (b) every parent-child edge not outgoing from index is ordered, so each child subtree of index is a valid heap; (c) every proper ancestor of index is no larger than every key in the subtree rooted at index. Thus the only remaining possible violations are between the replacement at index and its children. Clause (c) also guarantees that moving a child upward cannot break order with an ancestor.

#### Initialization

If size becomes zero, returning the saved root immediately is correct and no loop is required. Otherwise index=0. Removing the final leaf preserves completeness and all edges elsewhere in the tree. Replacing the root preserves the required remaining multiset. Only its outgoing edges may be unordered, while the condition about proper ancestors is vacuous because the root has none. All invariant clauses therefore hold.

#### Maintenance

Suppose the current key is x and the smaller child key is y. Both child subtrees are heaps. If x>y, the code swaps x with this smaller child and moves index to that child's old position. The promoted y is no larger than the sibling, because it was chosen as the smaller child, and it is no larger than x, because the swap is performed only when x>y. Therefore the outgoing edges at the old position become ordered. The edge to the old parent stays ordered by clause (c).

The former subtree rooted at y was a heap, so y was no larger than all of its descendants; it is also smaller than the displaced x. Hence y, now an ancestor, is no larger than all keys in the new current subtree. Earlier ancestors retain that property. All other edges are unchanged. Only x at the new index can still violate order with its children. Swapping changes neither the active multiset nor the complete shape, establishing the invariant again.

#### Termination and progress

Each swap moves index down by one level, reducing the remaining height by one, so there can be at most the heap height in swaps. The loop stops either when no left child exists, which also means no right child exists in a complete tree, or when x<=y. In the second case, x is no larger than the smaller child and therefore no larger than either child. Both stopping conditions eliminate the only possible remaining order violations.

#### Postcondition and correctness

At termination all heap edges are ordered, the shape is complete, and exactly the original keys except the saved minimum remain. The returned value was the original global minimum. Therefore extractMin returns the correct value and restores the heap representation invariant. Equal child keys cause no difficulty: either smaller child is a valid choice, and an equal parent correctly stops the loop.

## 4. Experimental Setup

### 4.1 Fixed protocol

Every applicable workload uses n = 100, 1,000, 10,000, and 100,000. n is the initial list size; Workload 4 specifically starts from an empty heap and inserts n keys. Each combination has exactly five recorded repetitions. Two preliminary, unreported full warm-up rounds at n=1,000 run before the measured repetitions. Each n gets a new Random(42); all repetitions and both list structures receive the same pre-generated arrays.

Initial input is a Fisher-Yates shuffle of integers 0 through n-1. Thus the input consists of n distinct random integers with a uniformly random relative order, rather than an independent sample with replacement. Duplicate handling is tested separately. Indices are uniformly sampled from [0,n-1]. Search queries alternate between uniformly selected stored values and negative absent values, giving exactly 500 successful and 500 unsuccessful searches. New insertion values are also generated in advance.

| Workload | Structures | Operations m | Timed action |
| --- | --- | --- | --- |
| 1: Random access | Array and list | 10,000 | get(index) on the pre-generated index sequence |
| 2: Search | Array and list | 1,000 | contains(value); 50% hits and 50% misses |
| 3: Mutation | Array and list | 1,000 per experiment | Separate insertion/removal at 0 and fixed floor(n/2) |
| 4: Priority | Min-Heap | n insertions; n extractions | Measure insertion and extraction phases separately |

System.nanoTime measures elapsed time around only the operation loops. Input generation, list pre-filling, printing, restoration, and result validation are excluded. Allocation caused by the operations themselves, such as node creation or capacity growth, is included. Counter updates and loop/checksum bookkeeping are included. The heap extraction loop writes into a preallocated output array; its allocation and later sorting check are excluded. A volatile sink consumes results after the measured intervals.

### 4.2 Metric definitions

Array accesses count reads and writes of integer slots explicitly performed by the implementation. An ordinary get counts one; shifting one value counts one read and one write. Array movements count copies of existing elements to another slot, including copies during growth, but not the first write of a newly inserted value. JVM allocation/zero-initialization is not included in these logical counters, although its execution time is included when it happens inside an operation.

List accesses count logical node touches: every visited node during traversal, each directly handled removed node, and each new node created for insertion. A nonempty tail append also touches the old tail. A head insertion/removal counts one node; a fixed positive index i insertion/removal counts i+1. List movements are zero because existing values are not relocated. These node touches are not physically equivalent to array-slot reads/writes, so the counts should not be treated as identical machine instructions.

Comparisons count only comparisons between stored values and a search key, or between heap keys. They exclude index checks, loop conditions, and validation. Heap accesses are not instrumented: the zero access field in its CSV rows is an unused field, not a claim that the heap performs no memory accesses. Heap comparisons are the required metric; movements are retained as supplementary information. All counts use long to avoid 32-bit counter overflow.

### 4.3 Necessary interpretation of Workload 3

The brief requires 1,000 removals after restoring only n original elements, including n=100. That sequence cannot run as written. A fixed index n/2 also becomes invalid after n-n/2 removals, so n=1,000 middle removal cannot complete 1,000 operations either. This package does not silently skip operations, change m, or replace the requested fixed index with size()/2.

The chosen extension is a repeatable valid-batch protocol. Before a batch, rebuild the original n-element structure outside timing. Remove at the unchanged index until either all remaining required operations are completed or that index becomes invalid. Then rebuild again outside timing and continue. Sum the timed intervals and counters until exactly 1,000 successful removals have occurred. This is a documented resolution of an inconsistent instruction, not proof that the instructor has approved the interpretation [A, Section 7].

| Initial n | Front index | Front batches | Middle index | Middle batches |
| --- | --- | --- | --- | --- |
| 100 | 0 | 10 | 50 | 20 |
| 1,000 | 0 | 1 | 500 | 2 |
| 10,000 | 0 | 1 | 5,000 | 1 |
| 100,000 | 0 | 1 | 50,000 | 1 |

A batch count includes the first prepared structure: later restorations equal batches minus one. Insertion always starts from a separate original structure and performs all 1,000 insertions without resets. The middle index is computed once from initial n and remains fixed for both insertion and removal, as specified in the brief. Thus it need not stay at the midpoint of the changing structure.

### 4.4 Exact workload formulas

For array insertion at fixed index i, the shifts alone equal m(n-i) + m(m-1)/2. Add any measured growth copies to obtain total movements. Therefore the total insertion bound is Θ(mn+m²) for i=0 or floor(n/2), not merely Θ(mn) when m is allowed to vary independently. For the assignment's fixed m=1,000 it approaches linear growth in n once n dominates m.

For removal, let a=n-i be the number of valid removals in a restored batch, q=floor(m/a), and r=m mod a. Exact array movements are q·a(a-1)/2 + r·a - r(r+1)/2. A batch of b removals contributes b(n-i-1)-b(b-1)/2 movements. Array accesses in either mutation experiment equal 2·movements+m. At the tested sizes, batched removal has Θ(mn) total array work. Every list mutation at the fixed positions touches exactly m(i+1) nodes: Θ(m) at the front and Θ(mn) in the middle.

### 4.5 Environment and limitations of control

The recorded run used OpenJDK 21.0.11, the OpenJDK 64-Bit Server VM, Linux 6.18.44, amd64, four processors available to the container, and JVM arguments -Xms256m -Xmx1g. It ran in a shared execution container, not on the student's own computer. Exact runtime metadata is preserved in results/environment.txt. No claim is made about the physical CPU model or dedicated access to the host.

Structure order alternates between repetitions to reduce systematic order bias. The protocol does not pin CPU cores, force garbage collection, use independent JVM forks, or prove that compilation has fully stabilized. Restored inputs can affect cache state, and multiple short removal intervals increase timer overhead. These are limitations to discuss, not reasons to change or discard measured values.

## 5. Results

All times below are arithmetic means of five actual recorded repetitions, in milliseconds. No timing was fabricated, normalized into a desired trend, or removed as an outlier. raw.csv has 280 rows; summary.csv has 56 experiment rows, including mean, minimum, maximum, and sample standard deviation. Counter values are identical across the five seeded repetitions, so the displayed means are exact integer counts. 'Array' and 'List' refer to the custom implementations.

### 5.1 Workload 1: random access

| n | Structure | Mean ms | Accesses | Total theory |
| --- | --- | --- | --- | --- |
| 100 | Array | 0.180303 | 10,000 | Θ(m) |
| 100 | List | 1.062169 | 511,261 | Θ(mn), expected |
| 1,000 | Array | 0.248931 | 10,000 | Θ(m) |
| 1,000 | List | 8.283057 | 5,021,758 | Θ(mn), expected |
| 10,000 | Array | 0.215466 | 10,000 | Θ(m) |
| 10,000 | List | 91.494426 | 50,177,030 | Θ(mn), expected |
| 100,000 | Array | 0.037760 | 10,000 | Θ(m) |
| 100,000 | List | 891.315203 | 505,044,105 | Θ(mn), expected |

![Figure 1. Execution time versus n for 10,000 random accesses. Error bars show the observed minimum and maximum, not confidence intervals.](results/plots/01_access_time.png)

Figure 1. Execution time versus n for 10,000 random accesses. Error bars show the observed minimum and maximum, not confidence intervals.

The array always performs exactly 10,000 slot accesses. The list grows from 511,261 node visits at n=100 to 505,044,105 at n=100,000, close to the expected m(n+1)/2. At the largest n, the recorded means are 0.037760 ms for the array and 891.315203 ms for the list. The operation counts directly agree with constant-time array access and linear expected list access. The non-monotonic array timing is addressed in Section 6; Θ(1) does not predict identical elapsed times.

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

![Figure 2. Search time for 1,000 queries, of which exactly half are absent. The query sequence is identical for both structures.](results/plots/03_search_time.png)

Figure 2. Search time for 1,000 queries, of which exactly half are absent. The query sequence is identical for both structures.

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

![Figure 3. Insertion times for both structures and positions. Growth and new-node allocation are included when they occur inside insertions.](results/plots/05_insert_time.png)

Figure 3. Insertion times for both structures and positions. Growth and new-node allocation are included when they occur inside insertions.

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

![Figure 4. Removal times under the valid-batch protocol. The small-n rows use more timed intervals than the large-n rows.](results/plots/06_remove_time.png)

Figure 4. Removal times under the valid-batch protocol. The small-n rows use more timed intervals than the large-n rows.

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

![Figure 5. Separate timings for inserting n random-order keys and extracting n minima from the resulting heap.](results/plots/07_heap_time.png)

Figure 5. Separate timings for inserting n random-order keys and extracting n minima from the resulting heap.

Insertion comparisons rise from 220 to 227,941; extraction comparisons rise from 855 to 2,831,730. At n=100,000, insertion takes 4.729696 ms and extraction takes 14.429577 ms. All extracted arrays are checked for non-decreasing order and exact equality with a separately sorted copy of the input, outside timing.

Random-order insertion usually climbs only a short part of the heap, whereas draining repeatedly sends replacement keys down the tree. This is compatible with the distinct expected and worst-case bounds in Section 2, not evidence that worst-case sift-up is constant time. peekMin is tested and analyzed as Θ(1), but there is no separate peekMin benchmark because Workload 4 requires only insertion and extraction timings.

### 5.6 Required metric-versus-n plots

![Figure 6. Access counts versus n: a constant 10,000 for the array and approximately 10,000(n+1)/2 for the list. These are logical counts, not CPU instructions.](results/plots/02_access_counts.png)

Figure 6. Access counts versus n: a constant 10,000 for the array and approximately 10,000(n+1)/2 for the list. These are logical counts, not CPU instructions.

The count plot removes much of the ambiguity visible in the small elapsed times. Exactly one array access occurs per get. List visits increase in proportion to the expected distance from the head.

![Figure 7. One curve represents both structures because their search comparison counts are identical at every n. This directly follows from identical data and queries.](results/plots/04_search_comparisons.png)

Figure 7. One curve represents both structures because their search comparison counts are identical at every n. This directly follows from identical data and queries.

Figures 1 and 6 meet the two required plot types in the brief: execution time versus n and operations/accesses versus n. The other plots add workload-specific evidence. Both axes use logarithmic scales; the original numeric values remain in the tables.

### 5.7 Heap counts and normalized growth

![Figure 8. Comparison counts for heap insertion and extraction, independently of elapsed-time noise.](results/plots/08_heap_comparisons.png)

Figure 8. Comparison counts for heap insertion and extraction, independently of elapsed-time noise.

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

### 6.6 Why is the Dynamic Array preferable for some workloads?

It provides true indexed access and avoids pointer traversal, making it the clear choice here for read-heavy indexed workloads. It is also the faster measured search implementation despite identical comparison counts, and it outperforms the linked list on the measured large middle-index mutations. It is not universally preferable: repeated insertion/removal at the front shifts many values, which is the workload where the linked list has the stronger operation bound.

### 6.7 When can a Linked List be useful?

The head can be updated without shifting the remaining sequence. This is useful for front-heavy workloads or for operations at a predecessor already known to the algorithm. The latter is a general representation capability, not a separate public method in this project. Because the exposed API takes numerical indices, an interior position must be found by traversal before linking. Therefore the statement 'linked-list insertion is O(1)' is incomplete unless the access to the location is also specified.

### 6.8 Why is a heap appropriate for priority processing?

The root gives the minimum directly, and insertion/extraction restore only a logarithmic-height path in the worst case when capacity is available. Repeatedly scanning an unsorted sequence to find the next minimum would perform n+(n-1)+...+1 = Θ(n²) value inspections for a full drain. The heap's extraction phase has O(n log n) total work instead. A heap does not support constant-time arbitrary rank access or keep every array position globally sorted; its value is the efficient next-priority operation.

### 6.9 How does the workload determine the design choice?

The dominant operations, their positions, n, and m matter together. A structure that is excellent for indexed reads can be poor for repeated front deletion. Cheap pointer updates do not help when each operation first needs a long index search. Priority processing requires a different ordering contract from either list. Decisions should be based on the expected operation mix and measured costs under a controlled workload, rather than a single complexity label or one favorable timing.

### 6.10 What the experiment does not establish

Five repetitions of one seeded dataset measure timing variation, not variation across many random datasets. No confidence interval for a broad population is claimed. Four values of n cannot prove asymptotic behavior. Counter units differ between array slot work and list node touches; direct numerical ratios between those metrics are not speed predictions. The smaller-n removal experiments include repeated restorations by necessity, so they are extensions of the otherwise impossible instruction. Heap insert results describe a random permutation, not adversarial descending priorities. The report separates these limitations from the correctness proofs and exact operation-count checks.

## 7. Design Recommendations

| Dominant workload | Recommended structure | Reason and qualification |
| --- | --- | --- |
| Frequent get(index) | Dynamic Array | One indexed slot read; the list traverses from head. |
| Repeated contains on an unsorted sequence | Dynamic Array among these two | Both are Θ(n), but the array was faster in these measurements; neither eliminates linear searching. |
| Frequent front insertion/removal | Linked List | Direct head updates; no relocation of all remaining values. |
| Frequent middle operations by numeric index | Dynamic Array for the measured sizes | Both depend linearly on n; traversal made the list slower here. A different API with known nodes would change the comparison. |
| Repeated next-minimum processing | Min-Heap | Constant-time peek; logarithmic-height sift paths and O(n log n) draining. |
| Mostly append, later indexed reads | Dynamic Array | Θ(1) amortized append and Θ(1) get; capacity spikes must still be accepted. |

These recommendations are specific to the implemented structures and operation contracts. They do not claim that one structure is fastest for every program, that all linked-list operations are constant time, or that random-order heap insertion has constant worst-case latency. For strict per-operation latency requirements, backing-array growth would need to be considered explicitly.

## 8. Conclusion

The implementations preserve their data-structure properties and pass reference-based, randomized, boundary, and large-input tests. Two complete loop-invariant proofs establish the correctness of backward array insertion and heap extraction. Logical metrics match the exact shift/traversal formulas and support the workload-level complexity analysis. Timing measurements broadly support the predicted differences, while also showing why warm-up, constant factors, and experimental limitations must be discussed.

The central result is that representation and operation mix must be considered together: arrays are strong for indexed reading, head updates favor a linked list, and priority extraction favors a heap. Average, amortized, and worst-case costs are not interchangeable. All tables and plots are reproducible from the source code; the necessary Workload 3 interpretation and the remaining hosted GitHub publication step are explicitly documented.

### References and evidence

[A] Supplied course brief: Assignment 2 — Algorithmic Analysis, Correctness and Performance Trade-offs, Assignment 2-1.pdf, 7 pages. Sections 3-5 specify implementations, proofs, and complexity; Sections 6-10 specify experiments and tests; Sections 11-14 specify the repository, report, and rubric. The file is the source of the assignment requirements, not of the measured results.

[B] Bollobás, B., and Simon, I. (1985). Repeated random insertion into a priority queue. Journal of Algorithms, 6(4), 466-477. DOI: 10.1016/0196-6774(85)90028-8. This additional theoretical reference supports the random-order expected insertion discussion; it is not an extra requirement from the supplied brief.

[C] Project evidence: src/*.java; results/tables/raw.csv; results/tables/summary.csv; results/environment.txt; results/test-results.txt; results/benchmark-log.txt; results/verification.txt. All empirical claims in this report are derived from these local artifacts. The proofs, formula derivations, and interpretation of the removal inconsistency are the analysis presented in this project.

## Appendix: Reproduction and Submission

### Run from the extracted project root

The recorded implementation was compiled and run with JDK 21. Java source files use no external libraries. Tests do not depend on -ea because failed checks explicitly throw AssertionError. The commands below work from the project directory; Windows users can alternatively run run.bat, and macOS/Linux users can run sh run.sh.

```
mkdir -p out
javac -Xlint:all -d out src/*.java
java -cp out Tests
java -Xms256m -Xmx1g -cp out Benchmark
```

Only regenerating plots and the Word report requires Python packages. The existing plots and report can be read without Python. To regenerate all derived artifacts after new Java measurements:

```
python -m pip install matplotlib python-docx
python scripts/verify_results.py
python scripts/make_plots.py
python scripts/build_report.py
```

Rerunning Benchmark overwrites raw.csv, summary.csv, and environment.txt with new measurements. Regenerate the README, report, and plots together so they remain consistent. The shell scripts run Java only; Python is an optional reporting step. Do not expect identical times on another computer, but seed-derived operation counts should match.

### Rubric evidence map

| Criterion | Points | Evidence / status |
| --- | --- | --- |
| Data-structure implementation | 20 | src/DynamicArray.java, LinkedList.java, MinHeap.java |
| O / Ω / Θ analysis | 15 | Section 2; operation and workload bounds |
| Correctness and loop invariants | 15 | Section 3; two complete proofs |
| Experimental design and workloads | 10 | Section 4; Benchmark.java; W3 interpretation needs approval |
| Benchmarking and metrics | 15 | Five repetitions, nanoTime, seed 42, raw.csv, counters |
| Results, plots, interpretation | 10 | Section 5; 56 result rows and eight plots |
| Performance/design analysis | 5 | Sections 6 and 7; answers to all nine questions |
| Report and README | 5 | English report and matching README.md |
| GitHub / code quality | 5 | Local staged history included; hosted repository link still required |

The local repository contains genuine preparation-stage commits under the neutral author Assignment Project Builder. No dates were fabricated and no previous student development is claimed. A hosted GitHub repository has not been created by this package: publish it under an authorized account and submit its link. docs/PUBLISHING.md explains the handoff; docs/DEVELOPMENT.md records provenance. This checklist identifies coverage, not a guaranteed grade.

