package entities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

@Data
public class ScheduledTask {
    private final String topic;
    private final String key;
    @NonNull
    private final ObjectNode message;

    public ScheduledTask(String topic, ObjectNode message) {
        if (Objects.isNull(message)) {
            throw new RuntimeException("Message cannot be null");
        }
        this.key = UUID.randomUUID().toString();
        this.message = message;
        this.topic = topic;
    }
    public ScheduledTask(String topic, String key, ObjectNode message) {
        if (Objects.isNull(message)) {
            throw new RuntimeException("Message cannot be null");
        }
        this.key = key;
        this.message = message;
        this.topic = topic;
    }

    @Override
    public String toString() {
        return String.format("topic: %s | key: %s | message: %s ", this.topic, this.key, this.message);
    }
}
