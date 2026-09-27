# Explaining the project

## A short introduction

"This project compares three structures: a Dynamic Array, a Linked List, and a
Min-Heap. The same operations can have different costs because the values are
stored differently. The project uses Java loops and classes, tests the answers,
and records five timings for each experiment."

## 1. Dynamic Array

`data` stores the values. `size` is the number of values currently in use.
`data.length` is the capacity. For example, size can be 3 while capacity is 16.

`get(index)` checks the index and returns `data[index]` directly.

`add(value)` calls `add(size, value)`, which means insertion at the end.

`add(index, value)` checks the index, makes more space if needed, moves the later
values one position right, and places the new value at the chosen index.
The loop starts at the end. Starting from the front would overwrite values
before copying them.

Example: `[10, 30]`, followed by `add(1, 20)`, becomes `[10, 20, 30]`.

`remove(index)` saves the removed value, moves later values one place left,
decreases size, and returns the saved value.

`contains(value)` checks each value until it finds a match or reaches size.

`ensureCapacity()` creates an array twice as long and copies the old values.
This makes a single full-array append slow, but many appends are efficient overall.

## 2. Linked List

A `Node` holds an integer and `next`, a link to another node.
`head` is the first node; `tail` is the last. The last node has `next == null`.

`add(value)` makes a node and attaches it after tail. It does not scan the list.

`add(0, value)` puts the new node before head. For a middle index, the code first
finds the previous node and then changes the two links.

`remove(0)` moves head to `head.next`. Other removals first find the previous
node and make it skip the removed node. Removing the final node resets tail.

`nodeAt(index)` starts at head and follows next links until it reaches the index.
`get(index)` returns that node's value. The list cannot jump directly to an index.

`contains(value)` checks each node's value until a match or null.

## 3. Min-Heap

The heap is stored in an array. Every parent value is <= its children.
This is enough to keep the minimum at position 0; the whole array is not sorted.

For a node at index i:

```
parent = (i - 1) / 2
left child = 2 * i + 1
right child = 2 * i + 2
```

`insert(value)` puts the value at the end. While it is smaller than its parent,
the code swaps it upward. This is often called sift-up.

`peekMin()` returns the root without removing it.

`extractMin()` saves the root, moves the last value to the root, and moves this
replacement down. It swaps with the smaller child. It stops when the value is
<= its children or has no children. This is called sift-down.

The tests check that the parent-child rule still holds after operations and that
repeated extraction gives values in non-decreasing order.

## 4. Basic Java used here

`class` groups data and methods. `new` creates an object.
`private` keeps a field or helper method inside its class. `public` makes it
available from other classes. `static` means a method or field belongs to the
class rather than one individual object. `final` means a variable cannot be
assigned a new value after initialization.

`IntList` is an interface: a shared set of methods for the two list classes.
`implements IntList` says the class provides those methods. It avoids copying
all benchmark code for each structure.

`for` repeats a known set of steps. `while` repeats while a condition is true.
`if/else` chooses a path. `return` sends a value back and ends the method.
`break` exits a loop. `null` means there is no object at that link.

`for (int value : values)` means "do this once for each value in the array."

`List<Integer>` in the tests is a standard Java collection of integer values.
It provides reference answers; it is not used inside the custom list structures.
`instanceof` checks an object's class. The cast in the tests lets the test call
the linked-list-only validation method.

`throw` reports an error. `try/catch` lets tests check that an invalid operation
really reports one. `try (...)` in file writing closes the file automatically.

`Metrics` uses `long` because large counts can exceed an int's range.
`volatile sink` keeps a result after timing to discourage Java from discarding
unused work. You do not need threads to run this project.

## 5. Benchmark.java, step by step

`Inputs` holds the prepared values, random indices, queries, and new values.
`Result` holds one experiment's time and counts. They are small data holders.

The main method first warms up Java twice. It then loops through the four sizes
and five repetitions. Each round runs the required workloads for both list types
and the heap. The code saves all results after timing is finished.

Each list experiment prepares the structure and resets its counters. Then it
records a start time, runs the operation loop, and records elapsed time.
Correctness checks and CSV writing happen after that measured section.

`saveResults` writes all 280 runs to raw.csv. It groups matching experiments and
computes their average. Minimum, maximum, and standard deviation are extra
information about timing variation. The rubric needs the mean, not identical
times across runs.

In search, exactly half the queries match. This makes the experiment easy to
compare as n changes. Both structures receive the same data and queries.

In the heap workload, the output array is allocated before timing. The extraction
loop writes to it inside timing. Sorting the reference copy and checking the
answer happen after timing.

## 6. The removal problem

The brief asks for 1,000 removals from only 100 original values.
That cannot work in one batch. With a fixed middle index, the index can become
invalid even before the structure is empty.

The code removes all valid values at the fixed index, restores the original
structure outside timing, and continues. It still performs 1,000 successful
removals. It never silently changes the index to `size / 2`.
This interpretation must be approved by the instructor.

## 7. The two loop proofs

For array insertion: the processed right part has moved right once; the unread
left part is unchanged. Copying from right to left keeps this true. At the end,
the gap is at the insertion index, so the new value can be written there.

For list search: every node before current has been checked and did not match.
Each unsuccessful step adds one more non-match. A match proves true; reaching
null proves false. The report gives initialization, maintenance, termination,
and the final correctness argument for both proofs.

## 8. Questions to practice

**Why is array get constant time?**
It reads a position directly; it does not scan earlier values.

**Why can list insertion be linear?**
The link change is cheap, but an indexed insertion may need a long walk first.

**What does amortized mean?**
Cost per operation across a whole sequence. A rare expensive array copy is shared
over many cheap appends. It does not mean every single append is constant time.

**Is heap insert always logarithmic?**
The upward loop has a logarithmic worst case, but array growth can make a single
call linear. Random-order construction has a smaller expected sequence cost.
The report separates these cases.

**Why are the two searches not equally fast?**
They can do the same number of comparisons with different memory-access costs.
The linked list follows links; the array reads positions next to one another.

**What are n and m?**
n is the initial list size. m is the number of operations. The heap workload
starts empty and uses n insertions followed by n extractions.

**Why not time data generation?**
It would add work that is not part of the requested structure operation.

**Do the timings prove the complexity?**
No. The loop analysis gives the bounds. The counts and timings provide evidence
for these inputs and show practical differences.

**Which structure should be chosen?**
It depends on the main operations: indexed reads favor the array, head changes
favor the list, and next-minimum processing favors the heap.

**Were these measurements made on the student's computer?**
No. The supplied run was made in the shared execution container described in
results/environment.txt. Local runs will give different times.
