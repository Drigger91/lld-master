public class TrafficSignalSystemMain {
    public static void main(String[] args) throws InterruptedException {
        TrafficController trafficController = TrafficController.getInstance();

        // Create roads
        Road road1 = new Road("R1", "Main Street");
        Road road2 = new Road("R2", "Broadway");
        Road road3 = new Road("R3", "Park Avenue");
        Road road4 = new Road("R4", "Elm Street");

        // Create traffic lights (durations in ms; short so the demo finishes quickly)
        TrafficLight trafficLight1 = new TrafficLight("TL1", 300, 100, 400);
        TrafficLight trafficLight2 = new TrafficLight("TL2", 300, 100, 400);
        TrafficLight trafficLight3 = new TrafficLight("TL3", 300, 100, 400);
        TrafficLight trafficLight4 = new TrafficLight("TL4", 300, 100, 400);

        // Assign traffic lights to roads
        road1.setTrafficLight(trafficLight1);
        road2.setTrafficLight(trafficLight2);
        road3.setTrafficLight(trafficLight3);
        road4.setTrafficLight(trafficLight4);

        // Add roads to the traffic controller
        trafficController.addRoad(road1);
        trafficController.addRoad(road2);
        trafficController.addRoad(road3);
        trafficController.addRoad(road4);

        System.out.println("Initial signal on R1: " + trafficLight1.getCurrentSignal());

        // Start traffic control and let the lights cycle for a while
        trafficController.startTrafficControl();
        Thread.sleep(1000);

        // Simulate an emergency on a specific road: its light is forced GREEN immediately
        System.out.println("--- Emergency on R2 ---");
        trafficController.handleEmergency("R2");
        System.out.println("R2 signal after emergency: " + trafficLight2.getCurrentSignal());

        // Emergency on an unknown road is ignored
        trafficController.handleEmergency("R99");

        Thread.sleep(600);
        trafficController.stopTrafficControl();
        System.out.println("Traffic control stopped.");
    }
}
