import booking.Booking;
import flight.Flight;
import passenger.Passenger;
import payment.Payment;
import seat.Seat;
import seat.SeatType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AirlineManagementSystemMain {
    public static void main(String[] args) {
        AirlineManagementSystem airline = new AirlineManagementSystem();

        // Fleet and schedule
        airline.addAircraft(new Aircraft("N12345", "Boeing 737", 180));
        LocalDate travelDate = LocalDate.now().plusDays(7);
        Flight f1 = new Flight("AA101", "NYC", "LAX", travelDate.atTime(9, 0), travelDate.atTime(12, 30));
        Flight f2 = new Flight("AA202", "NYC", "SFO", travelDate.atTime(14, 0), travelDate.atTime(17, 45));
        Flight f3 = new Flight("AA303", "NYC", "LAX", travelDate.plusDays(1).atTime(9, 0), travelDate.plusDays(1).atTime(12, 30));
        airline.addFlight(f1);
        airline.addFlight(f2);
        airline.addFlight(f3);

        // Search: case-insensitive on airports, exact on departure date
        List<Flight> results = airline.searchFlights("nyc", "lax", travelDate);
        System.out.println("Flights NYC -> LAX on " + travelDate + ":");
        results.forEach(f -> System.out.println("  " + f.getFlightNumber() + " departs " + f.getDepartureTime()));

        // Edge: no flights on a date with no departures
        List<Flight> none = airline.searchFlights("NYC", "LAX", travelDate.plusDays(30));
        System.out.println("Flights NYC -> LAX 30 days later: " + none.size());

        // Book a seat for a passenger
        Passenger alice = new Passenger("P1", "Alice", "alice@example.com", "555-0100");
        Seat seat12A = new Seat("12A", SeatType.ECONOMY);
        seat12A.reserve();
        Booking booking = airline.bookFlight(results.get(0), alice, seat12A, 250.0);
        System.out.println("Booking " + booking.getBookingNumber() + " for " + booking.getPassenger().getName()
                + " on " + booking.getFlight().getFlightNumber() + " seat " + booking.getSeat().getSeatNumber()
                + " -> " + booking.getStatus() + ", seat " + seat12A.getStatus());

        // Pay for it
        Payment payment = new Payment("PAY1", "CREDIT_CARD", booking.getPrice());
        System.out.println("Payment " + payment.getPaymentId() + " before: " + payment.getStatus());
        airline.processPayment(payment);
        System.out.println("Payment " + payment.getPaymentId() + " after: " + payment.getStatus());

        // Cancel the booking and release the seat
        airline.cancelBooking(booking.getBookingNumber());
        seat12A.release();
        System.out.println("After cancel: booking " + booking.getStatus() + ", seat " + seat12A.getStatus());

        // Edge: cancelling an unknown booking number is a no-op
        airline.cancelBooking("BKG-DOES-NOT-EXIST");
        System.out.println("Cancelling unknown booking: no error");
    }
}
