package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.Iterator;


/** Performs some basic linked list tests. */
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();

        lld1.removeLast();
        lld1.removeLast();
        lld1.removeLast();
        assertEquals(0, lld1.size());
        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());

        for(int i=0;i<1000;i++) {
            lld1.addFirst(6);
        }
        for(int i=0;i<999;i++) {
            lld1.removeFirst();
        }
        assertEquals(1, lld1.size());

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);

    }

    @Test
    /* Check if you can create ArrayDeques with different parameterized types*/
    public void multipleParamTest() {
        ArrayDeque<String>  lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
        System.out.println(s);
        System.out.println(d);
        System.out.println(b);
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());


    }

    @Test
    public void get(){
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 100; i++) {
            lld1.addLast(i);
        }
        for (int i = 0; i < 50; i++) {
            lld1.removeFirst();
        }
        System.out.println(lld1.size());
        assertEquals((int)lld1.get(lld1.size()-1),99);

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        // 1. 只往屁股后面加 100 万个
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        // 此时队列：[0, 1, 2, ..., 999999]

        // 2. 从头取 50 万个
        // 应该依次取出 0, 1, 2...
        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        // 此时队列剩下：[500000, 500001, ..., 999999]

        // 3. 从屁股取剩下的 50 万个
        // 应该依次取出 999999, 999998...
        for (double i = 999999; i >= 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void littleLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 100; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 2; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

    }


    private static void printTimingTable(ArrayDeque<Integer> Ns, ArrayDeque<Double> times, ArrayDeque<Integer> opCounts) {
            System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
            System.out.printf("------------------------------------------------------------\n");
            for (int i = 0; i < Ns.size(); i += 1) {
                int N = Ns.get(i);
                double time = times.get(i);
                int opCount = opCounts.get(i);
                double timePerOp = time / opCount * 1e6;
                System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
            }
        }

    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> L = new ArrayDeque<>();
        LinkedListDeque<Integer> M = new LinkedListDeque<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 5);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                M.addLast(randVal);
                //System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();
                int sizeM = M.size();
                //System.out.println("size: " + size);
                assertEquals(sizeL, sizeM);
            } else if (operationNumber == 2) {
                if (L.size() > 0) {
                    int randVal = StdRandom.uniform(0, 100);
                    L.addFirst(randVal);
                    M.addFirst(randVal);
                }
            } else if (operationNumber == 2) {
                if (L.size() > 0) {
                    L.removeFirst();
                    M.removeFirst();
                }
            } else {
                if (L.size() > 0) {
                    L.removeLast();
                    M.removeLast();
                }
            }
        }
    }


    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        ArrayDeque<Integer> Ns = new ArrayDeque<>();
        ArrayDeque<Double> times = new ArrayDeque<>();
        ArrayDeque<Integer> opCounts = new ArrayDeque<>();
        int N=1000;
        while (N <= 128000000){
            Ns.addLast(N);
            opCounts.addLast(N);
            ArrayDeque<Integer> testList = new ArrayDeque<>();
            Stopwatch sw = new Stopwatch();
            for(int i=0;i<N;i++) {
                testList.addLast(1);
                testList.removeLast();
                testList.addFirst(2);
                testList.removeFirst();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
            N *= 2;
        }
        printTimingTable(Ns, times, opCounts);
    }

    @Test
    public void iterator(){
        int N=100;
        ArrayDeque<Integer> test = new ArrayDeque<>();
        for (int i=0;i<N;i++){
            test.addLast(i);
        }

        for (int i:test){
            System.out.println(i);
        }
    }
}
