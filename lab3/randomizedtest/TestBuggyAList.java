package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> M = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);M.addLast(randVal);
                //System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();int sizeM = M.size();
                //System.out.println("size: " + size);
                assertEquals(sizeL,sizeM);
            } else if (operationNumber == 2){
                if(L.size()>0){
                    int getLastL = L.getLast();int getLastM = M.getLast();
                    //System.out.println("getLast(" + getLast + ")");
                    assertEquals(getLastL,getLastM);
                }
            } else {
                if(L.size()>0){
                    int removeLastL = L.removeLast();int removeLastM = M.removeLast();
                    //System.out.println("removeLast(" + removeLast + ")");
                    assertEquals(removeLastL,removeLastM);
                }
            }
        }
    }
}
