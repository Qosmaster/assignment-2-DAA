# Defense guide

This guide explains the implementation. Use it to understand the project rather
than memorizing claims about work you have not reviewed.

## A short project explanation

The project compares a Dynamic Array, a singly Linked List, and a binary Min-Heap.
The structures store integers and are implemented without standard collections
as their internal storage. I need to explain both correctness and performance:
the same abstract operation can have different costs in different representations.
The benchmark separates input generation from operation timing and preserves the
same seed, workload size, and query sequence across compared structures.

## What each source file does

`DynamicArray.java` stores values in an `int[]`. `size` is the number of active
values, while `data.length` is allocated capacity. The capacity begins at 16 and
doubles only when full. The unused part of the array is not part of the sequence.

`LinkedList.java` stores a chain of nodes. Each node has a value and a `next`
reference. `head` points to the first node, `tail` to the last, and `size` counts
nodes. Keeping `tail` makes append constant time. Numerical indexed access still
starts from `head` and follows links.

`MinHeap.java` stores a complete binary tree in an array. A node at index `i` has
parent `(i-1)/2`, left child `2*i+1`, and right child `2*i+2`. The parent key is no
larger than either child key. The entire array is not globally sorted.

`IntList.java` defines the operations that both list implementations share. It
lets the benchmark use the same operation loops for both types. It is not a
standard library collection.

`Metrics.java` contains three `long` counters: accesses, comparisons, and movements.
A long is useful because workloads can count hundreds of millions of operations.
The metrics are logical work counts, not measurements of CPU instructions.

`Tests.java` checks known cases and compares random operations with standard
collections. The reference collections are allowed for validation, not used to
implement the required structures. A failed check throws `AssertionError`.

`Benchmark.java` generates inputs, runs the fixed workloads, times the operation
loops, validates outputs, and writes raw and summary CSV files. The nested Inputs
and Result classes are just small containers for the benchmark data.

`Demo.java` is an optional short interactive-defense demonstration. It is not
part of the measured workloads.

## Explain an array insertion step by step

Start with `[10, 20, 30]` and call `add(1, 99)`.

The index is valid, and there is room. With old size 3, the loop first writes
`data[3] = data[2]`, moving 30 right. It then writes `data[2] = data[1]`, moving 20
right. The loop stops at index 1. Finally, it writes 99 at index 1 and increases
size to 4. The result is `[10, 99, 20, 30]`.

The loop goes backward so it does not overwrite a value that has not yet been
copied. The invariant describes an intact prefix and an already-shifted suffix.
Initialization, maintenance, termination, and the final insertion write together
prove the resulting order. The complete formal proof is in README Section 3.1.

Removing an array element does the reverse kind of repair: read the removed value,
shift its suffix left, reduce size, and return the saved value. Removing the last
value needs no shift. Removing the first value usually shifts almost the whole array.

## Explain linked-list mutations

A new first node points to the old head, and then `head` changes to that new node.
No existing value needs to move. Removing the first node changes head to its next
node. If this makes the list empty, tail must also become null.

For a middle insertion, first find the predecessor. Make the new node point to
`previous.next`, then set `previous.next` to the new node. Reversing those two
assignments carelessly could lose the original suffix or make a wrong link.

For middle removal, find the predecessor and bypass its next node. Tail must be
updated when the removed node was last. The link change is constant time, but
finding the predecessor by index is linear. This is why middle indexed insertion
is not O(1) in this API.

## Explain heap extraction with an example

Take the valid heap array `[1, 3, 2, 7, 5, 4]`. Save the root, 1. Remove the last
position and put its value, 4, at the root: `[4, 3, 2, 7, 5]`.

The smaller root child is 2, so swap 4 with 2. The array becomes
`[2, 3, 4, 7, 5]`. The current position has no children, so the loop stops.
Return the saved minimum, 1. The array is a valid heap even though 7 appears
before 5: heap order is parent-child order, not complete sorted order.

