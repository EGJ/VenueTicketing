package com.example.company.struct;

import java.util.Set;

/**
 * Holds seat(s) for a customer
 *
 * @param <T> How each seat is represented. (e.g. Integer ids, String names, etc.)
 */
//I could make this class more generic using the following (and replacing Set<T> with CollectionType)
//but I feel that doing so would make this class a little too generic:
//public abstract class SeatHold<CollectionType extends Collection<T>, T> {
public abstract class SeatHold<T> {
	//The id of the SeatHold Object
	private final int id;
	//The list of the seats reserved
	private Set<T> reservedSeats;

	/**
	 * @param id            The id of the new SeatHold Object
	 * @param reservedSeats The list of seats reserved by this SeatHold Object
	 */
	protected SeatHold(int id, Set<T> reservedSeats) {
		this.id = id;
		this.reservedSeats = reservedSeats;
	}

	/**
	 * @return The id of the seat hold object
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return The seats which this SeatHold is reserving
	 */
	public Set<T> getReservedSeats() {
		return reservedSeats;
	}

	/**
	 * @param reservedSeats The seats to be reserved
	 */
	public void setReservedSeats(Set<T> reservedSeats) {
		this.reservedSeats = reservedSeats;
	}
}
