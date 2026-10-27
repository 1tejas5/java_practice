import java.util.Arrays;
import java.util.List;

/*
 * TOPIC: Arrays - creation, defaults, 2D/jagged, the Arrays utility class, common traps
 * RUN:   java code/01_basics/ArraysDemo.java
 */
public class ArraysDemo {

    public static void main(String[] args) {
        section("1. Creating arrays and default values");
        int[] nums = new int[4];
        double[] ds = new double[2];
        boolean[] flags = new boolean[2];
        String[] names = new String[2];
        char[] chars = new char[2];
        System.out.println("int[]     " + Arrays.toString(nums));
        System.out.println("double[]  " + Arrays.toString(ds));
        System.out.println("boolean[] " + Arrays.toString(flags));
        System.out.println("String[]  " + Arrays.toString(names));
        System.out.println("char[] default is '\\u0000' -> (int) " + (int) chars[0]);

        int[] literal = {5, 3, 9, 1, 7};
        System.out.println("printing the array directly: " + literal + "  <- type@hash, not content!");
        System.out.println("Arrays.toString:             " + Arrays.toString(literal));
        System.out.println("length (a field, no ()):     " + literal.length);

        section("2. 2D and jagged arrays");
        int[][] grid = new int[3][4];
        grid[1][2] = 5;
        System.out.println(Arrays.deepToString(grid));
        int[][] jagged = new int[3][];
        jagged[0] = new int[]{1};
        jagged[1] = new int[]{1, 2};
        jagged[2] = new int[]{1, 2, 3};
        for (int[] row : jagged) {
            System.out.println("row length " + row.length + " -> " + Arrays.toString(row));
        }

        section("3. The Arrays toolbox");
        int[] copy = Arrays.copyOf(literal, literal.length);
        Arrays.sort(copy);
        System.out.println("sorted copy         " + Arrays.toString(copy));
        System.out.println("original untouched  " + Arrays.toString(literal));
        System.out.println("binarySearch(7)     index " + Arrays.binarySearch(copy, 7) + " (needs a sorted array)");
        System.out.println("copyOfRange(1,3)    " + Arrays.toString(Arrays.copyOfRange(copy, 1, 3)));
        int[] filled = new int[3];
        Arrays.fill(filled, 7);
        System.out.println("fill(7)             " + Arrays.toString(filled));
        int[] other = {1, 3, 5, 7, 9};
        System.out.println("copy == other       " + (copy == other) + " (references)");
        System.out.println("Arrays.equals       " + Arrays.equals(copy, other) + " (content)");
        System.out.println("sum with stream     " + Arrays.stream(literal).sum());
        Integer[] boxed = {5, 3, 9};
        Arrays.sort(boxed, (p, q) -> q - p);          // custom order needs objects, not primitives
        System.out.println("descending (boxed)  " + Arrays.toString(boxed));

        section("4. Arrays.asList is a fixed-size VIEW");
        String[] backing = {"a", "b", "c"};
        List<String> view = Arrays.asList(backing);
        view.set(0, "Z");                             // allowed - writes through to the array
        System.out.println("after view.set(0,\"Z\") array = " + Arrays.toString(backing));
        try {
            view.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("view.add -> UnsupportedOperationException (size is fixed)");
        }

        section("5. Traps");
        try {
            int bad = literal[5];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("AIOOBE: " + e.getMessage());
        }
        Object[] objs = new String[2];                // arrays are covariant
        try {
            objs[0] = 42;                             // compiles, fails at runtime
        } catch (ArrayStoreException e) {
            System.out.println("ArrayStoreException: " + e.getMessage());
        }

        section("6. Classic algorithms on arrays");
        int[] data = {4, 8, 1, 8, 6};
        System.out.println("max = " + max(data) + ", second largest = " + secondLargest(data));
        int[] rev = data.clone();
        reverse(rev);
        System.out.println("reversed " + Arrays.toString(rev));
    }

    static int max(int[] a) {
        int m = a[0];
        for (int v : a) if (v > m) m = v;
        return m;
    }

    static int secondLargest(int[] a) {
        int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
        for (int v : a) {
            if (v > first) {
                second = first;
                first = v;
            } else if (v > second && v != first) {
                second = v;
            }
        }
        return second;
    }

    static void reverse(int[] a) {
        for (int l = 0, r = a.length - 1; l < r; l++, r--) {
            int t = a[l];
            a[l] = a[r];
            a[r] = t;
        }
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
