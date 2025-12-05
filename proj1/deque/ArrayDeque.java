package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        nextFirst = items.length - 1;
        nextLast = 0;
        size = 0;
    }

    private void resize(int capacity) {
        T[] temp = (T[]) new Object[capacity];
        if (nextFirst + 1 == items.length) {
            nextFirst = -1;
        }
        if (nextLast - 1 < 0) {
            nextLast = items.length;
        }
        if ((nextFirst + 1) >= (nextLast - 1)) {
            System.arraycopy(items, nextFirst + 1, temp, 0, items.length - (nextFirst + 1));
            System.arraycopy(items, 0, temp, items.length - (nextFirst + 1), nextLast);
        } else {
            System.arraycopy(items, nextFirst + 1, temp, 0, nextLast - nextFirst - 1);
        }
        items = temp;
        nextFirst = items.length - 1;
        nextLast = size;
    }

    private void addJudgment() {
        if (size == items.length) {
            resize(2 * items.length);
        }
    }

    private void removeJudgment() {
        if (items.length <= 32) {
            return;
        }
        if (size / (double) items.length < 0.25) {
            resize(items.length / 2);
        }
    }

    @Override
    public void addFirst(T item) {
        addJudgment();
        items[nextFirst] = item;
        nextFirst -= 1;
        if (nextFirst == -1) {
            nextFirst = items.length - 1;
        }
        size += 1;
    }

    @Override
    public void addLast(T item) {
        addJudgment();
        items[nextLast] = item;
        nextLast += 1;
        if (nextLast == items.length) {
            nextLast = 0;
        }
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++)
            System.out.print(items[i] + " ");
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        removeJudgment();
        nextFirst += 1;
        if (nextFirst == items.length) {
            nextFirst = 0;
        }
        T temp = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        return temp;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        removeJudgment();
        nextLast -= 1;
        if (nextLast == -1) {
            nextLast = items.length - 1;
        }
        T temp = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        return temp;
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        index += nextFirst + 1;
        if (index >= items.length) {
            index -= items.length;
        }
        return items[index];
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int wizPos;
        public ArrayDequeIterator(){
            wizPos = 0;
        }

        @Override
        public boolean hasNext() {
            return wizPos < size;
        }

        @Override
        public T next() {
            int temp = nextFirst;
            if (temp + 1 == items.length) {
                temp = -1;
            }
            T returnItem = items[wizPos + temp + 1];
            wizPos += 1;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Deque<?>)) {
            return false;
        }
        Deque<T> other = (Deque<T>) o;
        if (other.size() != this.size) {
            return false;
        }
        if (other == this) {
            return true;
        }
        for (int i = 0; i < size; i++) {
            if (!other.get(i).equals(this.get(i))) {
                return false;
            }
        }
        return true;
    }
}
