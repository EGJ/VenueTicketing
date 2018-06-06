package com.example.company;

import static org.junit.Assert.*;

import com.example.company.struct.SeatHold;
import com.example.company.struct.TicketService;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NonConcurrentTests {

	/**
	 * Creates a venue with each of the constructors, and verifies the number of available seats is as expected
	 */
	@Test
	public void testNumSeatsAvailableReturnsCorrectValue(){
		String failureMessage = "Number of available seats different than expected";

		boolean[][] seatingConfiguration = new boolean[][]{
				{true , true , !true, true },
				{!true, true , true        },
				{true , true , true , true },
				{!true, !true, !true, !true}
		};
		TicketService ts1 = new Venue(seatingConfiguration);
		TicketService ts2 = new Venue(7, 9);
		TicketService ts3 = new Venue(8);

		assertEquals(failureMessage, 9 , ts1.numSeatsAvailable());
		assertEquals(failureMessage, 63, ts2.numSeatsAvailable());
		assertEquals(failureMessage, 64, ts3.numSeatsAvailable());

		//Test odd seat numbers
		TicketService ts4 = new Venue(0);
		TicketService ts5 = new Venue(1);
		TicketService v6 = new Venue(-8, 4);

		assertEquals(failureMessage, 0, ts4.numSeatsAvailable());
		assertEquals(failureMessage, 1, ts5.numSeatsAvailable());
		assertEquals(failureMessage, 0, v6.numSeatsAvailable());
	}

	@Test
	public void testHoldingSeatsUpdatesAvailableSeats(){
		TicketService ts = new Venue(10);

		SeatHold sh = ts.findAndHoldSeats(17, "fake@email.com");
		assertNotNull("Expected to find seats", sh);

		assertEquals("Expected number of available seats to decrease", 83, ts.numSeatsAvailable());
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
	public void testSeatHoldIdsAreUniqueForDifferentCustomers(){
		TicketService ts = new Venue(10);
		
		SeatHold sh1 = ts.findAndHoldSeats(5, "fake@email.com");
		SeatHold sh2 = ts.findAndHoldSeats(7, "fake2@email.com");
		SeatHold sh3 = ts.findAndHoldSeats(3, "fake2@email.com");

		assertNotEquals("Expected SeatHold ids to differ", sh1.getId(), sh2.getId());
		assertEquals("Expected SeatHold ids to be the same", sh2.getId(), sh3.getId());
	}

	@Test
	public void testAddingAdditionalSeatsAddsSeats(){
		VenueSeatHold venueSeatHold = new VenueSeatHold(0, Stream.of(1,2,3,4,5,6,7).collect(Collectors.toSet()), "fake@email.com");

		//Reserve additional seats
		venueSeatHold.reserveAdditionalSeats(Stream.of(6,7,8).collect(Collectors.toSet()));

		//Get the new reserved seats
		Set<Integer> seats = venueSeatHold.getReservedSeats();
		//The set containing the seats the set above is expected to contain
		Set<Integer> expectedSeats = Stream.of(1,2,3,4,5,6,7,8).collect(Collectors.toSet());

		assertEquals("Expected Sets to be equal", expectedSeats, seats);
	}

	@Test
	public void testSeatHoldExpires(){
		//findAndHoldSeats
		//Sleep
		//reserveSeats should fail
	}

	@Test
	public void testSeatsBecomesAvailableAfterSeatHoldExpires(){
		//numSeatsAvailable == 1
		//findAndHoldSeats
		//numSeatsAvailable == 0
		//Sleep
		//numSeatsAvailable == 1
	}

	@Test
	public void testHoldingThenImmediatelyReservingSeatsSucceeds(){
		//findAndHoldSeats
		//reserveSeats should succeed
	}

	@Test
	public void testSeatingPreferenceGetsBestSeats(){
		boolean[][] seatingConfiguration = new boolean[][]{
				{!true, !true, true, true, true , !true},
				{!true, true , true, true, !true, true },
				{!true, !true, true, true, true , !true},
				{!true, true , true, true, true , !true},
				{!true, true , true, true, !true, true },
		};
		Venue ts = new Venue(seatingConfiguration);
		//findAndHoldSeats for 4 seats

		//For closest to front, all available seats in the bottom row should be selected

		//For closest to back, the top three available seats and second-from-the-left middle seat should be selected

		//For closest to center, all available middle row seats should be selected

		//For closest together, all seats in the second from the bottom row should be selected
	}
}
