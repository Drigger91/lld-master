package CarParkingSystem;

public class Main {
    public static void main(String[] args) {
        ParkingFactory parkingFactory = new ParkingFactory();
        Car suv1 = parkingFactory.getCar(1, CarType.SUV);
        Car hatchback1 = parkingFactory.getCar(1, CarType.HATCHBACK);
        ParkingSpace space = new ParkingSpace(1, 1);
        Car hatchback2 = parkingFactory.getCar(2, CarType.HATCHBACK);
        parkingFactory.parkCar(suv1, space);
        parkingFactory.parkCar(hatchback1, space);
        parkingFactory.parkCar(hatchback2, space);
        int rateForSuv1 = parkingFactory.checkoutAndGetPrice(suv1, space, 2);
        System.out.println("Rate of parking for suv1: " + rateForSuv1);
        parkingFactory.parkCar(hatchback2, space);
    }
}
