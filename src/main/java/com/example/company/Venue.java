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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Ticketing service for a Venue that supports concurrent operations, allows
 * users to specify their desired seating preference, and automatically releases
 * held seats that have not been reserved after a set period of time.
 */
public class Venue implements TicketService {

    /**
     * The total seating capacity of the venue.
     */
    private final int totalSeatsInVenue;
    /**
     * The amount of time in seconds before held seats are released.
     */
    private final int seatHoldExpirationTime = 5;
    /**
     * The set containing the number of seats available to hold.
     */
    private final ConcurrentSkipListSet<Integer> availableSeats =
        new ConcurrentSkipListSet<>();
    /**
     * Maps the email of customers who have been given a temporary seat hold to
     * their SeatHold.
     */
    private final ConcurrentHashMap<String, VenueSeatHold> seatHolds =
        new ConcurrentHashMap<>();
    /**
     * Maps the email of customers who have been given a temporary seat hold to
     * the pending event that will remove the hold.
     */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingTasks =
        new ConcurrentHashMap<>();
    /**
     * Maps the email of customers who have registered their seats to their
     * SeatHold.
     */
    private final ConcurrentHashMap<String, VenueSeatHold> reservedSeats
        = new ConcurrentHashMap<>();
    /**
     * The id to be assigned to the next SeatHold.
     */
    private final AtomicInteger nextSeatHoldId = new AtomicInteger(0);
    /**
     * This uses an unbounded queue, and .shutdown() is never called, so a
     * RejectedExecutionHandler is not necessary.
     */
    private final ScheduledThreadPoolExecutor timerExecutorService =
        new ScheduledThreadPoolExecutor(3);
    /**
     * The default seating order of the venue.
     */
    private SeatingPreference seatingPreference = SeatingPreference.NONE;

    /**
     * Initializes the set of available seats in a way that represents a square
     * venue with seatsPerSide^2 seats. i.e. initializes a 2d-array of shape
     * [seatsPerSide][seatsPerSide] in a set representation.
     *
     * @param seatsPerSide The number of seats in every row and column
     */
    public Venue(final int seatsPerSide) {
        // Creates a square seating arrangement.
        // Note: The index of a seat is represented by its position in row-major
        // order. Index 0 is in the back left corner of the venue.
        for (int i = 0; i < seatsPerSide * seatsPerSide; i++) {
            availableSeats.add(i);
        }
        totalSeatsInVenue = seatsPerSide * seatsPerSide;
    }

    /**
     * Initializes the set of available seats in a way that represents a
     * rectangular venue with seatsPerRow*numColumns seats. i.e. initializes a
     * 2d-array of shape [seatsPerRow][numColumns] in a set representation.
     *
     * @param seatsPerRow The number of seats in every row
     * @param numColumns The number of seats in every column
     */
    public Venue(final int seatsPerRow, final int numColumns) {
        // Creates a rectangular seating arrangement.
        // Note: The index of a seat is represented by its position in row-major
        // order. Index 0 is in the back left corner of the venue.
        for (int i = 0; i < seatsPerRow * numColumns; i++) {
            availableSeats.add(i);
        }
        totalSeatsInVenue = seatsPerRow * numColumns;
    }

    /**
     * Initializes the set of available seats given a 2d-array representing
     * available seats. True values represent a free seat while false values
     * represent an unavailable seat (or a lack of a seat, for venues with an
     * odd-shaped layout).
     *
     * @param seatingConfiguration A 2d-array representing the seats
     *     available in the venue
     */
    public Venue(final boolean[][] seatingConfiguration) {
        // Note: The index of a seat is represented by its position in row-major
        // order. Index 0 is in the back left corner of the venue.

        int currentSeatNumber = -1;
        // For each row
        for (boolean[] row : seatingConfiguration) {
            // For each column
            for (boolean seatAvailable : row) {
                currentSeatNumber++;
                // Check if the seat at this location is available
                if (seatAvailable) {
                    // If it is, add it to the set of available seats
                    // Note: The index of a seat is represented by its position
                    // in row-major order.
                    availableSeats.add(currentSeatNumber);
                }
            }
        }
        this.totalSeatsInVenue = currentSeatNumber + 1;
    }

