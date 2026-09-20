package DoctorBookingService;

import DoctorBookingService.Entities.Appointment;
import DoctorBookingService.Entities.Doctor;
import DoctorBookingService.Entities.Patient;
import DoctorBookingService.Entities.Slot;
import DoctorBookingService.Entities.Speciality;
import DoctorBookingService.Services.BookingService;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BookingService service = new BookingService();

        Doctor drRao = service.registerDoctor("Dr. Rao", "12 MG Road", "9000000001", Speciality.DENTIST, "Bangalore", 500);
        Doctor drMehta = service.registerDoctor("Dr. Mehta", "4 Park Street", "9000000002", Speciality.DENTIST, "Bangalore", 700);
        Doctor drSen = service.registerDoctor("Dr. Sen", "9 Lake View", "9000000003", Speciality.GENERAL_PHYSICIAN, "Kolkata", 300);
        Patient asha = service.registerPatient("Asha", "8000000001");
        Patient ravi = service.registerPatient("Ravi", "8000000002");

        Date day = new GregorianCalendar(2026, Calendar.SEPTEMBER, 22).getTime();
        LocalDateTime tenAm = LocalDateTime.of(2026, 9, 22, 10, 0);
        // Dr. Rao: 10:00-11:00 in 30-minute slots. Dr. Mehta has no slots that day.
        drRao.addSlotsForDate(day, 30, tenAm, tenAm.plusHours(1), drRao.getFees());
        drSen.addSlotsForDate(day, 15, tenAm, tenAm.plusMinutes(30), drSen.getFees());
        drRao.addSlotsForDate(day, 30, tenAm, tenAm.plusHours(2), drRao.getFees()); // ignored: already set

        System.out.println("Dentists free on " + day + ":");
        List<Doctor> dentists = service.findDoctors(Speciality.DENTIST, day);
        dentists.forEach(d -> System.out.println("  " + d.getName() + " (fee " + d.getFees() + ")"));

        drRao.getAvailableSlotsForTheDay(day);

        System.out.println("\nAsha books Dr. Rao 10:00");
        Appointment a1 = service.bookAppointment(drRao.getId(), asha.getId(), day, new Slot(tenAm, 30));
        System.out.println("  " + a1);

        System.out.println("Ravi tries the same slot");
        Appointment a2 = service.bookAppointment(drRao.getId(), ravi.getId(), day, new Slot(tenAm, 30));
        System.out.println("  booked? " + (a2 != null));

        System.out.println("Ravi books Dr. Rao 10:30");
        Appointment a3 = service.bookAppointment(drRao.getId(), ravi.getId(), day, new Slot(tenAm.plusMinutes(30), 30));
        System.out.println("  " + a3);

        System.out.println("Ravi tries a slot the doctor never offered (11:00)");
        Appointment a4 = service.bookAppointment(drRao.getId(), ravi.getId(), day, new Slot(tenAm.plusHours(1), 30));
        System.out.println("  booked? " + (a4 != null));

        drRao.getAvailableSlotsForTheDay(day);
        System.out.println("Dentists still free that day: " + service.findDoctors(Speciality.DENTIST, day).size());

        System.out.println("\nAsha cancels appointment " + a1.getAppointmentId());
        service.cancelAppointment(a1.getAppointmentId());
        drRao.getAvailableSlotsForTheDay(day);

        System.out.println("Cancelling it again");
        service.cancelAppointment(a1.getAppointmentId());

        System.out.println("Ravi now takes the freed 10:00 slot");
        Appointment a5 = service.bookAppointment(drRao.getId(), ravi.getId(), day, new Slot(tenAm, 30));
        System.out.println("  " + a5);

        System.out.println("Booking with an unknown doctor id");
        service.bookAppointment(99, ravi.getId(), day, new Slot(tenAm, 30));
    }
}
