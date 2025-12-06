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
        assertEquals(i, j);
        boolean a = isSameNumber(i, j);
        assertTrue(a);
        System.out.println(isSameNumber(128, 128));
        System.out.println(isSameNumber(127, 127));
    }
}
