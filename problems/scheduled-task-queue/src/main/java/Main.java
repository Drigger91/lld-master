import com.fasterxml.jackson.databind.ObjectMapper;
import entities.ScheduledTask;

public class Main {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Scheduled Queue init");

        ScheduledTask task = new ScheduledTask("test", "test1", mapper.createObjectNode());

        System.out.println(task);


    }
}
