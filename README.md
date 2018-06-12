# Venue Ticketing

My implementation of the TicketService coding challenge by Walmart Labs.

## Building and testing

To build the solution without running tests, run
`mvn package -DskipTests`

To run the all tests after successfully building, run
`mvn test`

To only run one specific test class, use the command
`mvn -Dtest=testClass test`

To run specific tests within a test class, use the command
`mvn -Dtest=testClass#testName1+testName2+... test`

To build the solution and run all the tests at once, run
`mvn package`

## Assumptions

**numSeatsAvailable:** I assume the method does not *require* returning a value that
represents the number of seats available in the venue at the time it is called. This is
because all other functions can run concurrently, so even if such a value was returned,
it itself is only useful as an estimate. In my implementation, this method may not always
show the latest value if the related set is modified while it is running.

**findAndHoldSeats:** Because multiple threads may run this method concurrently and
the threads temporarily set aside some seats during their execution, it is possible that the
threads cause each other to fail in finding and holding seats - even if one of them could
have succeeded individually. To clarify, this can only occur if multiple threads are trying to
reserve more than the total number of available seats (e.g. two threads attempt to hold 5 seats,
but there are only 9 available). I consider this to be an acceptable limitation.

## Examples

Given an initial venue `v` with the following seating arrangement:

```
_ _ _ _ U _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ U _ _ _ _ U _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U U _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U _ _ _ _ _
_ U _ _ U _ _ U _ _
_ U _ _ U _ _ U _ _
```
Where `_` represents a free seat, `U` represents an unavailable seat, and `H` represents a held seat.

The sequence of statements below are followed by the resulting seating arrangement at the venue.

`v.findAndHoldSeats(6, "email1", SeatingPreference.CLOSEST_TO_FRONT);`
```
_ _ _ _ U _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ U _ _ _ _ U _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U U _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U _ _ _ _ _
_ U _ _ U _ _ U _ _
_ U H H U H H U H H
```
`v.findAndHoldSeats(15, "email2", SeatingPreference.CLOSEST_TO_BACK);`
```
H H H H U H H H H H
H H H H H H _ _ _ _
_ _ U _ _ _ _ U _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U U _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U _ _ _ _ _
_ U _ _ U _ _ U _ _
_ U U U U U U U U U
```
`v.findAndHoldSeats(9, "email3", SeatingPreference.CLOSEST_TO_CENTER);`
```
U U U U U U U U U U
U U U U U U _ _ _ _
_ _ U _ _ _ _ U _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
H H H H U U H H H H
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U _ _ _ _ _
_ U _ _ U _ _ U _ _
_ U U U U U U U U U
```
`v.findAndHoldSeats(5, "email4", SeatingPreference.CLOSEST_TOGETHER);`
```
U U U U U U U U U U
U U U U U U _ _ _ _
_ _ U _ _ _ _ U _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
U U U U U U U U U U
H H H H H _ _ _ _ _
_ _ _ _ _ _ _ _ _ _
_ _ _ _ U _ _ _ _ _
_ U _ _ U _ _ U _ _
_ U U U U U U U U U
```
