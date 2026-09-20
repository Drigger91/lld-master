import vehicle.Car;
import vehicle.Motorcycle;
import vehicle.Truck;
import vehicle.Vehicle;

public class ParkingLotMain {
    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.getInstance();
        parkingLot.addLevel(new Level(1, 10)); // 2 motorcycle, 5 car, 3 truck spots
        parkingLot.addLevel(new Level(2, 5));  // 1 motorcycle, 2 car, 2 truck spots

        Vehicle car = new Car("ABC123");
        Vehicle truck = new Truck("XYZ789");
        Vehicle motorcycle = new Motorcycle("M1234");

        // Park vehicles
        System.out.println("Park car: " + parkingLot.parkVehicle(car));
        System.out.println("Park truck: " + parkingLot.parkVehicle(truck));
        System.out.println("Park motorcycle: " + parkingLot.parkVehicle(motorcycle));

        // Display availability
        parkingLot.displayAvailability();

        // Unpark vehicle
        System.out.println("Unpark motorcycle: " + parkingLot.unparkVehicle(motorcycle));
        // Unparking a vehicle that is not in the lot is a no-op
        System.out.println("Unpark unknown car: " + parkingLot.unparkVehicle(new Car("NOTHERE")));

        // Display updated availability
        parkingLot.displayAvailability();

        // Fill every motorcycle spot (3 across both levels), then one more must be rejected
        System.out.println("Park M2: " + parkingLot.parkVehicle(new Motorcycle("M2")));
        System.out.println("Park M3: " + parkingLot.parkVehicle(new Motorcycle("M3")));
        System.out.println("Park M4: " + parkingLot.parkVehicle(new Motorcycle("M4")));
        System.out.println("Park M5 (lot full for motorcycles): " + parkingLot.parkVehicle(new Motorcycle("M5")));
    }
}
