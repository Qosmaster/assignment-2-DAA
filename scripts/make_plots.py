"""Create the report plots from measured CSV data. Requires matplotlib only."""
from pathlib import Path
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "results/plots"
OUTPUT.mkdir(parents=True, exist_ok=True)
with (ROOT / "results/tables/summary.csv").open() as source:
    ROWS = list(csv.DictReader(source))


def plot_series(filename, title, ylabel, specifications, error_bars=False):
    """Each output file has exactly one chart, with automatic default colors."""
    fig, ax = plt.subplots(figsize=(8.4, 3.8))
    for workload, structure, metric, label in specifications:
        selected = sorted((r for r in ROWS if r["workload"] == workload
                           and r["structure"] == structure), key=lambda r: int(r["n"]))
        x = [int(r["n"]) for r in selected]
        y = [float(r[metric]) for r in selected]
        if error_bars:
            lower = [float(r[metric]) - float(r["min_ms"]) for r in selected]
            upper = [float(r["max_ms"]) - float(r[metric]) for r in selected]
            ax.errorbar(x, y, yerr=[lower, upper], marker="o", capsize=3, label=label)
        else:
            ax.plot(x, y, marker="o", label=label)
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("Initial input size, n (log scale)")
    ax.set_ylabel(ylabel + " (log scale)")
    ax.set_title(title, pad=12)
    ax.grid(True, which="major", alpha=0.25)
    ax.legend(loc="best", fontsize=9)
    note = "Mean of 5 runs; error bars show min-max." if error_bars else "Counts are identical across all 5 seeded repetitions."
    fig.text(0.5, 0.01, note, ha="center", fontsize=9)
    fig.tight_layout(rect=(0, 0.04, 1, 1))
    fig.savefig(OUTPUT / filename, dpi=180)
    plt.close(fig)


plot_series("01_access_time.png", "Random access: 10,000 get(index) operations", "Total time (ms)",
            [("W1_access", s, "mean_ms", s) for s in ("DynamicArray", "LinkedList")], True)
plot_series("02_access_counts.png", "Random access: logical element accesses", "Access count",
            [("W1_access", s, "mean_accesses", s) for s in ("DynamicArray", "LinkedList")])
plot_series("03_search_time.png", "Search: 1,000 queries, 50% successful", "Total time (ms)",
            [("W2_search", s, "mean_ms", s) for s in ("DynamicArray", "LinkedList")], True)
plot_series("04_search_comparisons.png", "Search: identical key comparisons in both structures", "Comparison count",
            [("W2_search", "DynamicArray", "mean_comparisons", "Both structures")])
for verb in ("insert", "remove"):
    series = [(f"W3_{verb}_{position}", structure, "mean_ms", f"{structure}: {position}")
              for structure in ("DynamicArray", "LinkedList") for position in ("front", "middle")]
    number = "05" if verb == "insert" else "06"
    title = "Insertion" if verb == "insert" else "Removal (restoration excluded)"
    plot_series(f"{number}_{verb}_time.png", title + ": 1,000 operations at a fixed index", "Total time (ms)", series, True)
plot_series("07_heap_time.png", "Priority processing: n insertions and n extractions", "Total time (ms)",
            [("W4_" + operation, "MinHeap", "mean_ms", label)
             for operation, label in (("insert", "Insert n values"), ("extract", "Extract n minima"))], True)
plot_series("08_heap_comparisons.png", "Priority processing: key comparison counts", "Comparison count",
            [("W4_" + operation, "MinHeap", "mean_comparisons", label)
             for operation, label in (("insert", "Insert n values"), ("extract", "Extract n minima"))])
print("Created 8 plots in results/plots/ from the measured results.")
