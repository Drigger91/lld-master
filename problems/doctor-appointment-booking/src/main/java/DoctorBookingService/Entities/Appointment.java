package DoctorBookingService.Entities;

import java.util.Date;

public class Appointment {
    private final int doctorId;
    private final int patientId;
    private final int appointmentId;
    private final Date dateOfAppointment;
    private final Slot timeSlot;
    private final double fees;

    public Appointment(int doctorId, int patientId, int appointmentId, Date dateOfAppointment, Slot timeSlot, double fees) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.fees = fees;
        this.appointmentId = appointmentId;
        this.dateOfAppointment = dateOfAppointment;
        this.timeSlot = timeSlot;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public Date getDateOfAppointment() {
        return dateOfAppointment;
    }

    public Slot getTimeSlot() {
        return timeSlot;
    }

    public double getFees() {
        return fees;
    }

    @Override
    public String toString() {
        return "Appointment#" + appointmentId + "[doctor=" + doctorId + ", patient=" + patientId
                + ", slot=" + timeSlot + ", fees=" + fees + "]";
    }
}