The invariant says that only edges from the current replacement position may be
unordered. Child subtrees remain heaps, and earlier ancestors are no larger than
keys in the current subtree. Moving the smaller child upward fixes one level and
moves the possible problem downward. The remaining height decreases, so the loop
must terminate. See the full proof in README Section 3.2.

## Questions about complexity

**What do O, Omega, and Theta mean?** O is an upper bound, Omega is a lower bound,
and Theta is a matching pair of bounds. They are not synonyms for worst case,
best case, and average case. A best, average, or worst-case function can each
have its own O, Omega, and Theta bounds.

**Why is get constant time only in the array?** The array directly addresses the
requested slot. A singly linked list cannot jump to node i; it visits i+1 nodes.

**Is array append always constant time?** No. A full array must allocate a larger
array and copy existing values. That particular append is linear. Across many
appends, geometric doubling gives constant amortized cost per append.

**Is average case the same as amortized cost?** No. Average case needs a probability
model for the inputs. Amortized cost spreads the cost of an entire operation
sequence and need not assume random data.

**Is heap insertion O(log n)?** The sift-up part has that worst-case bound. This
implementation can also resize, so one growing call can be linear. Over a
sequence, growth is amortized. Under the random-permutation construction used
here, expected sift work per insertion is constant, but descending priorities can
force long sift-up paths. Do not confuse the workload expectation with a worst case.

**Why does search have the same count but different times?** Both structures scan
the same values and stop at the same point. Their memory organization and access
mechanisms differ. This experiment does not separately measure cache misses, so
cache explanations should be presented as plausible contributors, not proven causes.

## Questions about the benchmark

**What are n and m?** n is the initial list size, while m is the number of operations.
Random access uses m=10,000, search uses m=1,000, and each mutation experiment uses
m=1,000. The priority workload starts empty and has n insertions and n extractions.

**Why use the same seed?** It makes input and query generation reproducible.
Both list structures and all five repetitions receive the same data. The seed
does not make timing identical because the runtime environment still varies.

**Why shuffle distinct integers?** The random relative order is controlled and
well defined. Exactly half the search values can be sampled from the stored data,
while negative queries are definitely absent. Duplicate support is tested separately.

**What is timed?** Only the operation loops, including the counters and any growth
or node allocation caused by those operations. Input preparation, setup, printing,
restoration, and output validation are outside the measured intervals.

**Why five repeats?** The brief requires five. The project reports their arithmetic
mean and also preserves the raw observations, minimum, maximum, and sample standard
deviation. No slow result is discarded merely because it is inconvenient.

**Why do some constant-work timings decrease at larger n?** JIT compilation,
scheduling, and other uncontrolled runtime effects can matter more than the useful
work in short loops. The exact cause is not isolated here. Counts provide stronger
evidence of the algorithmic work than a single very short time.

**How can there be 1,000 removals when n is 100?** The literal instruction is
impossible. The documented extension restores the original structure outside the
timer whenever the fixed index becomes invalid. It sums batches until exactly
1,000 valid removals have happened. The middle index stays fixed at original n/2.
This interpretation must be disclosed and confirmed with the instructor.

**What are the main measured results?** Array access uses exactly 10,000 accesses
at every n. List access grows to 505,044,105 node visits at n=100,000. Search has
identical comparison counts for both structures. List front mutation needs only
1,000 node touches, while indexed middle mutations need repeated traversal.
Heap insertion comparisons stay near a constant per inserted key, while extraction
comparisons per key increase with heap height.

## A short demonstration sequence

Compile with the project run commands, then run:

```sh
java -cp out Demo
java -cp out Tests
```

Open the source operation you are explaining, connect each loop or pointer update
to the example, and then show the corresponding proof and result table. Use the
raw CSV and environment file when asked where the numbers came from. Describe
local preparation commits and your own later edits honestly; no student history
or hosted GitHub publication was invented by the package.
