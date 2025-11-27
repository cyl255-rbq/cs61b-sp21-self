package deque;

public class LinkedListDeque<T> {

    private static class IntNode<T>{
        public T item;
        public IntNode<T> next;
        public IntNode<T> previous;
        public IntNode(T i, IntNode<T> n, IntNode<T> m){
            item = i;
            next = n;
            previous = m;
        }
    }
    private IntNode<T> sentinel;
    private int size;

    public LinkedListDeque(){
        sentinel = new IntNode<>(null, null, null);
        sentinel.next = sentinel;
        sentinel.previous = sentinel;
        size = 0;
    }

    public LinkedListDeque(T x){
        sentinel = new IntNode<>(null, null, null);
        sentinel.next = new IntNode<>(x, sentinel, sentinel);
        sentinel.previous = sentinel.next;
        size = 1;
    }

    public void addFirst(T item){
        IntNode<T> temp = sentinel.next;
        sentinel.next = new IntNode<>(item, temp, sentinel);
        temp.previous = sentinel.next;
        size += 1;
    }

    public void addLast(T item){
        sentinel.previous.next = new IntNode<>(item, sentinel, sentinel.previous);
        sentinel.previous = sentinel.previous.next;
        size += 1;
    }

    public boolean isEmpty(){
        if(sentinel.next==sentinel)return true;
        else return false;
    }

    public int size(){
        return size;
    }

    public void printDeque(){
        IntNode<T> temp = sentinel.next;
        while (temp !=sentinel){
            System.out.print(temp.item + " ");
        }
        System.out.println();
    }

    public T removeFirst(){
        IntNode<T> temp = sentinel.next;
        if(temp == sentinel)return null;
        sentinel.next = temp.next;
        temp.next.previous = sentinel;
        return temp.item;
    }

    public T removeLast(){
        IntNode<T> temp = sentinel.previous;
        if(temp == sentinel)return null;
        sentinel.previous = temp.previous;
        temp.previous.next = sentinel;
        return temp.item;
    }

    public T get(int index){
        IntNode<T> temp = sentinel.next;
        while (temp != sentinel){
            if(index == 0)return temp.item;
            temp = temp.next;
            index -= 1;
        }
        return null;
    }

    public T getRecursive(int index){
        return helpGetRecursive(index, sentinel);
    }

    public T helpGetRecursive(int index, IntNode<T> temp){
        if(temp == sentinel)return null;
        if(index == 0)return temp.item;
        return helpGetRecursive(index-1, temp.next);
    }
}