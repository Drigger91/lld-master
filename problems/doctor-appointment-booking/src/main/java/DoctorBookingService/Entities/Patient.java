package DoctorBookingService.Entities;

public class Patient {
    private final String name;
    private final String mobileNumber;
    private final int id;

    public Patient(String name, String mobileNumber, int id) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.id = id;
    }
}
