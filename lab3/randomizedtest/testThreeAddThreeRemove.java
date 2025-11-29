package randomizedtest;
import static org.junit.Assert.*;

public class testThreeAddThreeRemove {
    //@Test
    public void TestThreeAddThreeRemove(){
        AListNoResizing<Integer> noResizing = new AListNoResizing<>();
        BuggyAList<Integer> buggyAList = new BuggyAList<>();
        noResizing.addLast(4);buggyAList.addLast(4);
        noResizing.addLast(5);buggyAList.addLast(5);
        noResizing.addLast(6);buggyAList.addLast(6);
        assertEquals(noResizing.removeLast(),buggyAList.removeLast());
        assertEquals(noResizing.removeLast(),buggyAList.removeLast());
        org.junit.Assert.assertEquals(noResizing.removeLast(),buggyAList.removeLast());
    }
}