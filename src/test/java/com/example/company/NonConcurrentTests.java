package com.example.company;

import com.example.company.struct.SeatHold;
import com.example.company.struct.SeatingPreference;
import com.example.company.struct.TicketService;

import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class NonConcurrentTests {

    /**
     * Creates a venue with each of the constructors, and verifies the number of
     * available seats is as expected
     */
    @Test
    public void testVenueCreationReturnsCorrectNumSeatsAvailable() {
        String failureMessage = "Number of available seats different than "
            + "expected";

        boolean[][] seatingConfiguration =
            new boolean[][]{
                {true, true, false, true},
                {false, true, true},
                {true, true, true, true},
                {false, false, false, false}
            };
        boolean[][] seatingConfiguration2 =
            new boolean[][]{
                {true, true, true, true},
                {true, true, true},
                {true, true, true, true},
                {true, true, true}
            };
        TicketService ts1 = new Venue(seatingConfiguration);
        TicketService ts2 = new Venue(seatingConfiguration2);
        TicketService ts3 = new Venue(7, 9);
        TicketService ts4 = new Venue(8);

        assertEquals(failureMessage, 9, ts1.numSeatsAvailable());
        assertEquals(failureMessage, 14, ts2.numSeatsAvailable());
        assertEquals(failureMessage, 63, ts3.numSeatsAvailable());
        assertEquals(failureMessage, 64, ts4.numSeatsAvailable());

        // Test odd seat numbers
        TicketService ts5 = new Venue(0);
        TicketService ts6 = new Venue(1);
        TicketService ts7 = new Venue(-8, 4);

        assertEquals(failureMessage, 0, ts5.numSeatsAvailable());
        assertEquals(failureMessage, 1, ts6.numSeatsAvailable());
        assertEquals(failureMessage, 0, ts7.numSeatsAvailable());
    }

    @Test
    public void testHoldingSeatsUpdatesAvailableSeats() {
        TicketService ts = new Venue(10);

        SeatHold sh = ts.findAndHoldSeats(17, "fake@email.com");
        assertNotNull("Expected to find seats", sh);

        assertEquals("Expected number of available seats to decrease", 83,
            ts.numSeatsAvailable());
    }

    @Test
    public void testPartialSeatHoldsFail() {
        TicketService ts1 = new Venue(1);

        SeatHold sh1 = ts1.findAndHoldSeats(2, "fake@email.com");
        assertNull("Expected not to find seats", sh1);

        TicketService ts2 = new Venue(5);
        SeatHold sh2 = ts2.findAndHoldSeats(20, "fake@email.com");
        SeatHold sh3 = ts2.findAndHoldSeats(10, "fake2@email.com");

        assertNotNull("Expected to find seats", sh2);
        assertNull("Expected not to find seats", sh3);
    }

    @Test
    public void testSeatHoldIdsAreUniqueForDifferentCustomers() {
        TicketService ts = new Venue(10);

        SeatHold sh1 = ts.findAndHoldSeats(5, "fake@email.com");
        SeatHold sh2 = ts.findAndHoldSeats(7, "fake2@email.com");
        SeatHold sh3 = ts.findAndHoldSeats(3, "fake2@email.com");

        assertNotEquals("Expected SeatHold ids to differ", sh1.getId(),
            sh2.getId());
        assertEquals("Expected SeatHold ids to be the same", sh2.getId(),
            sh3.getId());
    }

    @Test
    public void testAddingAdditionalSeatsAddsSeats() {
        VenueSeatHold venueSeatHold =
            new VenueSeatHold(
                0, Stream.of(1, 2, 3, 4, 5, 6, 7).collect(Collectors.toSet()),
                "fake@email.com");

        // Reserve additional seats
        venueSeatHold.reserveAdditionalSeats(
            Stream.of(6, 7, 8).collect(Collectors.toSet()));

        // Get the new reserved seats
        Set<Integer> seats = venueSeatHold.getReservedSeats();
        // The set containing the seats the set above is expected to contain
        Set<Integer> expectedSeats = Stream.of(1, 2, 3, 4, 5, 6, 7, 8)
            .collect(Collectors.toSet());

        assertEquals("Expected Sets to be equal", expectedSeats, seats);
    }

    @Test
    public void testSeatHoldExpires() {
        TicketService ts = new Venue(10);

        SeatHold sh = ts.findAndHoldSeats(10, "fake@email.com");

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Thread interrupted");
        }

        String result = ts.reserveSeats(sh.getId(), "fake@email.com");

        assertNull("Expected reserveSeats to fail", result);
    }

    @Test
    public void testSeatsChangeAvailabilityWhileSeatHoldIsHeld() {
        TicketService ts = new Venue(10);

        ts.findAndHoldSeats(15, "fake@email.com");

        assertEquals(
            "Expected seats to become unavailable after SeatHold given", 85,
            ts.numSeatsAvailable());

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Thread interrupted");
        }

        assertEquals(
            "Expected seats to become available after SeatHold expired", 100,
            ts.numSeatsAvailable());
    }

    @Test
    public void testHoldingThenImmediatelyReservingSeatsSucceeds() {
        TicketService ts = new Venue(10);

        SeatHold sh = ts.findAndHoldSeats(15, "fake@email.com");
        String confirmationCode = ts.reserveSeats(sh.getId(), "fake@email.com");

        assertNotNull("Expected reservation to succeed", confirmationCode);
    }

    @Test
    public void testSeatingPreferenceGetsBestSeats() {
        boolean[][] seatingConfiguration =
            new boolean[][]{
                {true, false, true, true, false, false},
                {false, true, true, true, false, true},
                {false, false, true, true, true, false},
                {false, true, true, true, true, false},
                {false, true, true, true, false, true},
            };

        Venue v1 = new Venue(seatingConfiguration);
        Venue v2 = new Venue(seatingConfiguration);
        Venue v3 = new Venue(seatingConfiguration);
        Venue v4 = new Venue(seatingConfiguration);

        SeatHold<Integer> sh1 =
            v1.findAndHoldSeats(4, "fake@email.com",
                SeatingPreference.CLOSEST_TO_FRONT);
        SeatHold<Integer> sh2 =
            v2.findAndHoldSeats(4, "fake@email.com",
                SeatingPreference.CLOSEST_TO_BACK);
        SeatHold<Integer> sh3 =
            v3.findAndHoldSeats(4, "fake@email.com",
                SeatingPreference.CLOSEST_TO_CENTER);
        SeatHold<Integer> sh4 =
            v4.findAndHoldSeats(4, "fake@email.com",
                SeatingPreference.CLOSEST_TOGETHER);

        Set<Integer> reservedSeats1 = sh1.getReservedSeats();
        Set<Integer> reservedSeats2 = sh2.getReservedSeats();
        Set<Integer> reservedSeats3 = sh3.getReservedSeats();
        Set<Integer> reservedSeats4 = sh4.getReservedSeats();

        // For closest to front, all available seats in the bottom row should
        // be selected
        // For closest to back, the top three available seats and the
        // second-from-the-left middle seat
        // should be selected
        // For closest to center, all available middle row seats and the last
        // 2nd row seat should be
        // selected
        // For closest together, all seats in the second from the bottom row
        // should be selected
        Set<Integer> expectedSeats1 = Stream.of(25, 26, 27, 29)
            .collect(Collectors.toSet());
        Set<Integer> expectedSeats2 = Stream.of(0, 2, 3, 7)
            .collect(Collectors.toSet());
        Set<Integer> expectedSeats3 = Stream.of(11, 14, 15, 16)
            .collect(Collectors.toSet());
        Set<Integer> expectedSeats4 = Stream.of(19, 20, 21, 22)
            .collect(Collectors.toSet());

        assertEquals("Unexpected Seating Order for CLOSEST_TO_FRONT",
            expectedSeats1,
            reservedSeats1);
        assertEquals("Unexpected Seating Order for CLOSEST_TO_BACK",
            expectedSeats2,
            reservedSeats2);
        assertEquals("Unexpected Seating Order for CLOSEST_TO_CENTER",
            expectedSeats3,
            reservedSeats3);
        assertEquals("Unexpected Seating Order for CLOSEST_TOGETHER",
            expectedSeats4,
            reservedSeats4);
    }
}
