package uniquindio.edu.co.inmobiliaria.structures;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Simple singly linked list implementation.
 *
 * @param <T> element type
 */
public class SinglyLinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>(size);
        Node<T> cur = head;
        while (cur != null) {
            list.add(cur.getValue());
            cur = cur.getNext();
        }
        return list;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public void addFirst(T value) {
        Node<T> n = new Node<>(value);
        n.setNext(head);
        head = n;
        if (tail == null) {
            tail = n;
        }
        size++;
    }

    public void addLast(T value) {
        Node<T> n = new Node<>(value);
        if (tail == null) {
            head = n;
            tail = n;
        } else {
            tail.setNext(n);
            tail = n;
        }
        size++;
    }

    public T getFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        return head.getValue();
    }

    public T getLast() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.getValue();
    }

    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(T value) {
        int idx = 0;
        Node<T> cur = head;
        while (cur != null) {
            if (Objects.equals(cur.getValue(), value)) {
                return idx;
            }
            cur = cur.getNext();
            idx++;
        }
        return -1;
    }

    public T removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        T v = head.getValue();
        head = head.getNext();
        if (head == null) {
            tail = null;
        }
        size--;
        return v;
    }

    public T removeLast() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        if (head == tail) {
            T v = head.getValue();
            head = null;
            tail = null;
            size = 0;
            return v;
        }

        Node<T> prev = head;
        while (prev.getNext() != tail) {
            prev = prev.getNext();
        }
        T v = tail.getValue();
        prev.setNext(null);
        tail = prev;
        size--;
        return v;
    }

    public boolean remove(T value) {
        if (head == null) {
            return false;
        }
        if (Objects.equals(head.getValue(), value)) {
            removeFirst();
            return true;
        }
        Node<T> prev = head;
        Node<T> cur = head.getNext();
        while (cur != null) {
            if (Objects.equals(cur.getValue(), value)) {
                prev.setNext(cur.getNext());
                if (cur == tail) {
                    tail = prev;
                }
                size--;
                return true;
            }
            prev = cur;
            cur = cur.getNext();
        }
        return false;
    }
}
