package CarParkingSystem;

public class ParkingSpace {
    int suvParkingSpace;
    int hatchBackParkingSpace;
    public ParkingSpace(int suvParkingSpace, int hatchBackParkingSpace) {
        this.suvParkingSpace = suvParkingSpace;
        this.hatchBackParkingSpace = hatchBackParkingSpace;
    }
    public int getTotalCapacity() {
        return hatchBackParkingSpace + suvParkingSpace;
    }
    public int getSuvParkingSpace() {
        return suvParkingSpace;
    }
}
