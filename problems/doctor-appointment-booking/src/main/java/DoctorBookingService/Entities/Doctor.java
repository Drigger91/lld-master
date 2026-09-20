package DoctorBookingService.Entities;

import DoctorBookingService.Environment;

import java.time.LocalDateTime;
import java.util.*;

public class Doctor {
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public String getCity() {
        return city;
    }

    public double getFees() {
        return fees;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    private final int id;
    private final String name;
    private final String address;
    private final Speciality speciality;
    private final String city;
    private final double fees;
    private final String mobileNumber;
    private final Map<Date, List<Slot>> availableSlots = new HashMap<>();
    private final Map<Date, List<Slot>> bookedSlots = new HashMap<>();

    public Doctor(String name, String address, String mobileNumber, Speciality speciality, String city, double fees) {
        this.id = Environment.doctorId++;
        this.name = name;
        this.address = address;
        this.mobileNumber = mobileNumber;
        this.speciality = speciality;
        this.city = city;
        this.fees = fees;
    }

    public synchronized boolean bookAppointment(Slot slot, Date dateOfAppointment) {
        List<Slot> unavailableSlots = bookedSlots.getOrDefault(dateOfAppointment, new ArrayList<>());
        List<Slot> freeSlots = availableSlots.getOrDefault(dateOfAppointment, new ArrayList<>());

        if (!freeSlots.contains(slot) || unavailableSlots.contains(slot)) {
            System.out.println("Slot not available for the time, please try a different slot");
            return false;
        }
        freeSlots.remove(slot);
        unavailableSlots.add(slot);
        bookedSlots.put(dateOfAppointment, freeSlots);
        availableSlots.put(dateOfAppointment, unavailableSlots);
        System.out.println("Appointment successfully booked for provided slot");
        return true;
    }

    public void getAvailableSlotsForTheDay(Date dateOfAppointment) {
        System.out.println("Available Slots: ");
        for (Slot slot : availableSlots.getOrDefault(dateOfAppointment, new ArrayList<>())) {
            System.out.println(slot.toString());
        }
    }

    public boolean checkIfFreeSlotAvailableOnDate(Date date) {
        return this.availableSlots.containsKey(date) && this.availableSlots.get(date).size() > 0;
    }

    public void addSlotsForDate(Date date, int duration, LocalDateTime startTime, LocalDateTime endTime, double fees) {
        if (availableSlots.containsKey(date) || bookedSlots.containsKey(date)) {
            System.out.println("Cannot modify slots once created");
            return;
        }
        List<Slot> slotList = new ArrayList<>();
        while (!startTime.isEqual(endTime)) {
            Slot slot = new Slot(startTime, duration);
            slotList.add(slot);
            startTime = startTime.plusMinutes(duration);
        }
        availableSlots.put(date, slotList);
    }
}
