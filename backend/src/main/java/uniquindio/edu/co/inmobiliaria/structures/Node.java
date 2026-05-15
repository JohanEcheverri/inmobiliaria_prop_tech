package uniquindio.edu.co.inmobiliaria.structures;

/**
 * Generic singly-linked node shared across linked structures
 * ({@link SinglyLinkedList}, {@link Stack}, {@link Queue}).
 *
 * @param <T> element type
 */
public class Node<T> {

    private T value;
    private Node<T> next;

    public Node(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }
}
