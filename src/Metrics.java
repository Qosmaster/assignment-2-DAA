// Count accesses, value comparisons, and movements made by our code.
// long is used because an int could be too small for a large counter.
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
