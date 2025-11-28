package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
         items = (T[]) new Object[8];
         nextFirst = items.length-1;
         nextLast = 0;
         size = 0;
    }

    private void addResize(int Capacity){
        T[] temp = (T[]) new Object[Capacity];
        System.arraycopy(items, nextFirst+1, temp, 0, size-(nextFirst+1));
        System.arraycopy(items, 0, temp, size-nextFirst, nextFirst+1);
        items = temp;
        nextFirst = items.length-1;
        nextLast = size;
    }

    private void removeResize(int Capacity){
        if(nextFirst>nextLast) addResize(Capacity);
        else {
            T[] temp = (T[]) new Object[Capacity];
            System.arraycopy(items, nextFirst, temp, 0, nextLast-nextFirst-1);
        }
    }

    private void addJudgment(){
        if(size == items.length){
            addResize(2*size);
        }
    }

    private void removeJudgment(){
        double itemsLength = items.length;
        if(size/itemsLength < 0.25){
            removeResize(size/2);
        }
    }

    public void addFirst(T item){
        addJudgment();
        items[nextFirst] = item;
        nextFirst -= 1;
        if(nextFirst == -1)nextFirst = items.length-1;
        size += 1;
    }

    public void addLast(T item){
        addJudgment();
        items[nextLast] = item;
        nextLast += 1;
        if(nextLast == items.length)nextLast = 0;
        size += 1;
    }

    public boolean isEmpty(){
        if(size == 0)return true; return false;
    }

    public int size(){
        return size;
    }

    public T removeFirst(){
        removeJudgment();
        nextFirst += 1;
        if(nextFirst == items.length)nextFirst = 0;
        T temp = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        return temp;
    }

    public T removeLast(){
        removeJudgment();
        nextLast -= 1;
        if(nextLast == -1)nextLast = items.length-1;
        T temp = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        return temp;
    }

    public T get(int index){

    }



}