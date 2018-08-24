package com.example.company.struct;

/**
 * This enum represents the different types of seating orders a venue supports.
 */
public enum SeatingPreference {
    /**
     * If the user prefers to sit closest to the front of the Ticket Service.
     */
    CLOSEST_TO_FRONT,
    /**
     * If the user prefers to sit closest to the back of the Ticket Service.
     */
    CLOSEST_TO_BACK,
    /**
     * If the user prefers to sit closest to the center of the Ticket Service.
     */
    CLOSEST_TO_CENTER,
    /**
     * If the user prefers to have all of their seats as close as possible to
     * each other in the Ticket Service.
     */
    CLOSEST_TOGETHER,
    /**
     * If the user has no preference regarding seating in the Ticket Service.
     */
    NONE
}
