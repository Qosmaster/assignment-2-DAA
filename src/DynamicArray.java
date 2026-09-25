/** An integer array that doubles its capacity when it becomes full. */
public class DynamicArray implements IntList {
    private int[] data = new int[16];
    private int size;
    private final Metrics metrics = new Metrics();

    public int size() {
        return size;
    }

    public Metrics metrics() {
        return metrics;
    }

    public void add(int value) {
        add(size, value);
    }

    public void add(int index, int value) {
        checkPosition(index);
        ensureCapacity();
        // Copy backwards so an unread element is never overwritten.
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            metrics.accesses += 2;
            metrics.movements++;
        }
        data[index] = value;
        metrics.accesses++;
        size++;
    }

    public int remove(int index) {
        checkIndex(index);
        int removed = data[index];
        metrics.accesses++;
        // Close the gap by shifting the suffix one position to the left.
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            metrics.accesses += 2;
            metrics.movements++;
        }
        size--;
        return removed;
    }

    public int get(int index) {
        checkIndex(index);
        metrics.accesses++;
        return data[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            metrics.accesses++;
            metrics.comparisons++;
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    private void ensureCapacity() {
        if (size < data.length) {
            return;
        }
        int[] larger = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            larger[i] = data[i];
            metrics.accesses += 2;
            metrics.movements++;
        }
        data = larger;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkPosition(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}
