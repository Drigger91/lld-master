import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficController {
    private static TrafficController instance;
    private final Map<String, Road> roads;
    private final List<Thread> signalThreads;

    private TrafficController() {
        roads = new HashMap<>();
        signalThreads = new ArrayList<>();
    }

    public static synchronized TrafficController getInstance() {
        if (instance == null) {
            instance = new TrafficController();
        }
        return instance;
    }

    public void addRoad(Road road) {
        roads.put(road.getId(), road);
    }

    public void removeRoad(String roadId) {
        roads.remove(roadId);
    }

    public Road getRoad(String roadId) {
        return roads.get(roadId);
    }

    // Each road's light cycles RED -> GREEN -> YELLOW -> RED on its own thread until stopTrafficControl().
    public synchronized void startTrafficControl() {
        if (!signalThreads.isEmpty()) {
            return; // already running
        }
        for (Road road : roads.values()) {
            TrafficLight trafficLight = road.getTrafficLight();
            Thread thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(trafficLight.getRedDuration());
                        trafficLight.changeSignal(Signal.GREEN);
                        Thread.sleep(trafficLight.getGreenDuration());
                        trafficLight.changeSignal(Signal.YELLOW);
                        Thread.sleep(trafficLight.getYellowDuration());
                        trafficLight.changeSignal(Signal.RED);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "signal-" + road.getId());
            thread.setDaemon(true);
            signalThreads.add(thread);
            thread.start();
        }
    }

    public synchronized void stopTrafficControl() {
        for (Thread thread : signalThreads) {
            thread.interrupt();
        }
        for (Thread thread : signalThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        signalThreads.clear();
    }

    public void handleEmergency(String roadId) {
        Road road = roads.get(roadId);
        if (road != null) {
            TrafficLight trafficLight = road.getTrafficLight();
            trafficLight.changeSignal(Signal.GREEN);
            // Perform emergency handling logic
            // ...
        }
    }
}
