package com.example.company.struct;

import java.util.Set;

/**
 * Holds seat(s) for a customer.
 *
 * @param <T> How each seat is represented. (e.g. Integer ids, String names,
 *     etc.)
 */
// I could make this class more generic using the following (and replacing
// Set<T> with CollectionType) but I feel that doing so would make this class a
// little too generic: public abstract class SeatHold<CollectionType extends
// Collection<T>, T> {
public abstract class SeatHold<T> {

    /**
     * The id of the SeatHold Object.
     */
    private final int id;

    /**
     * The list of the seats reserved.
     */
    private Set<T> reservedSeats;

    /**
     * @param seatHoldId The id of the new SeatHold Object
     * @param heldSeats The list of seats reserved by this SeatHold Object
     */
    protected SeatHold(final int seatHoldId, final Set<T> heldSeats) {
        id = seatHoldId;
        reservedSeats = heldSeats;
    }

    /**
     * @return The id of the seat hold object
     */
    public final int getId() {
        return id;
    }

    /**
     * @return The seats which this SeatHold is reserving
     */
    public final Set<T> getReservedSeats() {
        return reservedSeats;
    }

    /**
     * @param heldSeats The seats to be reserved
     */
    public final void setReservedSeats(final Set<T> heldSeats) {
        reservedSeats = heldSeats;
    }
}
