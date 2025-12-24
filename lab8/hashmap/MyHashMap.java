package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    @Override
    public void clear() {
        bucketsSize = 16;
        buckets = createTable(bucketsSize);
        size = 0;
    }

    private int absHash(int hash, int bucketsSize) {
        if (hash < 0) {
            return bucketsSize + hash % bucketsSize;
        }
        return hash % bucketsSize;
    }
    private Node getHelper(K key) {
        if (buckets == null) {
            return null;
        }
        int index = absHash(key.hashCode(), bucketsSize);
        if (buckets[index] == null) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        Node node = getHelper(key);
        return node != null;
    }

    @Override
    public V get(K key) {
        Node node = getHelper(key);
        if (node == null) {
            return null;
        }
        return node.value;
    }

    @Override
    public int size() {
        return size;
    }

    private void resize() {
        int newSize = 2 * bucketsSize;
        Collection<Node>[] temp = createTable(newSize);
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node node : bucket) {
                int index = absHash(node.key.hashCode(), newSize);
                if (temp[index] == null) {
                    temp[index] = createBucket();
                }
                temp[index].add(node);
            }
        }
        bucketsSize = newSize;
        buckets = temp;
    }

    @Override
    public void put(K key, V value) {
        int index = absHash(key.hashCode(), bucketsSize);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        boolean replace = false;
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                node.value = value;
                replace = true;
                break;
            }
        }
        if (!replace) {
            Node nodeNow = createNode(key, value);
            buckets[index].add(nodeNow);
            size += 1;
            double nowLoadFactor = size * 1.0 / bucketsSize;
            if (nowLoadFactor == loadFactor) {
                resize();
            }
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        if (buckets == null) {
            return null;
        }
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node node : bucket) {
                set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        if (buckets == null) {
            return null;
        }
        int index = absHash(key.hashCode(), bucketsSize);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                buckets[index].remove(node);
                return node.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        if (buckets == null) {
            return null;
        }
        int index = absHash(key.hashCode(), bucketsSize);
        for (Node node : buckets[index]) {
            if (node.key.equals(key) && node.value.equals(value)) {
                buckets[index].remove(node);
                return node.value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    public class MyHashMapIterator implements Iterator<K> {

        // 状态1：巡逻员在哪一排书架（数组下标）
        private int arrayIndex;
        // 状态2：巡逻员在这一排书架的具体哪本书前（当前桶的迭代器）
        private Iterator<Node> currentShelfIterator;

        // 初始化：巡逻员进门，找到第一本有书的书架
        public MyHashMapIterator() {
            arrayIndex = 0;
            currentShelfIterator = null;
            findNextShelfWithBooks(); // 赶紧找到第一个有书的地方
        }

        // 辅助动作：这一排查完了，去找下一排有书的书架
        private void findNextShelfWithBooks() {
            // 如果当前书架还没查完，就别动
            if (currentShelfIterator != null && currentShelfIterator.hasNext()) {
                return;
            }

            // 当前书架查完了，往后走，直到找到一个有书的书架
            while (arrayIndex < buckets.length) {
                Collection<Node> shelf = buckets[arrayIndex];
                arrayIndex++; // 走到下一排

                // 如果这排书架有书
                if (shelf != null && !shelf.isEmpty()) {
                    currentShelfIterator = shelf.iterator(); // 把手指放在这排书的开头
                    return; // 找到了，开工
                }
            }
            // 如果走遍了所有书架都没书，那 currentShelfIterator 就只能是 null 了
            currentShelfIterator = null;
        }

        @Override
        public boolean hasNext() {
            // 只要手指指着的地方还有书（或者是能找到下一排书），就是 true
            return currentShelfIterator != null && currentShelfIterator.hasNext();
        }

        @Override
        public K next() {
            // 1. 抄下当前这本书
            K key = currentShelfIterator.next().key;

            // 2. 抄完这本书，马上检查：这排书架是不是空了？
            // 如果空了，赶紧走向下一排，为下一次抄写做准备
            if (!currentShelfIterator.hasNext()) {
                findNextShelfWithBooks();
            }

            return key;
        }
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!

    private int size = 0;
    private double loadFactor = 0.75;
    private int bucketsSize = 16;

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(bucketsSize);
    }

    public MyHashMap(int initialSize) {
        bucketsSize = initialSize;
        buckets = createTable(bucketsSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        bucketsSize = initialSize;
        buckets = createTable(bucketsSize);
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }
}
