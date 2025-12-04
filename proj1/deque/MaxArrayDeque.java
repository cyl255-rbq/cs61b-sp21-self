package deque;

import deque.ArrayDeque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> c){
        super();
        comparator = c;
    }

    public T max(){
        return max(comparator);
    }

    public T max(Comparator<T> c){
        if(this.isEmpty())return null;
        T maxItem = this.get(0);
        for (int i = 1;i<this.size();i++){
            T thisItem = this.get(i);
            if(c.compare(maxItem,thisItem)<0)maxItem = thisItem;
        }
        return maxItem;
    }
}