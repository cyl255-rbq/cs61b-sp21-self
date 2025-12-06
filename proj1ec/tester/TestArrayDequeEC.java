package tester;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;
import static org.junit.Assert.*;

/**
 * @source StudentArrayDequeLauncher.java
 * @source AssertEqualsStringDemo.java
 */
public class TestArrayDequeEC {
    @Test
    public void test() {
        String record = "";
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> good = new ArrayDequeSolution<>();
        for (int i = 0; i < 1000; i += 1) {
            assertEquals(record, good.size(), sad.size());
            int randomNumber = StdRandom.uniform(0, 9);
            if (randomNumber == 1 || randomNumber == 5 || randomNumber == 6) {
                sad.addLast(i);
                good.addLast(i);
                record += "addLast(" + i + ")\n";
            } else if (randomNumber == 2 || randomNumber == 7 || randomNumber == 8) {
                sad.addFirst(i);
                good.addFirst(i);
                record += "addFirst(" + i + ")\n";
            } else if (randomNumber == 3) {
                if (!good.isEmpty()) {
                    Integer sad2 = sad.removeFirst();
                    Integer good2 = good.removeFirst();
                    record += "removeFirst()\n";
                    assertEquals(record, good2, sad2);
                }
            } else {
                if (!good.isEmpty()) {
                    Integer sad2 = sad.removeLast();
                    Integer good2 = good.removeLast();
                    record += "removeLast()\n";
                    assertEquals(record, good2, sad2);
                }
            }
        }
        sad.printDeque();
        good.printDeque();
    }
}
