import vehicle.Vehicle;
import vehicle.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class Level {
    // Share of spots reserved per vehicle type; the remainder are truck spots.
    private static final double MOTORCYCLE_SHARE = 0.20;
    private static final double CAR_SHARE = 0.50;

    private final int floor;
    private final List<ParkingSpot> parkingSpots;

    public Level(int floor, int totalSpots) {
        this.floor = floor;
        parkingSpots = new ArrayList<>(totalSpots);
        int motorcycleSpots = (int) (totalSpots * MOTORCYCLE_SHARE);
        int carSpots = (int) (totalSpots * CAR_SHARE);
        for (int i = 0; i < totalSpots; i++) {
            VehicleType type = i < motorcycleSpots ? VehicleType.MOTORCYCLE
                    : i < motorcycleSpots + carSpots ? VehicleType.CAR
                    : VehicleType.TRUCK;
            parkingSpots.add(new ParkingSpot(i, type));
        }
    }

    public synchronized boolean parkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isAvailable() && spot.getVehicleType() == vehicle.getType()) {
                spot.parkVehicle(vehicle);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean unparkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : parkingSpots) {
            if (!spot.isAvailable() && spot.getParkedVehicle().equals(vehicle)) {
                spot.unparkVehicle();
                return true;
            }
        }
        return false;
    }

    public void displayAvailability() {
        System.out.println("Level " + floor + " Availability:");
        for (ParkingSpot spot : parkingSpots) {
            System.out.println("Spot " + spot.getSpotNumber() + " (" + spot.getVehicleType() + "): "
                    + (spot.isAvailable() ? "Available" : "Occupied by " + spot.getParkedVehicle().getLicensePlate()));
        }
        System.out.println();
    }
}
