package uniquindio.edu.co.inmobiliaria.structures;

import java.util.NoSuchElementException;

/**
 * LIFO (Last-In-First-Out) stack implementation backed by a singly linked list.
 * <p>
 * Useful in the real estate project for:
 * <ul>
 *   <li>Undo/redo of operations (e.g. reverting property state changes).</li>
 *   <li>Navigation history (e.g. recently viewed properties).</li>
 *   <li>Recursive algorithm support (DFS on property graphs).</li>
 * </ul>
 *
 * @param <T> element type
 */
public class Stack<T> {

    private Node<T> top;
    private int size;

    // ──────────────────────────── capacity ────────────────────────────

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // ──────────────────────────── core ops ────────────────────────────

    /**
     * Pushes an element onto the top of the stack.
     */
    public void push(T value) {
        Node<T> n = new Node<>(value);
        n.setNext(top);
        top = n;
        size++;
    }

    /**
     * Removes and returns the element at the top of the stack.
     *
     * @throws NoSuchElementException if the stack is empty
     */
    public T pop() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        T v = top.getValue();
        top = top.getNext();
        size--;
        return v;
    }

    /**
     * Returns the element at the top without removing it.
     *
     * @throws NoSuchElementException if the stack is empty
     */
    public T peek() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        return top.getValue();
    }

    // ──────────────────────────── utilities ───────────────────────────

    public boolean contains(T value) {
        Node<T> cur = top;
        while (cur != null) {
            if (cur.getValue() == null ? value == null : cur.getValue().equals(value)) {
                return true;
            }
            cur = cur.getNext();
        }
        return false;
    }

    public void clear() {
        top = null;
        size = 0;
    }
}
