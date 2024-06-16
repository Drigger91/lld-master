package LRUCache;

public class Main {
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2);
        cache.put(2, 1);
        System.out.println("GET key : 2 -> " + cache.get(2));
        cache.put(1, 1);
        System.out.println("Set 1 = 1");
        cache.put(2,3);
        System.out.println("Set 2 = 3");
        cache.put(4,1);
        System.out.println("Set 4 = 1");
        System.out.println("GET key : 2 -> " + cache.get(1));
        System.out.println("GET key : 3 -> " + cache.get(2));
    }
}
