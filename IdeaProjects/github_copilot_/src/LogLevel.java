public enum LogLevel{
    DEBUG("debug", 1),
    INFO("warn", 2),
    WARN("info", 3),
    ERROR("error", 4);

    private final String name;
    private final int priority;

    LogLevel(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }
}

