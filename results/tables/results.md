# Measured results

## 5. Results

The files contain 280 measured runs and 56 averages. Each table row is the total time for one workload, averaged over five runs. All values below come from the current CSV files [3]. Array and List mean the custom Java classes.

### 5.1 Workload 1: random access

| n | Type | Mean ms | Accesses | Total time bound |
| --- | --- | --- | --- | --- |
| 100 | Array | 0.095906 | 10,000 | Θ(m) |
| 100 | List | 0.831234 | 511,261 | Θ(mn), expected |
| 1,000 | Array | 0.055277 | 10,000 | Θ(m) |
| 1,000 | List | 10.593815 | 5,021,758 | Θ(mn), expected |
| 10,000 | Array | 0.091211 | 10,000 | Θ(m) |
| 10,000 | List | 113.106179 | 50,177,030 | Θ(mn), expected |
| 100,000 | Array | 0.053788 | 10,000 | Θ(m) |
| 100,000 | List | 1137.134463 | 505,044,105 | Θ(mn), expected |

At n = 100,000, the array takes 0.053788 ms and the list takes 1137.134463 ms. The array always makes 10,000 slot accesses. The list makes 505,044,105 node visits at the largest size.

The array jumps directly to an index. The list starts from head each time. A random list index needs about (n + 1) / 2 node visits on average. The counts therefore agree with Θ(1) array access and expected Θ(n) list access. Small timing changes do not change these bounds.

## 5.2 Workload 2: search

m = 1,000. Half of the search values are present and half are absent. Both structures hold values in the same order and receive the same queries.

| n | Type | Mean ms | Comparisons | Total time bound |
| --- | --- | --- | --- | --- |
| 100 | Array | 0.117265 | 75,912 | Θ(mn), expected |
| 100 | List | 0.238080 | 75,912 | Θ(mn), expected |
| 1,000 | Array | 0.613368 | 740,571 | Θ(mn), expected |
| 1,000 | List | 1.927573 | 740,571 | Θ(mn), expected |
| 10,000 | Array | 7.706681 | 7,478,562 | Θ(mn), expected |
| 10,000 | List | 19.888828 | 7,478,562 | Θ(mn), expected |
| 100,000 | Array | 48.324978 | 74,864,907 | Θ(mn), expected |
| 100,000 | List | 205.394460 | 74,864,907 | Θ(mn), expected |

At n = 100,000, both structures make 74,864,907 comparisons. The array takes 48.324978 ms; the list takes 205.394460 ms.

A successful search uses about (n + 1) / 2 comparisons on average. A failed search uses n. This explains the near-linear growth in the count when n increases. Equal comparison counts do not mean equal time: following next links and reading adjacent array positions have different costs.

## 5.3 Workload 3A: insertion

m = 1,000. Front means index 0. Middle means the fixed original n / 2. The metric is moved values for the array and handled nodes for the list. Array growth copies are included.

| n | Position | Type | Mean ms | Metric | Total bound |
| --- | --- | --- | --- | --- | --- |
| 100 | Front | Array | 0.481488 | 601,420 | Θ(mn + m²) |
| 100 | Front | List | 0.194483 | 1,000 | Θ(m) |
| 100 | Middle | Array | 0.356207 | 551,420 | Θ(mn + m²) |
| 100 | Middle | List | 0.208040 | 51,000 | Θ(mn) |
| 1,000 | Front | Array | 0.688234 | 1,500,524 | Θ(mn + m²) |
| 1,000 | Front | List | 0.155495 | 1,000 | Θ(m) |
| 1,000 | Middle | Array | 0.456444 | 1,000,524 | Θ(mn + m²) |
| 1,000 | Middle | List | 1.356892 | 501,000 | Θ(mn) |
| 10,000 | Front | Array | 5.699285 | 10,499,500 | Θ(mn + m²) |
| 10,000 | Front | List | 0.152341 | 1,000 | Θ(m) |
| 10,000 | Middle | Array | 2.920180 | 5,499,500 | Θ(mn + m²) |
| 10,000 | Middle | List | 10.732517 | 5,001,000 | Θ(mn) |
| 100,000 | Front | Array | 37.120313 | 100,499,500 | Θ(mn + m²) |
| 100,000 | Front | List | 0.062805 | 1,000 | Θ(m) |
| 100,000 | Middle | Array | 19.517181 | 50,499,500 | Θ(mn + m²) |
| 100,000 | Middle | List | 107.672069 | 50,001,000 | Θ(mn) |

