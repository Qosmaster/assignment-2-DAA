import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Fixed, reproducible workloads. Run this class from the project root. */
public class Benchmark {
    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final int REPETITIONS = 5;
    private static final int ACCESS_OPERATIONS = 10000;
    private static final int SEARCH_OPERATIONS = 1000;
    private static final int CHANGE_OPERATIONS = 1000;
    private static final int SEED = 42;
    // Keep each answer after timing so Java cannot ignore the result.
    private static volatile long sink;

    /** All input is generated before any measured workload starts. */
    // This class keeps the prepared numbers for one input size.
    private static class Inputs {
        int[] data;
        int[] indices = new int[ACCESS_OPERATIONS];
        int[] queries = new int[SEARCH_OPERATIONS];
        int[] additions = new int[CHANGE_OPERATIONS];

        Inputs(int n) {
            Random random = new Random(SEED);
            data = new int[n];
            for (int i = 0; i < n; i++) {
                data[i] = i;
            }
            // Fisher-Yates produces a random order of n distinct integers.
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temporary = data[i];
                data[i] = data[j];
                data[j] = temporary;
            }
            for (int i = 0; i < indices.length; i++) {
                indices[i] = random.nextInt(n);
            }
            // Exactly half the searches succeed; negative values are absent.
            for (int i = 0; i < queries.length; i++) {
                if (i % 2 == 0) {
                    queries[i] = data[random.nextInt(n)];
                } else {
                    queries[i] = -1 - random.nextInt(n);
                }
            }
            for (int i = 0; i < additions.length; i++) {
                additions[i] = random.nextInt();
            }
        }
    }

    // This class stores the time and counts from one experiment.
    private static class Result {
        String workload;
        String structure;
        String theory;
        int n;
        int m;
        int repeat;
        int batches = 1;
        long nanoseconds;
        long accesses;
        long comparisons;
        long movements;
        long checksum;

        Result(String workload, String structure, int n, int m, int repeat,
                String theory) {
            this.workload = workload;
            this.structure = structure;
            this.n = n;
            this.m = m;
            this.repeat = repeat;
            this.theory = theory;
        }

        void addMetrics(Metrics metrics) {
            accesses += metrics.accesses;
            comparisons += metrics.comparisons;
            movements += metrics.movements;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String name(boolean linked) {
        if (linked) {
            return "LinkedList";
        }
        return "DynamicArray";
    }

    private static IntList preparedList(boolean linked, int[] values) {
        IntList list;
        if (linked) {
            list = new LinkedList();
        } else {
            list = new DynamicArray();
        }
        for (int value : values) {
            list.add(value);
        }
        list.metrics().reset();
        return list;
    }

    private static Result randomAccess(boolean linked, Inputs inputs, int repeat) {
        IntList list = preparedList(linked, inputs.data);
        long expected = 0;
        for (int index : inputs.indices) {
            expected += inputs.data[index];
        }
        long sum = 0;
        long start = System.nanoTime();
        for (int index : inputs.indices) {
            sum += list.get(index);
        }
        long elapsed = System.nanoTime() - start;
        String theory = "Theta(m)";
        if (linked) {
            theory = "Theta(m*n) expected";
        }
        Result result = new Result("W1_access", name(linked), inputs.data.length,
                ACCESS_OPERATIONS, repeat, theory);
        result.nanoseconds = elapsed;
        result.addMetrics(list.metrics());
        result.checksum = sum;
        sink = sum;
        require(sum == expected, "Random access checksum failed");
        return result;
    }

    private static Result search(boolean linked, Inputs inputs, int repeat) {
        IntList list = preparedList(linked, inputs.data);
        int found = 0;
        long start = System.nanoTime();
        for (int value : inputs.queries) {
            if (list.contains(value)) {
                found++;
            }
        }
        long elapsed = System.nanoTime() - start;
        Result result = new Result("W2_search", name(linked), inputs.data.length,
                SEARCH_OPERATIONS, repeat, "Theta(m*n) expected");
        result.nanoseconds = elapsed;
        result.addMetrics(list.metrics());
        result.checksum = found;
        sink = found;
        require(found == SEARCH_OPERATIONS / 2, "Search hit ratio failed");
        return result;
    }

    private static Result insert(boolean linked, Inputs inputs, int repeat, boolean middle) {
        int n = inputs.data.length;
        int index = 0;
        if (middle) {
            index = n / 2;
        }
        IntList list = preparedList(linked, inputs.data);
        long start = System.nanoTime();
        for (int value : inputs.additions) {
            list.add(index, value);
        }
        long elapsed = System.nanoTime() - start;
        String theory = "Theta(m*n+m^2)";
        if (linked) {
            theory = "Theta(m)";
            if (middle) {
                theory = "Theta(m*n)";
            }
        }
        String workload = "W3_insert_front";
        if (middle) {
            workload = "W3_insert_middle";
        }
        Result result = new Result(workload, name(linked), n,
                CHANGE_OPERATIONS, repeat, theory);
        result.nanoseconds = elapsed;
        result.addMetrics(list.metrics());
        // Verification is outside timing and after the metric snapshot.
        require(list.size() == n + CHANGE_OPERATIONS, "Insertion size failed");
        require(list.get(index) == inputs.additions[CHANGE_OPERATIONS - 1],
                "Inserted value is not at the requested position");
        result.checksum = (long) list.get(index) + list.size();
        sink = result.checksum;
        return result;
    }

    private static Result remove(boolean linked, Inputs inputs, int repeat, boolean middle) {
        int n = inputs.data.length;
        int index = 0;
        if (middle) {
            index = n / 2;
        } // Keep this index fixed even when the list size changes.
        String theory = "Theta(m*n) under the batch protocol";
        if (linked) {
            theory = "Theta(m)";
            if (middle) {
                theory = "Theta(m*n)";
            }
        }
        String workload = "W3_remove_front";
        if (middle) {
            workload = "W3_remove_middle";
        }
        Result result = new Result(workload, name(linked), n,
                CHANGE_OPERATIONS, repeat, theory);
        result.batches = 0;
        int completed = 0;
        while (completed < CHANGE_OPERATIONS) {
            // A new original structure is required when the fixed index becomes invalid.
            // All restoration work is explicitly outside the timed section.
            IntList list = preparedList(linked, inputs.data);
            int count = Math.min(CHANGE_OPERATIONS - completed, n - index);
            long expected = 0;
            for (int i = 0; i < count; i++) {
                expected += inputs.data[index + i];
            }
            long sum = 0;
            long start = System.nanoTime();
            for (int i = 0; i < count; i++) {
                sum += list.remove(index);
            }
            long elapsed = System.nanoTime() - start;
            result.nanoseconds += elapsed;
            result.addMetrics(list.metrics());
            result.checksum += sum;
            result.batches++;
            completed += count;
            sink = sum;
            require(sum == expected && list.size() == n - count, "Removal batch failed");
        }
        return result;
    }

    private static Result[] priority(Inputs inputs, int repeat) {
        int n = inputs.data.length;
        MinHeap heap = new MinHeap();
        int[] extracted = new int[n]; // Output allocation is excluded from timing.
        long start = System.nanoTime();
        for (int value : inputs.data) {
            heap.insert(value);
        }
        long elapsed = System.nanoTime() - start;
        Result insertion = new Result("W4_insert", "MinHeap", n, n, repeat,
                "Theta(n) expected; O(n*log(n)) worst");
        insertion.nanoseconds = elapsed;
        insertion.addMetrics(heap.metrics());
        insertion.checksum = (long) heap.peekMin() + heap.size();
        sink = insertion.checksum;
        require(heap.size() == n && heap.isValidHeap(), "Heap insertion validation failed");

        heap.metrics().reset();
        start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            extracted[i] = heap.extractMin();
        }
        elapsed = System.nanoTime() - start;
        Result extraction = new Result("W4_extract", "MinHeap", n, n, repeat,
                "Theta(n*log(n)) expected; O(n*log(n)) worst");
        extraction.nanoseconds = elapsed;
        extraction.addMetrics(heap.metrics());
        int[] expected = inputs.data.clone();
        Arrays.sort(expected);
        for (int i = 0; i < n; i++) {
            require(extracted[i] == expected[i], "Heap extraction differs from sorted input");
            require(i == 0 || extracted[i - 1] <= extracted[i], "Output is not non-decreasing");
            extraction.checksum += extracted[i];
        }
        require(heap.size() == 0 && heap.isValidHeap(), "Heap did not become empty");
        sink = extraction.checksum;
        return new Result[] {insertion, extraction};
    }

    private static void runRound(Inputs inputs, int repeat, List<Result> results) {
        // Alternate list order between repetitions to reduce systematic order bias.
        for (int type = 0; type < 2; type++) {
            boolean linked = (type + repeat) % 2 == 0;
            results.add(randomAccess(linked, inputs, repeat));
            results.add(search(linked, inputs, repeat));
            results.add(insert(linked, inputs, repeat, false));
            results.add(remove(linked, inputs, repeat, false));
            results.add(insert(linked, inputs, repeat, true));
            results.add(remove(linked, inputs, repeat, true));
        }
        Result[] heapResults = priority(inputs, repeat);
        results.add(heapResults[0]);
        results.add(heapResults[1]);
    }

    private static boolean sameExperiment(Result first, Result second) {
        return first.n == second.n && first.workload.equals(second.workload)
                && first.structure.equals(second.structure);
    }

    // Save every run, then find the average of each group of five runs.
    // try (...) closes the output file automatically, even if writing fails.
    private static void saveResults(List<Result> results) throws IOException {
        Path directory = Paths.get("results", "tables");
        Files.createDirectories(directory);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(directory.resolve("raw.csv")))) {
            out.println("workload,structure,n,m,repeat,total_ns,accesses,comparisons,movements,batches,checksum,theory");
            for (Result r : results) {
                out.printf(Locale.US, "%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s%n",
                        r.workload, r.structure, r.n, r.m, r.repeat, r.nanoseconds,
                        r.accesses, r.comparisons, r.movements, r.batches, r.checksum, r.theory);
            }
        }
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(directory.resolve("summary.csv")))) {
            out.println("workload,structure,n,m,runs,mean_ns,mean_ms,min_ms,max_ms,sd_ms,mean_accesses,mean_comparisons,mean_movements,batches,theory");
            for (Result first : results) {
                if (first.repeat != 1) {
                    continue;
                }
                int count = 0;
                double total = 0;
                long minimum = Long.MAX_VALUE;
                long maximum = Long.MIN_VALUE;
                for (Result r : results) {
                    if (sameExperiment(first, r)) {
                        count++;
                        total += r.nanoseconds;
                        minimum = Math.min(minimum, r.nanoseconds);
                        maximum = Math.max(maximum, r.nanoseconds);
                        require(r.accesses == first.accesses && r.comparisons == first.comparisons
                                && r.movements == first.movements && r.batches == first.batches
                                && r.checksum == first.checksum, "Seeded metrics differ across repeats");
                    }
                }
                require(count == REPETITIONS, "An experiment does not have five repetitions");
                double average = total / count;
                // Optional: standard deviation shows how much times vary.
                double squared = 0;
                for (Result r : results) {
                    if (sameExperiment(first, r)) {
                        double difference = r.nanoseconds - average;
                        squared += difference * difference;
                    }
                }
                double sd = Math.sqrt(squared / (count - 1));
                out.printf(Locale.US, "%s,%s,%d,%d,%d,%.1f,%.6f,%.6f,%.6f,%.6f,%d,%d,%d,%d,%s%n",
                        first.workload, first.structure, first.n, first.m, count, average,
                        average / 1000000.0, minimum / 1000000.0, maximum / 1000000.0,
                        sd / 1000000.0, first.accesses, first.comparisons,
                        first.movements, first.batches, first.theory);
            }
        }
    }

    private static void saveEnvironment() throws IOException {
        Files.createDirectories(Paths.get("results"));
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get("results", "environment.txt")))) {
            out.println("Recorded at: " + Instant.now());
            out.println("Java version: " + System.getProperty("java.version"));
            out.println("Java VM: " + System.getProperty("java.vm.name"));
            out.println("Java vendor: " + System.getProperty("java.vendor"));
            out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            out.println("Architecture: " + System.getProperty("os.arch"));
            out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
            out.println("Maximum JVM memory bytes: " + Runtime.getRuntime().maxMemory());
            out.println("JVM arguments: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
            out.println("Seed: " + SEED + "; recorded repetitions: " + REPETITIONS);
            out.println("Warm-up: two unreported complete rounds at n=1000.");
            out.println("Measurements include logical counter updates, not data generation or printing.");
            out.println("Removal batch restorations and result validation are excluded from timing.");
            out.println("Runtime environment is a shared container, not the student's personal computer.");
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Warming up the JVM...");
        Inputs warmup = new Inputs(1000);
        for (int round = 0; round < 2; round++) {
            runRound(warmup, round, new ArrayList<Result>());
        }
        List<Result> results = new ArrayList<Result>();
        for (int n : SIZES) {
            Inputs inputs = new Inputs(n);
            for (int repeat = 1; repeat <= REPETITIONS; repeat++) {
                runRound(inputs, repeat, results);
            }
            System.out.println("Finished n=" + n + " with five repetitions.");
        }
        saveResults(results);
        saveEnvironment();
        System.out.println("Saved " + results.size() + " raw rows and 56 summary rows.");
        System.out.println("All workload validation checks passed. Checksum sink: " + sink);
    }
}
