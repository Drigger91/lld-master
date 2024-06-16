package LRUCache;

public class DoublyLinkedList {
    DoublyLinkedList prev;
    DoublyLinkedList next;
    int key;
    int val;

    /**
     * Instantiates a new Dll.
     *
     * @param key the key
     * @param val the val
     */
    public DoublyLinkedList(int key, int val) {
        this.key = key;
        this.val = val;
        this.prev = null;
        this.next = null;
    }

    /**
     * Update value.
     *
     * @param val the val
     */
    public void updateValue(int val) {
        this.val = val;
    }
}