The array shifts values to make space. Head insertion in the list only changes links, so it is cheaper in number of steps. Middle insertion in the list still needs a walk from head. The array grows during the 1,000 insertions, which is why its full bound includes m².

## 5.4 Workload 3B: removal

Each row has 1,000 successful removals. Small structures are restored outside timing as explained in Section 4.2. The counter units are the same as in the insertion table.

| n | Position | Type | Mean ms | Metric | Batches | Total bound |
| --- | --- | --- | --- | --- | --- | --- |
| 100 | Front | Array | 0.173249 | 49,500 | 10 | Θ(mn) |
| 100 | Front | List | 0.047745 | 1,000 | 10 | Θ(m) |
| 100 | Middle | Array | 0.081601 | 24,500 | 20 | Θ(mn) |
| 100 | Middle | List | 0.127822 | 51,000 | 20 | Θ(mn) |
| 1,000 | Front | Array | 0.254867 | 499,500 | 1 | Θ(mn) |
| 1,000 | Front | List | 0.023365 | 1,000 | 1 | Θ(m) |
| 1,000 | Middle | Array | 0.132875 | 249,500 | 2 | Θ(mn) |
| 1,000 | Middle | List | 1.107617 | 501,000 | 2 | Θ(mn) |
| 10,000 | Front | Array | 5.009604 | 9,499,500 | 1 | Θ(mn) |
| 10,000 | Front | List | 0.035335 | 1,000 | 1 | Θ(m) |
| 10,000 | Middle | Array | 2.532929 | 4,499,500 | 1 | Θ(mn) |
| 10,000 | Middle | List | 11.012722 | 5,001,000 | 1 | Θ(mn) |
| 100,000 | Front | Array | 46.483504 | 99,499,500 | 1 | Θ(mn) |
| 100,000 | Front | List | 0.007056 | 1,000 | 1 | Θ(m) |
| 100,000 | Middle | Array | 23.745779 | 49,499,500 | 1 | Θ(mn) |
| 100,000 | Middle | List | 107.667496 | 50,001,000 | 1 | Θ(mn) |

Front removal shifts almost all remaining array values. The list can just move head to the next node. In the middle, the array shifts fewer values, but the list still needs to find the previous node. The array bound shown here uses the documented batch rule and the tested sizes.

## 5.5 Workload 4: priority processing

The heap starts empty. It receives n values, then returns the minimum n times. These two parts have separate timers and comparison counters. The returned sequence is compared with a sorted copy of the input outside the timer.

| n | Phase | Mean ms | Comparisons | Total time bound |
| --- | --- | --- | --- | --- |
| 100 | Extract | 0.014232 | 855 | Θ(n log n), expected |
| 100 | Insert | 0.020782 | 220 | Θ(n), expected |
| 1,000 | Extract | 0.161368 | 15,018 | Θ(n log n), expected |
| 1,000 | Insert | 0.625535 | 2,281 | Θ(n), expected |
| 10,000 | Extract | 1.340480 | 216,624 | Θ(n log n), expected |
| 10,000 | Insert | 0.807918 | 22,918 | Θ(n), expected |
| 100,000 | Extract | 14.342128 | 2,831,730 | Θ(n log n), expected |
| 100,000 | Insert | 5.236890 | 227,941 | Θ(n), expected |

At n = 100,000, insertion makes 227,941 comparisons and extraction makes 2,831,730. The mean times are 5.236890 ms and 14.342128 ms.

Random-order insertion often stops after only a few upward comparisons. Repeated extraction usually moves values farther down. This agrees with the random-order averages in Section 2.4. Both phases have O(n log n) worst-case total bounds, including array growth. All extracted sequences passed the non-decreasing-order check.

peekMin is tested and analyzed, but it is not separately timed because this workload asks for insertion and extraction times only.

## 5.6 Operation-count graphs

These graphs show work counts rather than speed. Logarithmic axes keep small and large values readable. The exact numbers are in the tables and CSV files.

Counts are identical across the five runs because the input is fixed. verify_results.py checks averages, repeat counts, search hits, and exact insertion/removal counts. These checks passed for all 56 experiments [3].

