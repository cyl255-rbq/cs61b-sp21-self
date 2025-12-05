package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T> , Deque<T>{

    private static class IntNode<T> {
        public T item;
        public IntNode<T> next;
        public IntNode<T> prev;
        public IntNode(T i, IntNode<T> n, IntNode<T> m) {
            item = i;
            next = n;
            prev = m;
        }
    }
    private IntNode<T> sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new IntNode<>(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        IntNode<T> temp = sentinel.next;
        sentinel.next = new IntNode<>(item, temp, sentinel);
        temp.prev = sentinel.next;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        sentinel.prev.next = new IntNode<>(item, sentinel, sentinel.prev);
        sentinel.prev = sentinel.prev.next;
        size += 1;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void printDeque() {
        IntNode<T> temp = sentinel.next;
        while (temp != sentinel) {
            System.out.print(temp.item + " ");
            temp = temp.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        IntNode<T> temp = sentinel.next;
        if(temp == sentinel) {return null;}
        sentinel.next = temp.next;
        temp.next.prev = sentinel;
        size -= 1;
        return temp.item;
    }

    @Override
    public T removeLast() {
        IntNode<T> temp = sentinel.prev;
        if(temp == sentinel)return null;
        sentinel.prev = temp.prev;
        temp.prev.next = sentinel;
        size -= 1;
        return temp.item;
    }

    @Override
    public T get(int index) {
        IntNode<T> temp = sentinel.next;
        while (temp != sentinel){
            if(index == 0)return temp.item;
            temp = temp.next;
            index -= 1;
        }
        return null;
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int wizPos;
        private IntNode<T> wizPosItem;
        public LinkedListDequeIterator() {
            wizPos = 1;
            wizPosItem = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return wizPos <= size;
        }

        @Override
        public T next() {
            T returnItem = wizPosItem.item;
            wizPosItem = wizPosItem.next;
            wizPos += 1;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {return false;}
        if (!(o instanceof Deque<?>)) {return false;}
        Deque<T> other = (Deque<T>) o;
        if(other.size() != this.size()) {return false;}
        if(other == this) {return true;}
        for(int i=0;i<size;i++){
            if(other.get(i) != this.get(i)) {return false;}
        }
        return true;
    }

    public T getRecursive(int index) {
        return helpGetRecursive(index, sentinel.next);
    }

    private T helpGetRecursive(int index, IntNode<T> temp) {
        if(temp == sentinel) {return null;}
        if(index == 0) {return temp.item;}
        return helpGetRecursive(index-1, temp.next);
    }
}