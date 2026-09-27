import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

/** Plain Java tests: run with java -cp out Tests. No test library is needed. */
public class Tests {
    private static long checks;

    public static void main(String[] args) {
        testList(false);
        testList(true);
        testListMetrics();
        testHeap();
        System.out.println("PASS: " + checks + " checks; all test groups passed.");
    }

    private static IntList newList(boolean linked) {
        if (linked) {
            return new LinkedList();
        }
        return new DynamicArray();
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkListState(IntList actual, List<Integer> expected) {
        check(actual.size() == expected.size(), "List size differs");
        for (int i = 0; i < expected.size(); i++) {
            check(actual.get(i) == expected.get(i), "List value differs at " + i);
        }
        if (actual instanceof LinkedList) {
            check(((LinkedList) actual).isValidList(), "Invalid head, tail, or chain");
        }
    }

    private static void invalidIndex(IntList list, int operation, int index) {
        int oldSize = list.size();
        boolean thrown = false;
        try {
            if (operation == 0) {
                list.get(index);
            } else if (operation == 1) {
                list.remove(index);
            } else {
                list.add(index, 99);
            }
        } catch (IndexOutOfBoundsException exception) {
            thrown = true;
        }
        check(thrown, "An invalid index must throw an exception");
        check(list.size() == oldSize, "Invalid operation changed the size");
    }

    private static void testList(boolean linked) {
        String name = "DynamicArray";
        List<Integer> expected = new ArrayList<Integer>();
        if (linked) {
            name = "LinkedList";
            expected = new java.util.LinkedList<Integer>();
        }
        IntList actual = newList(linked);

        check(actual.size() == 0, "New list must be empty");
        check(!actual.contains(8), "Empty list contains a value");
        for (int operation = 0; operation < 3; operation++) {
            invalidIndex(actual, operation, -1);
        }
        invalidIndex(actual, 0, 0);
        invalidIndex(actual, 1, 0);
        invalidIndex(actual, 2, 1);

        actual.add(7);
        check(actual.get(0) == 7 && actual.contains(7), "One-element list failed");
        check(actual.remove(0) == 7 && actual.size() == 0, "Remove last element failed");
        // Appending after becoming empty checks that the tail was reset.
        actual.add(8);
        actual.remove(0);

        int[] values = {5, 5, -4, 12, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (int value : values) {
            actual.add(value);
            expected.add(value);
        }
        actual.add(0, 17);
        expected.add(0, 17);
        actual.add(actual.size(), 23);
        expected.add(expected.size(), 23);
        actual.add(3, 88);
        expected.add(3, 88);
        checkListState(actual, expected);
        check(actual.remove(actual.size() - 1) == expected.remove(expected.size() - 1),
                "Remove tail failed");
        check(actual.remove(0) == expected.remove(0), "Remove head failed");
        check(actual.remove(2) == expected.remove(2), "Remove middle failed");
        checkListState(actual, expected);
        invalidIndex(actual, 0, actual.size());
        invalidIndex(actual, 1, actual.size());
        invalidIndex(actual, 2, actual.size() + 1);

        Random random = new Random(42);
        for (int step = 0; step < 3000; step++) {
            int operation = random.nextInt(6);
            int value = random.nextInt(101) - 50;
            if (operation == 0 || expected.isEmpty()) {
                actual.add(value);
                expected.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(expected.size() + 1);
                actual.add(index, value);
                expected.add(index, value);
            } else if (operation == 2) {
                int index = random.nextInt(expected.size());
                check(actual.remove(index) == expected.remove(index), "Random remove differs");
            } else if (operation == 3) {
                int index = random.nextInt(expected.size());
                check(actual.get(index) == expected.get(index), "Random get differs");
            } else {
                check(actual.contains(value) == expected.contains(value), "Random search differs");
            }
            check(actual.size() == expected.size(), "Random operation changed size incorrectly");
            if (actual instanceof LinkedList) {
                check(((LinkedList) actual).isValidList(), "List invariant failed");
            }
            if (step % 50 == 0) {
                checkListState(actual, expected);
            }
        }
        checkListState(actual, expected);

        actual = newList(linked);
        for (int i = 0; i < 100000; i++) {
            actual.add(i);
        }
        check(actual.size() == 100000, "Large list size failed");
        check(actual.get(0) == 0 && actual.get(50000) == 50000
                && actual.get(99999) == 99999, "Large list access failed");
        check(actual.contains(99999) && !actual.contains(-1), "Large search failed");
        for (int i = 0; i < 1000; i++) {
            check(actual.remove(0) == i, "Large head removal failed");
        }
        actual.add(50000, -9);
        check(actual.get(50000) == -9 && actual.remove(50000) == -9,
                "Large middle mutation failed");
        System.out.println("PASS: " + name + " empty, single, duplicates, boundaries, invalid, random, large");
    }

    private static void testListMetrics() {
        for (int type = 0; type < 2; type++) {
            IntList list = newList(type == 1);
            int getAccesses = 1;
            int movements = 3;
            int changeAccesses = 7;
            if (type == 1) {
                getAccesses = 3;
                movements = 0;
                changeAccesses = 1;
            }
            list.add(10);
            list.add(20);
            list.add(30);
            list.metrics().reset();
            check(list.get(2) == 30, "Metric get value failed");
            check(list.metrics().accesses == getAccesses, "Get access count differs");
            list.metrics().reset();
            check(!list.contains(99) && list.metrics().comparisons == 3,
                    "Missing search count differs");
            list.metrics().reset();
            list.add(0, 5);
            check(list.metrics().movements == movements, "Insertion movements differ");
            check(list.metrics().accesses == changeAccesses, "Insertion accesses differ");
            list.metrics().reset();
            check(list.remove(0) == 5, "Metric removal failed");
            check(list.metrics().movements == movements, "Removal movements differ");
            check(list.metrics().accesses == changeAccesses, "Removal accesses differ");
        }
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 16; i++) {
            array.add(i);
        }
        array.metrics().reset();
        array.add(99);
        check(array.metrics().movements == 16 && array.metrics().accesses == 33,
                "Resize copies are not counted correctly");
        System.out.println("PASS: list metric counters, including array resizing");
    }

    private static void heapOperationMustFail(MinHeap heap, boolean extract) {
        boolean thrown = false;
        try {
            if (extract) {
                heap.extractMin();
            } else {
                heap.peekMin();
            }
        } catch (NoSuchElementException exception) {
            thrown = true;
        }
        check(thrown && heap.size() == 0, "Empty heap operation must throw");
    }

    private static void testHeap() {
        MinHeap heap = new MinHeap();
        check(heap.isValidHeap(), "Empty heap is invalid");
        heapOperationMustFail(heap, false);
        heapOperationMustFail(heap, true);
        heap.insert(7);
        check(heap.peekMin() == 7 && heap.extractMin() == 7 && heap.size() == 0,
                "One-element heap failed");
        int[] special = {7, 7, -2, 3, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        for (int value : special) {
            heap.insert(value);
            check(heap.isValidHeap(), "Heap property failed after insert");
        }
        Arrays.sort(special);
        for (int value : special) {
            check(heap.extractMin() == value, "Duplicate or extreme value failed");
            check(heap.isValidHeap(), "Heap property failed after extract");
        }
        heapOperationMustFail(heap, true);

        Random random = new Random(42);
        PriorityQueue<Integer> reference = new PriorityQueue<Integer>();
        for (int step = 0; step < 10000; step++) {
            if (reference.isEmpty() || random.nextInt(100) < 60) {
                int value = random.nextInt(1000) - 500;
                heap.insert(value);
                reference.add(value);
            } else {
                check(heap.extractMin() == reference.remove(), "PriorityQueue extraction differs");
            }
            check(heap.size() == reference.size(), "Heap size differs");
            check(heap.isValidHeap(), "Heap property failed after a random operation");
            if (!reference.isEmpty()) {
                check(heap.peekMin() == reference.peek(), "PriorityQueue minimum differs");
            }
        }
        while (!reference.isEmpty()) {
            check(heap.extractMin() == reference.remove(), "Final heap draining differs");
            check(heap.isValidHeap(), "Draining violated heap order");
        }
        // Ascending and descending inputs test immediate stops and long sift paths.
        for (int direction = 0; direction < 2; direction++) {
            for (int i = 0; i < 1024; i++) {
                int value = i;
                if (direction == 1) {
                    value = 1023 - i;
                }
                heap.insert(value);
                check(heap.isValidHeap(), "Ordered-input insertion failed");
            }
            for (int i = 0; i < 1024; i++) {
                check(heap.extractMin() == i, "Ordered-input extraction failed");
                check(heap.isValidHeap(), "Ordered-input extraction invariant failed");
            }
        }
        int[] large = new int[100000];
        for (int i = 0; i < large.length; i++) {
            large[i] = random.nextInt();
            heap.insert(large[i]);
        }
        check(heap.isValidHeap(), "Large heap is invalid");
        Arrays.sort(large);
        for (int value : large) {
            check(heap.extractMin() == value, "Large sorted extraction differs");
        }
        check(heap.size() == 0 && heap.isValidHeap(), "Large heap did not become empty");
        heap.insert(3);
        heap.insert(1);
        heap.insert(2);
        heap.metrics().reset();
        heap.peekMin();
        check(heap.metrics().comparisons == 0, "peekMin should not compare keys");
        check(heap.extractMin() == 1 && heap.metrics().comparisons == 1,
                "Heap extraction comparison count differs");
        System.out.println("PASS: MinHeap empty, single, duplicates, extremes, random, ordered, large, metrics");
    }
}
