package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;
    private int size;

    public BSTMap() {
        size = 0;
    }

    private class Node {
        private K key;
        private V val;
        private Node left, right;

        Node(K key, V val) {
            this.key = key;
            this.val = val;
        }

        Node get(K k) {
            int cmp = k.compareTo(key);
            if (cmp == 0) {
                return this;
            } else if (cmp > 0) {
                if (right == null) {
                    return null;
                } else {
                    return right.get(k);
                }
            } else {
                if (left == null) {
                    return null;
                } else {
                    return left.get(k);
                }
            }
        }
    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        if (root == null) {
            return false;
        }
        return root.get(key) != null;
    }

    private Node getHelper(K key) {
        if (root == null) {
            return null;
        }
        return root.get(key);
    }

    @Override
    public V   get(K key) {
        Node get = getHelper(key);
        if (get == null) {
            return null;
        }
        return get.val;
    }

    @Override
    public int size() {
        return size;
    }

    private Node putHelper(Node T, K key,V value) {
        if (T == null) {
            size += 1;
            return new Node(key, value);
        }
        int cmp = key.compareTo(T.key);
        if (cmp > 0) {
            T.right = putHelper(T.right, key, value);
        } else if (cmp < 0) {
            T.left = putHelper(T.left, key, value);
        } else {
            T.val = value;
        }
        return T;
    }

    @Override
    public void put(K key, V value) {
        root = putHelper(root, key, value);
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (K key : this) {
            keys.add(key);
        }
        return keys;
    }

    private Node min(Node node) {
        if (node.right == null) {
            return node;
        } else {
            return min(node.right);
        }
    }

    private Node deleteMin(Node node) {
        if (node.right == null) {
            return node.left;
        }
        node.right = deleteMin(node.right);
        return node;
    }

    private Node remove(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left  = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.right == null) {
                return node.left;
            } else if (node.left  == null) {
                return node.right;
            } else {
            Node temp = node;
            node = min(temp.left);
            node.left = deleteMin(temp.left);
            node.right = temp.right;
            }
        }
        return node;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V val = get(key);
        root = remove(root, key);
        size = size - 1;
        return val;
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        V val = get(key);
        if (val == get(key)) {
            return null;
        } else {
            root = remove(root, key);
            size = size - 1;
            return val;
        }
    }


    private class BSTMapIter implements Iterator<K> {
        private Stack<Node> stack;

        public BSTMapIter() {
            stack = new Stack<>();
            moveLeft(root);
        }

        private void moveLeft(Node p) {
            while (p != null) {
                stack.push(p);  // 压进去！
                p = p.left;     // 继续往左走
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) throw new java.util.NoSuchElementException();
            Node current = stack.pop();
            if (current.right != null) {
                moveLeft(current.right);
            }
            return current.key;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    private void printHelper(Node T) {
        if (T == null) {
            return;
        }
        printHelper(T.left);
        System.out.println(T.key.toString() + T.val.toString());
        printHelper(T.right);
    }

    public void printInOrder() {
        printHelper(root);
    }
}