    /**
     * Given a sorted array, this function returns a set of all the runs
     * (consecutive integers of size >= 1) in said array. The TreeSet returns
     * the runs in by descending number of elements.
     *
     * @param arr The sorted array of integers to find runs in
     * @return A set of all runs in the array, in descending order
     */
    private static TreeSet<LinkedHashSet<Integer>> getRuns(
        final Integer[] arr) {
        // If there are no elements
        if (arr.length == 0) {
            return new TreeSet<>();
        }
        // The variable holding the previous element in the array (see loop
        // below)
        int previousValue = arr[0];

        // Create a new TreeSet (ordered) and override the comparator so largest
        // elements come first
        TreeSet<LinkedHashSet<Integer>> allRuns =
            new TreeSet<>(
                (LinkedHashSet<Integer> o1, LinkedHashSet<Integer> o2) -> {
                    // Compare the lengths of the sets
                    int result = Integer.compare(o2.size(), o1.size());
                    // If the sets have the same size
                    if (result == 0) {
                        // Check if they are equal (so two sets with the same
                        // size can both be added)
                        if (!o1.equals(o2)) {
                            // If they are different sets, set the result to
                            // 1 (o1 is "bigger" than o2)
                            result = 1;
                        }
                        // If they are equal, keep the result 0 so the set is
                        // not added again
                    }
                    return result;
                });
        // The set of runs for this loop iteration
        LinkedHashSet<Integer> currentRuns = new LinkedHashSet<>();
        // Add the first element in the array to the above set
        currentRuns.add(previousValue);

        // For each element in the array, starting at index 1
        for (int i = 1; i < arr.length; i++) {
            // Get the value of that element
            int val = arr[i];
            // If this value is part of a run with the previous element
            if (val == previousValue + 1) {
                // Add it to the set of runs for this iteration
                currentRuns.add(val);
            } else {
                // Otherwise, the run for this iteration to the set of all
                // runs (may contain only one value)
                allRuns.add(currentRuns);
                // Clear the set of runs in preparation for the next iteration
                currentRuns = new LinkedHashSet<>();
                // Add this element to the new set of runs
                currentRuns.add(val);
            }
            // Update the value of the previous element in the array
            previousValue = val;
        }
        // Add the last run to the set of all runs
        allRuns.add(currentRuns);
        return allRuns;
    }

    @Override
    public final int numSeatsAvailable() {
        // Note: This is not guaranteed to always show the latest value if
        // the set is modified while
        // this is running.
        // As such, this value can only be used as an estimate. To prevent
        // this, a lock should be used
        // in addition to,
        // the availableSeats set, but then the benefits of concurrent code
        // would negated.
        // As such, I consider this estimated value to be okay.
        return availableSeats.size();
    }

    @Override
    public final SeatHold<Integer> findAndHoldSeats(final int numSeats,
        final String customerEmail) {
        return findAndHoldSeats(numSeats, customerEmail, seatingPreference);
    }

