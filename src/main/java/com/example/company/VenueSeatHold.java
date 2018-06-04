package com.example.company;

import com.example.company.struct.SeatHold;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Creates a SeatHold object for a venue where seats are represented as Integers
 */
public class VenueSeatHold extends SeatHold<Integer> {
	//The email address of the person who registered this SeatHold
	private final String email;
	//A lock for this SeatHold that must be held before reading/writing
	private final Lock lock = new ReentrantLock();


	/**
	 * @param id The id of the new VenueSeatHold Object
	 * @param reservedSeats The list of seats reserved by this VenueSeatHold Object
	 */
	public VenueSeatHold(int id, Set<Integer> reservedSeats, String email){
		super(id, reservedSeats);
		this.email = email;
	}

	public void reserveAdditionalSeats(Set<Integer> seats){
		//Get the current seats reserved
		Set<Integer> reservedSeats = getReservedSeats();
		//Add the new seats to the set (passed by reference).
		reservedSeats.addAll(seats);
	}

	/**
	 * @return The email of the person whom this VenueSeatHold is registered to
	 */
	public String getEmail(){
		return email;
	}

	/**
	 * Locks the lock associated with this VenueSeatHold
	 */
	public void lock(){
		lock.lock();
	}

	/**
	 * Unlocks the lock associated with this VenueSeatHold
	 */
	public void unlock() throws IllegalMonitorStateException{
		//IllegalMonitorStateException will only be thrown if a thread that does not own the lock on this Object tries
		//to unlock it. This shouldn't happen in my implementation, but I want the exception to be thrown if it does.
		lock.unlock();
	}
}
