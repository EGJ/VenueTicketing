package com.example.company;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NonConcurrentTests {

	@Test
	public void testPartialSeatHoldsFail() {
		//Venue v = new Venue(1 seat);
		//findAndHoldSeats for seats > 1 should return null
		//numSeatsAvailable == 1
	}

	@Test
	public void testNumSeatsAvailableReturnsCorrectValue(){
		//Create venue with first constructor
		//Check
		//Create venue with second constructor
		//Check
		//Create venue with third constructor with some missing values
		//Check
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
	public void testSeatBecomesAvailableAfterSeatHoldExpires(){
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
}
