package uniquindio.edu.co.inmobiliaria.structures;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Hash table with separate chaining for collision resolution.
 * <p>
 * Useful in the real estate project for:
 * <ul>
 *   <li>Fast property lookup by code ({@code HashTable<String, Inmueble>}).</li>
 *   <li>Client indexing by email ({@code HashTable<String, Cliente>}).</li>
 *   <li>Session storage by session ID ({@code HashTable<String, Sesion>}).</li>
 *   <li>Advisor assignment cache by zone ({@code HashTable<Zona, Asesor>}).</li>
 * </ul>
 *
 * @param <K> key type
 * @param <V> value type
 */
public class HashTable<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    private static final class Entry<K, V> {
        private final K key;
        private V value;
        private Entry<K, V> next;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public HashTable() {
        this.buckets = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be > 0");
        }
        this.buckets = (Entry<K, V>[]) new Entry[initialCapacity];
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
     * Associates the specified value with the specified key.
     * If the key already exists, the previous value is replaced.
     */
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if ((double) (size + 1) / buckets.length > LOAD_FACTOR_THRESHOLD) {
            rehash();
        }
        int idx = bucketIndex(key);
        Entry<K, V> cur = buckets[idx];
        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                cur.value = value;
                return;
            }
            cur = cur.next;
        }
        Entry<K, V> entry = new Entry<>(key, value);
        entry.next = buckets[idx];
        buckets[idx] = entry;
        size++;
    }

    /**
     * Returns the value associated with the given key.
     *
     * @throws NoSuchElementException if the key is not present
     */
    public V get(K key) {
        Entry<K, V> entry = findEntry(key);
        if (entry == null) {
            throw new NoSuchElementException("Key not found: " + key);
        }
        return entry.value;
    }

    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        Entry<K, V> entry = findEntry(key);
            if (entry != null) {
                return entry.value;
            }
            V value = mappingFunction.apply(key); 
            put(key, value);
                return value;
}

    /**
     * Returns the value associated with the given key, or {@code defaultValue}
     * if the key is not present.
     */
    public V getOrDefault(K key, V defaultValue) {
        Entry<K, V> entry = findEntry(key);
        return entry != null ? entry.value : defaultValue;
    }

    /**
     * Removes the entry for the given key and returns its value.
     *
     * @throws NoSuchElementException if the key is not present
     */
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        int idx = bucketIndex(key);
        Entry<K, V> prev = null;
        Entry<K, V> cur = buckets[idx];
        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                if (prev == null) {
                    buckets[idx] = cur.next;
                } else {
                    prev.next = cur.next;
                }
                size--;
                return cur.value;
            }
            prev = cur;
            cur = cur.next;
        }
        throw new NoSuchElementException("Key not found: " + key);
    }

    public boolean containsKey(K key) {
        return findEntry(key) != null;
    }

    // ──────────────────────────── bulk access ─────────────────────────

    /**
     * Returns all keys as a {@link DynamicArrayList}.
     */
    public DynamicArrayList<K> keys() {
        DynamicArrayList<K> list = new DynamicArrayList<>(size);
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> cur = bucket;
            while (cur != null) {
                list.add(cur.key);
                cur = cur.next;
            }
        }
        return list;
    }

    /**
     * Returns all values as a {@link DynamicArrayList}.
     */
    public DynamicArrayList<V> values() {
        DynamicArrayList<V> list = new DynamicArrayList<>(size);
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> cur = bucket;
            while (cur != null) {
                list.add(cur.value);
                cur = cur.next;
            }
        }
        return list;
    }

    // ──────────────────────────── utilities ───────────────────────────

    @SuppressWarnings("unchecked")
    public void clear() {
        this.buckets = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
        this.size = 0;
    }

    // ──────────────────────────── internals ───────────────────────────

    private int bucketIndex(K key) {
        return (key.hashCode() & 0x7FFFFFFF) % buckets.length;
    }

    private Entry<K, V> findEntry(K key) {
        if (key == null) {
            return null;
        }
        int idx = bucketIndex(key);
        Entry<K, V> cur = buckets[idx];
        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        Entry<K, V>[] oldBuckets = buckets;
        buckets = (Entry<K, V>[]) new Entry[oldBuckets.length * 2];
        size = 0;
        for (Entry<K, V> bucket : oldBuckets) {
            Entry<K, V> cur = bucket;
            while (cur != null) {
                put(cur.key, cur.value);
                cur = cur.next;
            }
        }
    }
}
