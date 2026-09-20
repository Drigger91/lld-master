# Test cases — Doctor Appointment Booking

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register doctor and patient | `service.registerDoctor("Dr. Rao", addr, mobile, DENTIST, "Bangalore", 500)`; `service.registerPatient("Asha", mobile)` | Both get sequential ids and are retrievable via their repositories |
| H2 ✅ | Publish slots | `drRao.addSlotsForDate(day, 30, 10:00, 11:00, fee)`; `drRao.getAvailableSlotsForTheDay(day)` | Prints `10:00-10:30` and `10:30-11:00` |
| H3 ✅ | Search by speciality and date | `service.findDoctors(DENTIST, day)` | Only doctors with a free slot that day (Dr. Rao, not Dr. Mehta who has no slots) |
| H4 ✅ | Book a free slot | `service.bookAppointment(drRao.getId(), asha.getId(), day, new Slot(10:00, 30))` | Returns `Appointment` with id 1, fee 500; slot leaves the free list |
| H5 ✅ | Book a second, different slot | `bookAppointment(..., ravi.getId(), day, new Slot(10:30, 30))` | Succeeds; free list is now empty |
| H6 ✅ | Cancel | `service.cancelAppointment(a1.getAppointmentId())` | Returns true; `10:00-10:30` is back in the free list |
| H7 ✅ | Re-book a freed slot | After H6, `bookAppointment(..., ravi.getId(), day, new Slot(10:00, 30))` | Succeeds with a new appointment id |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | Doctor fully booked drops out of search | After H5, `service.findDoctors(DENTIST, day)` | Dr. Rao no longer returned (0 dentists) |
| E2 ✅ | Slots cannot be re-published for a date | `drRao.addSlotsForDate(day, 30, 10:00, 12:00, fee)` after H2 | Prints "Cannot modify slots once created"; original slots unchanged |
| E3 ✅ | Slot value equality | Book with a freshly constructed `new Slot(10:00, 30)` rather than the doctor's instance | Matches the published slot |
| E4 ⬜ | Different duration is a different slot | Publish 30-min slots, book `new Slot(10:00, 15)` | Rejected: slot not offered |
| E5 ⬜ | Same time on another date | Book `new Slot(10:00, 30)` on a date with no slots published | Rejected |
| E6 ⬜ | Freed slot ordering | Cancel the 10:00 booking while 10:30 is free | Free list is sorted by start time |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Double booking | Book `Slot(10:00, 30)` twice for the same doctor and date | Second call prints "Slot not available", returns null; no appointment created |
| X2 ✅ | Slot the doctor never offered | `bookAppointment(..., new Slot(11:00, 30))` when slots end at 11:00 | Returns null |
| X3 ✅ | Cancel twice | `cancelAppointment(id)` again after H6 | Prints "No appointment with id", returns false |
| X4 ✅ | Unknown doctor | `bookAppointment(99, patientId, day, slot)` | Prints "Unknown doctor or patient", returns null |
| X5 ⬜ | Unknown patient | `bookAppointment(doctorId, 99, day, slot)` | Returns null |
| X6 ⬜ | End time not a multiple of duration | `addSlotsForDate(day, 30, 10:00, 10:45, fee)` | Current code loops forever; should reject or stop at the last full slot |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two patients race for one slot | Two threads call `bookAppointment` for the same doctor/date/slot | Exactly one succeeds (`Doctor.bookAppointment` is `synchronized`) |
| C2 ⬜ | Cancel racing with a booking of the same slot | Thread A cancels 10:00, thread B books 10:00 | Both serialise on the doctor; B succeeds only if it runs after A |

## Interviewer follow-ups / extensions
- How would you reschedule? — Cancel + book as one operation under the doctor's lock, or a `reschedule` on `BookingService` that rolls back on failure.
- How would you speed up search? — Index doctors by `Speciality` (and city) in `DoctorRepository`; keep a per-date free-slot count on `Doctor`.
- How would you prevent a patient from double-booking themselves? — Track the patient's appointments and check for overlapping `Slot`s before reserving.
- How would you handle recurring weekly availability? — Store a weekly template and materialise slots per date lazily.
- Ids and dates? — Replace static counters with an id generator and `java.util.Date` keys with `LocalDate`.
- Payments, reminders, waitlists? — Observer on booking/cancel events; a waitlist per doctor/date that is offered the freed slot.
