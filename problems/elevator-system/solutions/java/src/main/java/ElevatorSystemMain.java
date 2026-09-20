public class ElevatorSystemMain {
    public static void main(String[] args) throws InterruptedException {
        ElevatorController controller = new ElevatorController(3, 5);

        controller.requestElevator(5, 10);
        controller.requestElevator(3, 7);
        controller.requestElevator(8, 2);
        controller.requestElevator(1, 9);

        // Wait until every elevator has served its queue before exiting the demo.
        controller.awaitIdle();
        System.out.println("All requests served.");
    }
}
