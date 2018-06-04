package com.example.company;

import com.example.company.struct.SeatHold;
import com.example.company.struct.TicketService;

import java.nio.file.NoSuchFileException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;

/**
 *
 */
public class Venue implements TicketService {
	//Note: It is undefined whether or not iterators will see the changes of any concurrent modifications to the map.
	//This is fine in my case - stale reads are acceptable.

	//The set containing the number of seats available to hold
	//TODO: Determine if ConcurrentSkipListSet is necessary
	private Set<Integer> availableSeats = Collections.newSetFromMap(new ConcurrentHashMap<>());
	//Maps the email of customers who have been given a temporary seat hold to their SeatHold
	private ConcurrentHashMap<String, VenueSeatHold> seatHolds = new ConcurrentHashMap<>();
	//Maps the email of customers who have registered their seats to their SeatHold
	private ConcurrentHashMap<String, VenueSeatHold> reservedSeats = new ConcurrentHashMap<>();

	//This uses an unbounded queue, and .shutdown() is never called, so a RejectedExecutionHandler is not necessary
	private final ScheduledThreadPoolExecutor timerExecutorService = new ScheduledThreadPoolExecutor(3);

	/**
	 * Initializes the set of available seats in a way that represents a square venue with seatsPerSide^2 seats.
	 * i.e. initializes a 2d-array of shape [seatsPerSide][seatsPerSide] in a set representation.
	 *
	 * @param seatsPerSide The number of seats in every row and column
	 */
	public Venue(int seatsPerSide){
		//Creates a square seating arrangement.
		//Note: The index of a seat is represented by its position in row-major order.
		for(int i=0; i<seatsPerSide*seatsPerSide; i++){
			availableSeats.add(i);
		}
	}

	/**
	 * Initializes the set of available seats in a way that represents
	 * a rectangular venue with seatsPerRow*numColumns seats.
	 * i.e. initializes a 2d-array of shape [seatsPerRow][numColumns] in a set representation.
	 *
	 * @param seatsPerRow The number of seats in every row
	 * @param numColumns The number of seats in every column
	 */
	public Venue(int seatsPerRow, int numColumns){
		//Creates a rectangular seating arrangement.
		//Note: The index of a seat is represented by its position in row-major order.
		for(int i=0; i<seatsPerRow*numColumns; i++){
			availableSeats.add(i);
		}
	}

	/**
	 * Initializes the set of available seats given a 2d-array representing available seats.
	 * True values represent a free seat while false values represent an unavailable seat
	 * (or a lack of a seat, for venues with a odd-shaped layout).
	 *
	 * @param seatingConfiguration A 2d-array representing the seats available in the venue
	 */
	public Venue(boolean[][] seatingConfiguration){

		//The number of rows in the venue
		int numRows = seatingConfiguration.length;
		//For each row
		for(int row=0; row<numRows; row++){
			//For each column
			for(int col=0; col<seatingConfiguration[row].length; col++){
				//Check if the seat at this location is available
				boolean currentSeatAvailable = seatingConfiguration[row][col];
				if(currentSeatAvailable){
					//If it is, add it to the set of available seats
					//Note: The index of a seat is represented by its position in row-major order.
					availableSeats.add(numRows*row + col);
				}
			}
		}
	}

	@Override
	public int numSeatsAvailable() {
		return availableSeats.size();
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, String customerEmail){
		//TODO: Finish
		return null;
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		//Get the actual SeatHold registered by this customer, if there is one
		VenueSeatHold seatHold = seatHolds.get(customerEmail);
		//If the customer actually has a SeatHold
		if(seatHold != null){
			seatHold.lock();
			//If the SeatHold expired after the lock was acquired
			if(seatHolds.get(customerEmail) == null){
				return "Sorry, your seat hold has expired";
			}
			seatHolds.remove(customerEmail);
			reservedSeats.put(customerEmail, seatHold);
			return "Your seats have been registered. You registration code is: " + seatHoldId;
		}else{
			return "Sorry, no seat hold was found for the email address.\nYou may have entered";
		}
	}

	/*public void something(){
		//Create a task to automatically release a SeatHold after 5 seconds.
		ScheduledFuture<?> scheduledTask = timerExecutorService.schedule(()  -> {
			//...
		}, 5, TimeUnit.SECONDS);
	}*/
}
