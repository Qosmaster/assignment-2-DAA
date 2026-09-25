/** Small examples for explaining the structures; not used by the benchmark. */
public class Demo {
    private static void printList(IntList list) {
        System.out.print("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(list.get(i));
        }
        System.out.println("]");
    }

    private static void demonstrateList(IntList list, String name) {
        System.out.println(name);
        list.add(10);
        list.add(30);
        list.add(1, 20);
        System.out.print("After inserting 20 at index 1: ");
        printList(list);
        System.out.println("Removed first value: " + list.remove(0));
        System.out.print("Remaining values: ");
        printList(list);
        System.out.println("get(1): " + list.get(1));
        System.out.println("contains(20): " + list.contains(20));
        System.out.println();
    }

    public static void main(String[] args) {
        demonstrateList(new DynamicArray(), "Dynamic Array");
        demonstrateList(new LinkedList(), "Linked List");
        MinHeap heap = new MinHeap();
        int[] values = {5, 1, 4, 2, 2};
        for (int value : values) {
            heap.insert(value);
        }
        System.out.println("Min-Heap");
        System.out.println("Minimum before extraction: " + heap.peekMin());
        System.out.print("Priority order: ");
        while (heap.size() > 0) {
            System.out.print(heap.extractMin() + " ");
        }
        System.out.println();
    }
}
