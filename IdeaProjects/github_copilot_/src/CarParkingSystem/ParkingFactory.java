package CarParkingSystem;

import java.util.Calendar;
import java.util.Date;

public class ParkingFactory {
    public Car getCar(int entryTime, CarType carType) {
        Car car = null;
        switch (carType) {
            case SUV:
                car = new MySuv(entryTime, carType);
                break;
            case HATCHBACK:
                car = new MyHatchBack(entryTime, carType);
                break;

        }
        return car;
    }

    public void parkCar(Car car, ParkingSpace parkingSpace) {
        if (car.getCarType() == CarType.SUV) {
            if (parkingSpace.getSuvParkingSpace() > 0) {
                System.out.println("Parking SUV");
                parkingSpace.suvParkingSpace -= 1;
                return;
            }
        } else {
            if (parkingSpace.hatchBackParkingSpace > 0) {
                System.out.println("Parking hatchBack in hatchBackParking space");
                parkingSpace.hatchBackParkingSpace -= 1;
                return;
            } else if (parkingSpace.suvParkingSpace > 0) {
                System.out.println("Hatchback space full, parking in SUV space");
                parkingSpace.suvParkingSpace -= 1;
                car.setIsUsingSuvParkingSpace(true);
                return;
            }
        }
        System.out.printf("No %s parking available%n", car.getCarType());
    }

    public int checkoutAndGetPrice(Car car, ParkingSpace parkingSpace, int endTime) {
        if (car.getCarType() == CarType.SUV || car.isUsingSuvParkingSpace()) {
            parkingSpace.suvParkingSpace += 1;
        } else {
            parkingSpace.hatchBackParkingSpace -= 1;
        }
        return (endTime - car.getEntryTime()) * car.getCarType().getRateOfParking();
    }
}
