// Both list classes provide these methods.
// This lets the same test and benchmark code work with either list.
public interface IntList {
    void add(int value);
    void add(int index, int value);
    int remove(int index);
    int get(int index);
    boolean contains(int value);
    int size();
    Metrics metrics();
}
