# Doctor Appointment Booking

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Repository, Facade (service), Value object (Slot) | slot inventory per day, synchronized booking, value equality, search by speciality |

## Problem statement
Design a system that lets patients book appointments with doctors. Doctors register with a speciality, city and consultation fee and publish their available time slots for a given day. A patient searches doctors by speciality who still have a free slot on a date, books a slot, and can later cancel the appointment so the slot becomes available again. The same slot must never be booked twice.

## Functional requirements
1. Register doctors (name, address, mobile, `Speciality`, city, fee) and patients (name, mobile); ids are generated.
2. A doctor publishes slots for a date: a start time, an end time and a slot duration produce consecutive `Slot`s. Slots for a date can be set only once.
3. List a doctor's free slots for a date.
4. Search doctors by speciality that have at least one free slot on a date.
5. Book a slot for a patient: the slot must exist in the doctor's free list for that date and not already be booked. Booking creates an `Appointment` with the doctor's fee.
6. Reject a booking of an already-booked slot or of a slot the doctor never offered.
7. Cancel an appointment by id: the slot returns to the free list; cancelling an unknown appointment fails.

## Non-functional requirements
- In-memory repositories (`HashMap`s); ids are static counters in `Environment`.
- `Doctor.bookAppointment` and `cancelAppointment` are `synchronized` on the doctor, so two patients racing for the same slot of one doctor are serialised.
- Dates are `java.util.Date` keys (compared by millisecond value) and slot times are `LocalDateTime`; a `Slot` is equal to another when start time and duration match.
- Fees are captured on the appointment at booking time.

## Constraints
- Exactly four specialities: `GENERAL_PHYSICIAN`, `OPTHALMOLOGIST`, `ORTHOPAEDIAC`, `DENTIST` (`Speciality`); search matches one speciality exactly and never filters by city.
- Slot duration is a positive whole number of minutes and `endTime - startTime` must be an exact multiple of it: `addSlotsForDate` walks `while (!start.isEqual(end))` and never terminates otherwise.
- Once published, each slot of a (doctor, date) is in exactly one of `availableSlots` or `bookedSlots`, never both and never neither, so a slot holds at most one appointment.
- Every `Appointment` corresponds to exactly one booked slot; cancelling frees the slot and deletes the appointment, so `AppointmentRepository` only ever holds live bookings.
- Ids are sequential `int`s starting at 1 with independent counters for doctors, patients and appointments.
- No real clock: the caller supplies the `Date` and the slot `LocalDateTime`s; nothing rejects a slot in the past or a booking `Date` that differs from the slot's own day.
- Small scale: tens of doctors and a handful of slots per day; single JVM, no persistence.

## Clarifying questions to ask
- Can a doctor edit slots after publishing them? — No, `addSlotsForDate` is one-shot per date.
- What makes two slots the same? — Same start time and duration (value equality), so callers can construct a `Slot` rather than look one up.
- Can a patient hold two appointments at the same time with different doctors? — Not prevented; the design is doctor-centric.
- What happens to the fee if the doctor changes it later? — The appointment keeps the fee at booking time.
- Concurrency scope? — Per doctor via `synchronized`; no global lock.
- Notifications, payments, reschedule? — Out of scope.

## Core entities
- `Doctor` — profile plus two per-date maps: `availableSlots` and `bookedSlots`; `addSlotsForDate`, `bookAppointment`, `cancelAppointment`, `getAvailableSlots`.
- `Patient` — id, name, mobile.
- `Slot` — value object: start time, duration, derived end time; `equals`/`hashCode` on start + duration.
- `Speciality` — enum used for search.
- `Appointment` — immutable record of doctorId, patientId, appointmentId, date, `Slot`, fee.
- `DoctorRepository` / `PatientRepository` / `AppointmentRepository` — in-memory stores; `DoctorRepository` filters by speciality and free slot on a date.
- `BookingService` — facade: register, find doctors, book, cancel.
- `Environment` — static id counters.

## Design hints
- **Doctor owns the slot inventory.** Moving a slot between `availableSlots` and `bookedSlots` inside one `synchronized` method is what makes double booking impossible for that doctor; the service never touches the lists directly.
- **Slot as a value object.** Without `equals`/`hashCode`, `List.contains(slot)` is identity-based and a caller can never match a published slot. Interviewers probe this.
- **Repository + service split.** Repositories only store and query; `BookingService` composes "check doctor and patient exist -> reserve slot -> persist appointment" and the inverse for cancel. Keep the two steps in this order so a failed reservation never leaves an orphan appointment.
- **Search** is a linear scan by speciality then a filter by free slot on the date; an index `Map<Speciality, List<Doctor>>` is the obvious upgrade.
- Common mistakes: writing the free list into the booked map (and vice versa) when swapping; mutable `Date` as a map key; generating slots with `while (!start.equals(end))`, which loops forever if the end is not a multiple of the duration; returning the internal slot list and letting callers mutate it.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :doctor-appointment-booking compile exec:java` |
