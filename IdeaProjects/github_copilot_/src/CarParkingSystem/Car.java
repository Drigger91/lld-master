package CarParkingSystem;

public abstract class Car {
    private final int entryTime;
    private final CarType carType;
    private boolean isUsingSuvParkingSpace;
    public Car(int entryTime, CarType carType) {
        this.carType = carType;
        this.entryTime = entryTime;
    }
    public int getEntryTime() {
        return this.entryTime;
    }
    public CarType getCarType() {
        return this.carType;
    }
    public boolean isUsingSuvParkingSpace() {
        return this.isUsingSuvParkingSpace;
    }
    public void setIsUsingSuvParkingSpace(boolean isUsingSuvParkingSpace) {
        this.isUsingSuvParkingSpace = isUsingSuvParkingSpace;
    }
    public int getParkingRates() {
        return this.carType.getRateOfParking();
    }
}
