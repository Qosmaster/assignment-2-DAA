"""Independently check the CSV aggregation and exact list-operation formulas."""
from pathlib import Path
import csv
import math
import statistics

ROOT = Path(__file__).resolve().parents[1]
raw = list(csv.DictReader((ROOT / "results/tables/raw.csv").open()))
summary = list(csv.DictReader((ROOT / "results/tables/summary.csv").open()))
assert len(raw) == 280 and len(summary) == 56
checks = 0
for row in summary:
    group = [r for r in raw if all(r[k] == row[k] for k in ("workload", "structure", "n"))]
    assert sorted(int(r["repeat"]) for r in group) == [1, 2, 3, 4, 5]
    times = [int(r["total_ns"]) for r in group]
    assert all(t > 0 for t in times)
    assert math.isclose(statistics.mean(times), float(row["mean_ns"]), abs_tol=0.051)
    for key, expected in (("mean_ms", statistics.mean(times) / 1e6),
                          ("min_ms", min(times) / 1e6),
                          ("max_ms", max(times) / 1e6),
                          ("sd_ms", statistics.stdev(times) / 1e6)):
        assert math.isclose(float(row[key]), expected, abs_tol=0.00000051)
    for metric in ("accesses", "comparisons", "movements"):
        assert len({r[metric] for r in group}) == 1
        assert int(row["mean_" + metric]) == int(group[0][metric])
    n, m = int(row["n"]), int(row["m"])
    workload, structure = row["workload"], row["structure"]
    accesses, moves = int(row["mean_accesses"]), int(row["mean_movements"])
    if workload == "W1_access" and structure == "DynamicArray":
        assert accesses == m == 10000
    if workload == "W2_search":
        paired = [r for r in summary if r["workload"] == workload and r["n"] == row["n"]]
        assert len(paired) == 2
        assert paired[0]["mean_comparisons"] == paired[1]["mean_comparisons"]
        assert all(int(r["checksum"]) == 500 for r in group)
    if workload.startswith("W3_"):
        index = n // 2 if workload.endswith("middle") else 0
        if "insert" in workload:
            capacity = 16
            while capacity < n:
                capacity *= 2
            copies = 0
            while capacity < n + m:
                copies += capacity
                capacity *= 2
            expected_moves = m * (n - index) + m * (m - 1) // 2 + copies
            expected_batches = 1
        else:
            available = n - index
            full, remainder = divmod(m, available)
            expected_moves = (full * available * (available - 1) // 2
                              + remainder * available - remainder * (remainder + 1) // 2)
            expected_batches = math.ceil(m / available)
        assert int(row["batches"]) == expected_batches
        if structure == "DynamicArray":
            assert moves == expected_moves
            assert accesses == 2 * moves + m
        else:
            assert moves == 0 and accesses == m * (index + 1)
    checks += 1
text = ("PASS: 280 raw rows, 56 experiment groups, exactly five repeats per group.\n"
        "PASS: mean, minimum, maximum, sample standard deviation, and all deterministic metrics.\n"
        "PASS: identical search comparisons, 50% search hits, and all exact W3 metric formulas.\n"
        "PASS: removal batch counts and restoration protocol for every n.\n"
        f"PASS: {checks} summary rows independently verified.\n")
(ROOT / "results/verification.txt").write_text(text, encoding="utf-8")
print(text, end="")
