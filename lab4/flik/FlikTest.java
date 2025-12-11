package flik;

import org.junit.Test;

import static flik.Flik.isSameNumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlikTest {
    @Test
    public void test() {
        int i = 128;
        int j = 128;
        Integer a = 128;
        Integer b = 128;
        System.out.println(a == b);
        boolean c = isSameNumber(i, j);
        assertTrue(c);
        System.out.println(isSameNumber(128, 128));
        System.out.println(isSameNumber(127, 127));
        assertEquals(i, j);
    }
}
