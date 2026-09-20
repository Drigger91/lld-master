package DocumentService;

public enum AccessLevel {
    OWNER(3),
    READ(1),
    NO_ACCESS(0),
    EDIT(2);
    private final int priority;
    AccessLevel(int priority) {
        this.priority = priority;
    }
    public int getPriority() {
        return this.priority;
    }
}
