package com.example.company;

import com.example.company.struct.SeatHold;
import com.example.company.struct.SeatingPreference;
import com.example.company.struct.TicketService;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class Venue implements TicketService {
	//The set containing the number of seats available to hold
	private ConcurrentSkipListSet<Integer> availableSeats = new ConcurrentSkipListSet<>();
	//Maps the email of customers who have been given a temporary seat hold to their SeatHold
	private ConcurrentHashMap<String, VenueSeatHold> seatHolds = new ConcurrentHashMap<>();
	//Maps the email of customers who have registered their seats to their SeatHold
	private ConcurrentHashMap<String, VenueSeatHold> reservedSeats = new ConcurrentHashMap<>();

	//The seating order of the venue
	private SeatingPreference seatingPreference = SeatingPreference.NONE;
	//The id to be assigned to the next SeatHold
	private final AtomicInteger seatHoldId = new AtomicInteger(0);

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
		//Index 0 is in the back left corner of the venue.
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
		//Index 0 is in the back left corner of the venue.
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
		//Note: The index of a seat is represented by its position in row-major order.
		//Index 0 is in the back left corner of the venue.

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
		//Note: This is not guaranteed to always show the latest value if the set is modified while this is running.
		//As such, this value can only be used as an estimate. To prevent this, a lock should be used in addition to,
		//the availableSeats set, but then the benefits of concurrent code would negated.
		//As such, I consider this estimated value to be okay.
		return availableSeats.size();
	}

	@Override
	public SeatHold<Integer> findAndHoldSeats(int numSeats, String customerEmail){
		//The id of the seats that have been held
		Set<Integer> heldSeats = new HashSet<>(numSeats);
		//The amount of seats that have been held so far
		int seatsSuccessfullyReserved = 0;

		//An iterator for the set
		//Note: It is undefined whether or not iterators will see the changes of any concurrent modifications to the map.
		Iterator<Integer> iterator;

		//Hold seats based on the venue's seating order
		if(seatingPreference == SeatingPreference.CLOSEST_TO_BACK || seatingPreference == SeatingPreference.NONE){
			//An iterator that iterates the set in order
			iterator = availableSeats.iterator();
		}else if(seatingPreference == SeatingPreference.CLOSEST_TO_FRONT){
			//An iterator that iterates the set backwards
			iterator = availableSeats.descendingIterator();
		}else if(seatingPreference == SeatingPreference.CLOSEST_TO_CENTER) {
			//Convert the set to an array
			Integer[] availableSeatsArray = availableSeats.toArray(new Integer[0]);
			int arrSize = availableSeatsArray.length;
			//TODO: Finish
			return null;
		}else if(seatingPreference == SeatingPreference.CLOSEST_TOGETHER) {
			//Convert the set to an array
			Integer[] availableSeatsArray = availableSeats.toArray(new Integer[0]);
			int arrSize = availableSeatsArray.length;

			//Find the runs in the array of available seats
			TreeSet<LinkedHashSet<Integer>> runs = getRuns(availableSeatsArray);

			//TODO: Finish
			return null;
		}else{
			//In case a new SeatingPreference is added, throw an exception
			throw new UnsupportedOperationException("SeatingPreference: " + seatingPreference + " is not currently supported.");
		}

		//For each available seat
		while(iterator.hasNext()){
			Integer seat = iterator.next();
			//If the number of seats the customer wanted has been reached
			if(seatsSuccessfullyReserved == numSeats){
				break;
			}

			//Remove it from the set of available seats
			boolean succeeded = availableSeats.remove(seat);
			//Check if the removal succeeded (will fail if another thread removed the seat before this one)
			if (succeeded) {
				//If it successfully removed the seat, add it to the set of held seats
				heldSeats.add(seat);
				seatsSuccessfullyReserved++;
			}
		}
		//If all seats have been checked and there are none left, but the customer wanted more seats
		if(heldSeats.size() != numSeats){
			//Remember to add the held seats back to the set of available seats
			availableSeats.addAll(heldSeats);
			//Since the customer's request did not succeed, return null
			return null;
		}else{
			//If all seats were registered successfully.

			//If this customer already has some seats held
			VenueSeatHold seatHold = seatHolds.get(customerEmail);
			if(seatHold != null){
				//Update that SeatHold with the additional seats
				seatHold.reserveAdditionalSeats(heldSeats);
			}else{
				//Otherwise, return a new SeatHold Object with those seats
				seatHold = new VenueSeatHold(seatHoldId.getAndIncrement(), heldSeats, customerEmail);
				seatHolds.put(customerEmail, seatHold);
			}

			return seatHold;
		}
		//Relevant test: testSeatOnlyHoldableBySingleCustomer
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

	/**
	 * @param seatingPreference The new seating preference for this venue
	 */
	public void setSeatingPreference(SeatingPreference seatingPreference){
		this.seatingPreference = seatingPreference;
	}

	/**
	 * @return The seating preference for this venue
	 */
	public SeatingPreference getSeatingPreference(){
		return seatingPreference;
	}

	/**
	 * Given a sorted array, this function returns a set of all the runs (consecutive integers of size >= 1)
	 * in said array. The TreeSet returns the runs in order of descending number of elements.
	 *
	 * @param arr The sorted array of integers to find runs in
	 * @return A set of all runs in the array, in descending order
	 */
	private static TreeSet<LinkedHashSet<Integer>> getRuns(Integer[] arr){
		//If there are no elements
		if(arr.length == 0){
			return null;
		}
		//The variable holding the previous element in the array (see loop below)
		int previousValue = arr[0];

		//Create a new TreeSet (ordered) and override the comparator so largest elements come first
		TreeSet<LinkedHashSet<Integer>> allRuns = new TreeSet<>((LinkedHashSet<Integer> o1, LinkedHashSet<Integer> o2) ->{
			//Compare the lengths of the sets
			int result = Integer.compare(o2.size(), o1.size());
			//If the sets have the same size
			if(result == 0){
				//Check if they are equal (so two sets with the same size can both be added)
				if(!o1.equals(o2)){
					//If they are different sets, set the result to 1 (o1 is "bigger" than o2)
					result = 1;
				}
				//If they are equal, keep the result 0 so the set is not added again
			}
			return result;
		});
		//The set of runs for this loop iteration
		LinkedHashSet<Integer> currentRuns = new LinkedHashSet<>();
		//Add the first element in the array to the above set
		currentRuns.add(previousValue);

		//For each element in the array, starting at index 1
		for(int i=1; i<arr.length; i++){
			//Get the value of that element
			int val = arr[i];
			//If this value is part of a run with the previous element
			if(val == previousValue+1){
				//Add it to the set of runs for this iteration
				currentRuns.add(val);
			}else{
				//Otherwise, the run for this iteration to the set of all runs (may contain only one value)
				allRuns.add(currentRuns);
				//Clear the set of runs in preparation for the next iteration
				currentRuns = new LinkedHashSet<>();
				//Add this element to the new set of runs
				currentRuns.add(val);
			}
			//Update the value of the previous element in the array
			previousValue = val;
		}
		//Add the last run to the set of all runs
		allRuns.add(currentRuns);
		return allRuns;
	}

	/*public void something(){
		//Create a task to automatically release a SeatHold after 5 seconds.
		ScheduledFuture<?> scheduledTask = timerExecutorService.schedule(()  -> {
			//...
		}, 5, TimeUnit.SECONDS);
	}*/
}
