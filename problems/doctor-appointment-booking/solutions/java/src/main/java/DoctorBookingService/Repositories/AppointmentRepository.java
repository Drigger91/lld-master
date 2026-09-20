package DoctorBookingService.Repositories;

import DoctorBookingService.Entities.Appointment;

import java.util.HashMap;
import java.util.Map;

public class AppointmentRepository {
    private final Map<Integer, Appointment> db = new HashMap<>();

    public void save(Appointment appointment) {
        db.put(appointment.getAppointmentId(), appointment);
    }

    public Appointment get(int appointmentId) {
        return db.get(appointmentId);
    }

    public Appointment delete(int appointmentId) {
        return db.remove(appointmentId);
    }
}
