package CarParkingSystem;

public enum CarType {
    SUV(20),
    HATCHBACK(10);
    private final int rateOfParking;
    CarType(int rateOfParking) {
        this.rateOfParking = rateOfParking;
    }
    public int getRateOfParking() {
        return this.rateOfParking;
    }
}
