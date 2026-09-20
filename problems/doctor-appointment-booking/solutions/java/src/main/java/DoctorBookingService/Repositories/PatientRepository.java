package DoctorBookingService.Repositories;

import DoctorBookingService.Entities.Patient;

import java.util.HashMap;
import java.util.Map;

public class PatientRepository {
    private final Map<Integer, Patient> db = new HashMap<>();

    public void savePatient(Patient patient) {
        db.put(patient.getId(), patient);
    }

    public Patient getPatient(int patientId) {
        return db.get(patientId);
    }
}
