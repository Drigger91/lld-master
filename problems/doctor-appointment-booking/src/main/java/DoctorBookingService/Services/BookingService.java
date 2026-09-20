package DoctorBookingService.Services;

import DoctorBookingService.Entities.Appointment;
import DoctorBookingService.Entities.Doctor;
import DoctorBookingService.Entities.Patient;
import DoctorBookingService.Entities.Slot;
import DoctorBookingService.Entities.Speciality;
import DoctorBookingService.Environment;
import DoctorBookingService.Repositories.AppointmentRepository;
import DoctorBookingService.Repositories.DoctorRepository;
import DoctorBookingService.Repositories.PatientRepository;

import java.util.Date;
import java.util.List;

/** Facade over the repositories: registration, search, booking and cancellation. */
public class BookingService {
    private final DoctorRepository doctorRepository = new DoctorRepository();
    private final PatientRepository patientRepository = new PatientRepository();
    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    public Doctor registerDoctor(String name, String address, String mobileNumber, Speciality speciality, String city, double fees) {
        Doctor doctor = new Doctor(name, address, mobileNumber, speciality, city, fees);
        doctorRepository.saveDoctor(doctor, doctor.getId());
        return doctor;
    }

    public Patient registerPatient(String name, String mobileNumber) {
        Patient patient = new Patient(name, mobileNumber, Environment.patientId++);
        patientRepository.savePatient(patient);
        return patient;
    }

    public List<Doctor> findDoctors(Speciality speciality, Date date) {
        return doctorRepository.getAllDoctorsWithSpecializationAndDate(speciality, date);
    }

    /** Returns the appointment, or null if the slot is not free (the doctor prints the reason). */
    public Appointment bookAppointment(int doctorId, int patientId, Date date, Slot slot) {
        Doctor doctor = doctorRepository.getDoctor(doctorId);
        Patient patient = patientRepository.getPatient(patientId);
        if (doctor == null || patient == null) {
            System.out.println("Unknown doctor or patient");
            return null;
        }
        if (!doctor.bookAppointment(slot, date)) {
            return null;
        }
        Appointment appointment = new Appointment(doctorId, patientId, Environment.appointmentId++, date, slot, doctor.getFees());
        appointmentRepository.save(appointment);
        return appointment;
    }

    public boolean cancelAppointment(int appointmentId) {
        Appointment appointment = appointmentRepository.get(appointmentId);
        if (appointment == null) {
            System.out.println("No appointment with id " + appointmentId);
            return false;
        }
        Doctor doctor = doctorRepository.getDoctor(appointment.getDoctorId());
        if (!doctor.cancelAppointment(appointment.getTimeSlot(), appointment.getDateOfAppointment())) {
            return false;
        }
        appointmentRepository.delete(appointmentId);
        return true;
    }
}
