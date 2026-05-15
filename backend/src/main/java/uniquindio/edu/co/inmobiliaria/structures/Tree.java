package uniquindio.edu.co.inmobiliaria.structures;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Binary Search Tree (BST) ordered by an external {@link Comparator}.
 * <p>
 * Useful in the real estate project for:
 * <ul>
 *   <li>Maintaining properties sorted by price for range queries.</li>
 *   <li>Organising clients by budget in a searchable structure.</li>
 *   <li>Keeping a sorted catalogue of property areas.</li>
 *   <li>Implementing hierarchical categories (zones → cities → neighbourhoods).</li>
 * </ul>
 *
 * @param <T> element type
 */
public class Tree<T> {

    private static final class TreeNode<T> {
        private T value;
        private TreeNode<T> left;
        private TreeNode<T> right;

        private TreeNode(T value) {
            this.value = value;
        }
    }

    private TreeNode<T> root;
    private int size;
    private final Comparator<T> comparator;

    public Tree(Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("comparator must not be null");
        }
        this.comparator = comparator;
        this.root = null;
        this.size = 0;
    }

    // ──────────────────────────── capacity ────────────────────────────

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // ──────────────────────────── core ops ────────────────────────────

    /**
     * Inserts a value into the BST.  Duplicate values (per comparator) are
     * placed in the right subtree.
     */
    public void insert(T value) {
        root = insertRec(root, value);
        size++;
    }

    /**
     * Searches for an element equal to {@code value} according to the comparator.
     *
     * @return the stored element, or {@code null} if not found
     */
    public T search(T value) {
        TreeNode<T> node = searchNode(root, value);
        return node != null ? node.value : null;
    }

    /**
     * Returns {@code true} if the tree contains an element equal to {@code value}.
     */
    public boolean contains(T value) {
        return searchNode(root, value) != null;
    }

    /**
     * Removes a single occurrence of {@code value} from the tree.
     *
     * @return {@code true} if the element was found and removed
     */
    public boolean remove(T value) {
        int before = size;
        root = removeRec(root, value);
        return size < before;
    }

    /**
     * Returns the minimum element in the tree.
     *
     * @throws NoSuchElementException if the tree is empty
     */
    public T min() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMin(root).value;
    }

    /**
     * Returns the maximum element in the tree.
     *
     * @throws NoSuchElementException if the tree is empty
     */
    public T max() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMax(root).value;
    }

    // ──────────────────────────── traversal ───────────────────────────

    /**
     * Returns all elements in sorted (in-order) order inside a
     * {@link DynamicArrayList}.
     */
    public DynamicArrayList<T> inOrder() {
        DynamicArrayList<T> result = new DynamicArrayList<>(size);
        inOrderRec(root, result);
        return result;
    }

    /**
     * Returns all elements in pre-order inside a {@link DynamicArrayList}.
     */
    public DynamicArrayList<T> preOrder() {
        DynamicArrayList<T> result = new DynamicArrayList<>(size);
        preOrderRec(root, result);
        return result;
    }

    /**
     * Returns all elements in post-order inside a {@link DynamicArrayList}.
     */
    public DynamicArrayList<T> postOrder() {
        DynamicArrayList<T> result = new DynamicArrayList<>(size);
        postOrderRec(root, result);
        return result;
    }

    /**
     * Returns the height of the tree (0 for a single-node tree, -1 if empty).
     */
    public int height() {
        return heightRec(root);
    }

    // ──────────────────────────── utilities ───────────────────────────

    public void clear() {
        root = null;
        size = 0;
    }

    // ──────────────────────────── recursive helpers ───────────────────

    private TreeNode<T> insertRec(TreeNode<T> node, T value) {
        if (node == null) {
            return new TreeNode<>(value);
        }
        if (comparator.compare(value, node.value) < 0) {
            node.left = insertRec(node.left, value);
        } else {
            node.right = insertRec(node.right, value);
        }
        return node;
    }

    private TreeNode<T> searchNode(TreeNode<T> node, T value) {
        if (node == null) {
            return null;
        }
        int cmp = comparator.compare(value, node.value);
        if (cmp == 0) {
            return node;
        }
        return cmp < 0 ? searchNode(node.left, value) : searchNode(node.right, value);
    }

    private TreeNode<T> removeRec(TreeNode<T> node, T value) {
        if (node == null) {
            return null;
        }
        int cmp = comparator.compare(value, node.value);
        if (cmp < 0) {
            node.left = removeRec(node.left, value);
        } else if (cmp > 0) {
            node.right = removeRec(node.right, value);
        } else {
            // Found the node to remove
            size--;
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            // Two children: replace with in-order successor
            TreeNode<T> successor = findMin(node.right);
            node.value = successor.value;
            size++; // compensate: removeRec will decrement again
            node.right = removeRec(node.right, successor.value);
        }
        return node;
    }

    private TreeNode<T> findMin(TreeNode<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private TreeNode<T> findMax(TreeNode<T> node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    private void inOrderRec(TreeNode<T> node, DynamicArrayList<T> list) {
        if (node == null) return;
        inOrderRec(node.left, list);
        list.add(node.value);
        inOrderRec(node.right, list);
    }

    private void preOrderRec(TreeNode<T> node, DynamicArrayList<T> list) {
        if (node == null) return;
        list.add(node.value);
        preOrderRec(node.left, list);
        preOrderRec(node.right, list);
    }

    private void postOrderRec(TreeNode<T> node, DynamicArrayList<T> list) {
        if (node == null) return;
        postOrderRec(node.left, list);
        postOrderRec(node.right, list);
        list.add(node.value);
    }

    private int heightRec(TreeNode<T> node) {
        if (node == null) return -1;
        return 1 + Math.max(heightRec(node.left), heightRec(node.right));
    }
}
