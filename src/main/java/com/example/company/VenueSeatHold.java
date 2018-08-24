package com.example.company;

import com.example.company.struct.SeatHold;

import java.util.Set;

/**
 * Creates a SeatHold object for a venue where seats are represented as
 * Integers.
 */
public class VenueSeatHold extends SeatHold<Integer> {

    /**
     * The email address of the person who registered this SeatHold.
     */
    private final String email;

    /**
     * @param id The id of the new VenueSeatHold Object
     * @param reservedSeats The list of seats reserved by this VenueSeatHold
     *     Object
     * @param customerEmail The
     */
    public VenueSeatHold(final int id, final Set<Integer> reservedSeats,
        final String customerEmail) {

        super(id, reservedSeats);
        email = customerEmail;
    }

    /**
     * Reserves an additional set of seats to the SeatHold.
     *
     * @param seats The set of additional seats to be added to the current
     *     SeatHold
     */
    public final void reserveAdditionalSeats(final Set<Integer> seats) {
        // Get the current seats reserved
        Set<Integer> reservedSeats = getReservedSeats();
        // Add the new seats to the set (passed by reference).
        reservedSeats.addAll(seats);
    }

    /**
     * @return The email of the person whom this VenueSeatHold is registered to
     */
    public final String getEmail() {
        return email;
    }
}
