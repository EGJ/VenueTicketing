package com.example.company;

import com.example.company.struct.SeatHold;

import java.util.Set;


/**
 * Creates a SeatHold object for a venue where seats are represented as Integers
 */
public class VenueSeatHold extends SeatHold<Integer> {
	//The email address of the person who registered this SeatHold
	private final String email;

	/**
	 * @param id            The id of the new VenueSeatHold Object
	 * @param reservedSeats The list of seats reserved by this VenueSeatHold Object
	 */
	public VenueSeatHold(int id, Set<Integer> reservedSeats, String email) {
		super(id, reservedSeats);
		this.email = email;
	}

	public void reserveAdditionalSeats(Set<Integer> seats) {
		//Get the current seats reserved
		Set<Integer> reservedSeats = getReservedSeats();
		//Add the new seats to the set (passed by reference).
		reservedSeats.addAll(seats);
	}

	/**
	 * @return The email of the person whom this VenueSeatHold is registered to
	 */
	public String getEmail() {
		return email;
	}
}
