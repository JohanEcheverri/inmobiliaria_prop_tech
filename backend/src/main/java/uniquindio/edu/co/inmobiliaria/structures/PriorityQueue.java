package uniquindio.edu.co.inmobiliaria.structures;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Min-heap-based priority queue.
 * <p>
 * The element with the <em>smallest</em> value according to the supplied
 * {@link Comparator} is dequeued first.  To get max-priority behaviour,
 * pass {@code comparator.reversed()}.
 * <p>
 * Useful in the real estate project for:
 * <ul>
 *   <li>Sorting properties by price (cheapest or most expensive first).</li>
 *   <li>Prioritizing visits by date/urgency.</li>
 *   <li>Ranking advisors by workload (fewest active deals first).</li>
 * </ul>
 *
 * @param <T> element type
 */
public class PriorityQueue<T> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] heap;
    private int size;
    private final Comparator<T> comparator;

    public PriorityQueue(Comparator<T> comparator) {
        this(comparator, DEFAULT_CAPACITY);
    }

    public PriorityQueue(Comparator<T> comparator, int initialCapacity) {
        if (comparator == null) {
            throw new IllegalArgumentException("comparator must not be null");
        }
        this.comparator = comparator;
        this.heap = new Object[Math.max(DEFAULT_CAPACITY, initialCapacity)];
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
     * Inserts an element into the priority queue.
     */
    public void enqueue(T value) {
        ensureCapacity(size + 1);
        heap[size] = value;
        siftUp(size);
        size++;
    }

    /**
     * Removes and returns the highest-priority (smallest per comparator) element.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (size == 0) {
            throw new NoSuchElementException("PriorityQueue is empty");
        }
        T min = (T) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return min;
    }

    /**
     * Returns the highest-priority element without removing it.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("PriorityQueue is empty");
        }
        return (T) heap[0];
    }

    // ──────────────────────────── utilities ───────────────────────────

    public boolean contains(T value) {
        for (int i = 0; i < size; i++) {
            if (heap[i] == null ? value == null : heap[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        Arrays.fill(heap, 0, size, null);
        size = 0;
    }

    // ──────────────────────────── heap helpers ────────────────────────

    @SuppressWarnings("unchecked")
    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare((T) heap[index], (T) heap[parent]) >= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int index) {
        int half = size / 2;
        while (index < half) {
            int left = 2 * index + 1;
            int right = left + 1;
            int smallest = left;
            if (right < size && comparator.compare((T) heap[right], (T) heap[left]) < 0) {
                smallest = right;
            }
            if (comparator.compare((T) heap[index], (T) heap[smallest]) <= 0) {
                break;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        Object tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void ensureCapacity(int needed) {
        if (needed <= heap.length) {
            return;
        }
        heap = Arrays.copyOf(heap, Math.max(needed, heap.length * 2));
    }
}
