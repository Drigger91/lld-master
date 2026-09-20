import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.ScheduledTask;

public class Main {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Scheduled Queue init");

        // Task with an explicit key
        ObjectNode payload = mapper.createObjectNode().put("action", "send-email").put("to", "user@example.com");
        ScheduledTask task = new ScheduledTask("notifications", "email-42", payload);
        System.out.println(task);

        // Task with an auto-generated key
        ScheduledTask autoKeyed = new ScheduledTask("notifications", mapper.createObjectNode().put("action", "send-sms"));
        System.out.println(autoKeyed);
        System.out.println("Auto-generated key present: " + (autoKeyed.getKey() != null && !autoKeyed.getKey().isBlank()));

        // Validation: a task without a message is rejected
        try {
            new ScheduledTask("notifications", "bad", null);
            System.out.println("ERROR: null message was accepted");
        } catch (RuntimeException e) {
            System.out.println("Rejected task without message: " + e.getMessage());
        }
    }
}
