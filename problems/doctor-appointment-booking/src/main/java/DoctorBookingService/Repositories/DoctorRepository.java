package DoctorBookingService.Repositories;

import DoctorBookingService.Entities.Doctor;
import DoctorBookingService.Entities.Speciality;

import java.util.*;
import java.util.stream.Collectors;

public class DoctorRepository {
    private final Map<Integer, Doctor> db = new HashMap<>();

    public void saveDoctor(Doctor doctor, int id) {
        db.put(id, doctor);
    }

    private List<Doctor> getAllDoctorsForSpecialization(Speciality specialization) {
        List<Doctor> doctors = new ArrayList<>();
        for (Map.Entry<Integer, Doctor> entry : db.entrySet()) {
            Doctor doctor = entry.getValue();
            if (doctor.getSpeciality().equals(specialization)) {
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    public List<Doctor> getAllDoctorsWithSpecializationAndDate(Speciality speciality, Date date) {
        List<Doctor> doctors = getAllDoctorsForSpecialization(speciality);
        return doctors
                .stream()
                .filter(doctor -> doctor.checkIfFreeSlotAvailableOnDate(date))
                .collect(Collectors.toList());
    }
}
