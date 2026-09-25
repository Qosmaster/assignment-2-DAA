/** Logical operation counters. They are not hardware instruction counts. */
public class Metrics {
    public long accesses;
    public long comparisons;
    public long movements;

    public void reset() {
        accesses = 0;
        comparisons = 0;
        movements = 0;
    }
}