    /**
     * Find and hold the best available seats for a customer.
     *
     * @param numSeats The number of seats to hold
     * @param customerEmail The email of the customer trying to hold the
     *     seats
     * @param userSeatingPreference The Seating preference the customer
     *     prefers
     * @return The SeatHold containing the seats that were reserved, or null if
     *     it was unable to reserve any seats
     */
    public final SeatHold<Integer> findAndHoldSeats(
        final int numSeats, final String customerEmail,
        final SeatingPreference userSeatingPreference) {

        // The id of the seats that have been held
        Set<Integer> heldSeats = new HashSet<>(numSeats);
        // The amount of seats that have been held so far
        int seatsSuccessfullyReserved = 0;

        // An iterator for the set
        // Note: It is undefined whether or not iterators will see the
        // changes of any concurrent
        // modifications to the map.
        Iterator<Integer> iterator = null;

        // Hold seats based on the venue's seating order
        if (userSeatingPreference == SeatingPreference.CLOSEST_TO_BACK
            || userSeatingPreference == SeatingPreference.NONE) {
            // An iterator that iterates the set in order
            iterator = availableSeats.iterator();
        } else if (userSeatingPreference
            == SeatingPreference.CLOSEST_TO_FRONT) {
            // An iterator that iterates the set backwards
            iterator = availableSeats.descendingIterator();
        } else if (userSeatingPreference
            == SeatingPreference.CLOSEST_TO_CENTER) {
            // Convert the set to an array
            Integer[] availableSeatsArray = availableSeats
                .toArray(new Integer[0]);

            // A sparse-array-like representation of the above array.
            // The array's size is the value of the largest available seat's
            // index (+1 to include that
            // index as well)
            Boolean[] seatAtIndex = new Boolean[totalSeatsInVenue];
            // Initialize the values in the array
            // e.g. [3,5,9] -> [false, false, false, true, false, true,
            // false, false, false, true]
            for (Integer i : availableSeatsArray) {
                seatAtIndex[i] = true;
            }

            final int arrSize = seatAtIndex.length;
            // Variable used to determine whether the next seat index that
            // should be check is above or
            // below the center
            boolean up = true;
            final int center = arrSize / 2;
            // The distance the current index is from the center
            int distanceFromCenter = 0;

            // If the number of reserved seats desired has not been reached,
            // and the end of the array
            // hasn't been reached
            for (int seat = center;
                (seatsSuccessfullyReserved != numSeats) && (seat < arrSize - 2
                    && seat > 1);
                up ^= true) {
                if (up) {
                    // If the next index that should be checked is above the
                    // center
                    // Get the seat at the appropriate distance from the
                    // center (initially the center itself)
                    seat = center + distanceFromCenter;
                    // Increment the current distance from the center (only
                    // done every other seat checked)
                    distanceFromCenter++;
                } else {
                    // If the next index that should be checked is below the
                    // center
                    // Get the seat at the appropriate distance from the center
                    seat = center - distanceFromCenter;
                }
                // Check if the seat at the specified index is available
                // This check is not strictly necessary, but would otherwise
                // hinder performance (though is
                // technically less accurate)
                if (seatAtIndex[seat] != null && seatAtIndex[seat]) {
                    // Remove it from the set of available seats
                    boolean succeeded = availableSeats.remove(seat);
                    // Check if the removal succeeded (will fail if another
                    // thread removed the seat before
                    // this one)
                    if (succeeded) {
                        // If it successfully removed the seat, add it to the
                        // set of held seats
                        heldSeats.add(seat);
                        seatsSuccessfullyReserved++;
                    }
                }
            }
        } else if (userSeatingPreference
            == SeatingPreference.CLOSEST_TOGETHER) {
            // Convert the set to an array
            Integer[] availableSeatsArray = availableSeats
                .toArray(new Integer[0]);

            // Find the runs in the array of available seats (largest run first)
            TreeSet<LinkedHashSet<Integer>> runs = getRuns(availableSeatsArray);

            if (runs != null) {
                // For each run
                for (LinkedHashSet<Integer> run : runs) {
                    Iterator<Integer> iterator1 = run.iterator();
                    // For each seat in the run, and while the number of
                    // seats the customer wanted has been
                    // not yet been held
                    while (iterator1.hasNext()
                        && seatsSuccessfullyReserved != numSeats) {
                        Integer seat = iterator1.next();
                        // Remove it from the set of available seats
                        boolean succeeded = availableSeats.remove(seat);
                        // Check if the removal succeeded (will fail if
                        // another thread removed the seat before
                        // this one)
                        if (succeeded) {
                            // If it successfully removed the seat, add it to
                            // the set of held seats
                            heldSeats.add(seat);
                            seatsSuccessfullyReserved++;
                        }
                    }
                }
            }
        } else {
            // In case a new SeatingPreference is added, throw an exception
            throw new UnsupportedOperationException(
                "SeatingPreference: " + userSeatingPreference
                    + " is not currently supported.");
        }

        // If an the current SeatingPreference required a single iterator
        if (iterator != null) {
            // For each available seat, and while the number of seats the
            // customer wanted has been not yet
            // been held
            while (iterator.hasNext()
                && seatsSuccessfullyReserved != numSeats) {
                Integer seat = iterator.next();

                // Remove it from the set of available seats
                boolean succeeded = availableSeats.remove(seat);
                // Check if the removal succeeded (will fail if another
                // thread removed the seat before this
                // one)
                if (succeeded) {
                    // If it successfully removed the seat, add it to the set
                    // of held seats
                    heldSeats.add(seat);
                    seatsSuccessfullyReserved++;
                }
            }
        }

        // If all seats have been checked and there are none left, but the
        // customer wanted more seats
        if (heldSeats.size() != numSeats) {
            // Optionally, check if there are more seats available now and
            // try to hold the remaining seats
            // needed
            /*if(numSeatsAvailable() != 0){
            VenueSeatHold vsh = (VenueSeatHold) findAndHoldSeats(numSeats -
            heldSeats.size(), customerEmail);

            if(vsh != null){
              vsh.reserveAdditionalSeats(heldSeats);
            }
            }else{See below}*/

            // Remember to add the held seats back to the set of available seats
            availableSeats.addAll(heldSeats);
            // Since the customer's request did not succeed, return null
            return null;
        } else {
            // If all seats were registered successfully.

            // Get the current SeatHold Object associated with the customer
            VenueSeatHold seatHold = seatHolds.remove(customerEmail);
            if (seatHold != null) {
                // If this customer already has some seats held it will have
                // a scheduled task running. Get
                // that task
                // (A lock isn't necessary since only one thread will succeed
                // in removing from a concurrent
                // collection)
                ScheduledFuture<?> pendingTask = pendingTasks
                    .remove(customerEmail);
                pendingTask.cancel(true);

                // Update the SeatHold with the additional seats
                seatHold.reserveAdditionalSeats(heldSeats);
            } else {
                // Otherwise, return a new SeatHold Object with those seats
                seatHold = new VenueSeatHold(nextSeatHoldId.getAndIncrement(),
                    heldSeats,
                    customerEmail);
                seatHolds.put(customerEmail, seatHold);
            }

            // Create a new timer to auto-release the seatHold
            createAutoReleaseTask(customerEmail);

            return seatHold;
        }
    }

