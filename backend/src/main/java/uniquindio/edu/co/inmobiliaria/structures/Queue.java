package uniquindio.edu.co.inmobiliaria.structures;

import java.util.NoSuchElementException;

/**
 * FIFO (First-In-First-Out) queue implementation backed by a singly linked list.
 * <p>
 * Useful in the real estate project for:
 * <ul>
 *   <li>Processing visit requests in order of arrival.</li>
 *   <li>Queuing pending operations (sales, rentals) for approval.</li>
 *   <li>Managing client service queues.</li>
 * </ul>
 *
 * @param <T> element type
 */
public class Queue<T> {

    private Node<T> front;
    private Node<T> rear;
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
     * Adds an element to the rear of the queue.
     */
    public void enqueue(T value) {
        Node<T> n = new Node<>(value);
        if (rear == null) {
            front = n;
            rear = n;
        } else {
            rear.setNext(n);
            rear = n;
        }
        size++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    public T dequeue() {
        if (front == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        T v = front.getValue();
        front = front.getNext();
        if (front == null) {
            rear = null;
        }
        size--;
        return v;
    }

    /**
     * Returns the element at the front without removing it.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    public T peek() {
        if (front == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return front.getValue();
    }

    // ──────────────────────────── utilities ───────────────────────────

    public boolean contains(T value) {
        Node<T> cur = front;
        while (cur != null) {
            if (cur.getValue() == null ? value == null : cur.getValue().equals(value)) {
                return true;
            }
            cur = cur.getNext();
        }
        return false;
    }

    public boolean remove(T value) {
        if (front == null) {
            return false;
        }
        if (front.getValue() == null ? value == null : front.getValue().equals(value)) {
            dequeue();
            return true;
        }
        Node<T> previous = front;
        Node<T> current = front.getNext();
        while (current != null) {
            if (current.getValue() == null ? value == null : current.getValue().equals(value)) {
                previous.setNext(current.getNext());
                if (current == rear) {
                    rear = previous;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.getNext();
        }
        return false;
    }

    public java.util.List<T> toList() {
        java.util.List<T> values = new java.util.ArrayList<>(size);
        Node<T> cur = front;
        while (cur != null) {
            values.add(cur.getValue());
            cur = cur.getNext();
        }
        return values;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }
}
