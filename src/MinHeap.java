import java.util.NoSuchElementException;

/** A binary min-heap stored in a growing integer array. */
public class MinHeap {
    private int[] data = new int[16];
    private int size;
    private final Metrics metrics = new Metrics();

    public int size() {
        return size;
    }

    public Metrics metrics() {
        return metrics;
    }

    public void insert(int value) {
        ensureCapacity();
        int index = size;
        data[index] = value;
        size++;
        // Only the path from the new leaf to the root can violate heap order.
        while (index > 0) {
            int parent = (index - 1) / 2;
            metrics.comparisons++;
            if (data[parent] <= data[index]) {
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }

    public int peekMin() {
        checkNotEmpty();
        return data[0];
    }

    public int extractMin() {
        checkNotEmpty();
        int minimum = data[0];
        size--;
        if (size == 0) {
            return minimum;
        }
        data[0] = data[size];
        metrics.movements++;
        int index = 0;
        // Move the replacement down until it is no larger than either child.
        while (true) {
            int left = 2 * index + 1;
            if (left >= size) {
                break;
            }
            int right = left + 1;
            int smaller = left;
            if (right < size) {
                metrics.comparisons++;
                if (data[right] < data[left]) {
                    smaller = right;
                }
            }
            metrics.comparisons++;
            if (data[index] <= data[smaller]) {
                break;
            }
            swap(index, smaller);
            index = smaller;
        }
        return minimum;
    }

    private void swap(int first, int second) {
        int temporary = data[first];
        data[first] = data[second];
        data[second] = temporary;
        metrics.movements += 2;
    }

    private void ensureCapacity() {
        if (size < data.length) {
            return;
        }
        int[] larger = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            larger[i] = data[i];
            metrics.movements++;
        }
        data = larger;
    }

    private void checkNotEmpty() {
        if (size == 0) {
            throw new NoSuchElementException("The heap is empty");
        }
    }

    /** Validation does not change the comparison counter. */
    public boolean isValidHeap() {
        for (int child = 1; child < size; child++) {
            int parent = (child - 1) / 2;
            if (data[parent] > data[child]) {
                return false;
            }
        }
        return true;
    }
}
