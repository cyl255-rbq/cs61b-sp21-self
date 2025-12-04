package deque;

import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;
public class MaxArrayDequeTest {

    private static class IntComparator implements Comparator<Integer>{
        public int compare(Integer a,Integer b){
            return a.compareTo(b);
        }
    }

    public static class StringLengthComparator implements Comparator<String> {
        public int compare(String a, String b) {
            return a.length()-b.length();
        }
    }

    @Test
    public void test1(){
        MaxArrayDeque<Integer> q1 = new MaxArrayDeque<>(new IntComparator());
        q1.addLast(1);
        q1.addLast(5);
        q1.addLast(10);
        for (int i=0;i<100;i++)
            q1.addFirst(20);
        assertEquals((Integer)20,q1.max());

        MaxArrayDeque<String> q2 = new MaxArrayDeque<>(new StringLengthComparator());
        q2.addFirst("cat");
        q2.addFirst("i");
        q2.addFirst("butterfly");
        assertEquals("butterfly",q2.max());

    }
}