    @Override
    public final String reserveSeats(final int seatHoldId,
        final String customerEmail) {
        // Get the actual SeatHold registered by this customer, if there is
        // one, and remove it from the
        VenueSeatHold seatHold = seatHolds.remove(customerEmail);
        // If the customer actually has a SeatHold
        if (seatHold != null) {
            // Update the relevant maps
            reservedSeats.put(customerEmail, seatHold);
            pendingTasks.remove(customerEmail);

            return Integer.toString(seatHoldId);
        } else {
            return null;
        }
    }

    /**
     * @return The default seating preference for this venue
     */
    public final synchronized SeatingPreference getSeatingPreference() {
        return seatingPreference;
    }

    /**
     * @param userSeatingPreference The new default seating preference for
     *     this venue
     */
    public final synchronized void setSeatingPreference(
        final SeatingPreference userSeatingPreference) {
        seatingPreference = userSeatingPreference;
    }

    /**
     * Automatically releases the held seats associated with a SeatHold after a
     * set amount of time goes by without those seats being reserved.
     *
     * @param customerEmail The email of the customer who owns the SeatHold
     */
    private void createAutoReleaseTask(final String customerEmail) {
        // Create a task to automatically release a SeatHold after 5 seconds.
        ScheduledFuture<?> scheduledTask =
            timerExecutorService.schedule(
                () -> {
                    // Remove the SeatHold from the map of SeatHolds
                    VenueSeatHold seatHold = seatHolds.remove(customerEmail);
                    if (seatHold != null) {
                        // Return the held seats to the set of available seats
                        availableSeats.addAll(seatHold.getReservedSeats());
                        // Remove this task from the map of tasks
                        pendingTasks.remove(customerEmail);
                    }
                    // Note: The task is not auto-removed if a SeatHold is
                    // not found; this means either an
                    // improper email was
                    // entered, or another thread calling
                    // reserveSeats/findAndHoldSeats got the SeatHold
                    // first and will
                    // remove the task there.
                },
                seatHoldExpirationTime,
                TimeUnit.SECONDS);

        pendingTasks.put(customerEmail, scheduledTask);
    }
}
