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
have succeeded individually. Though preventable, this would complicate the solution a
great deal and I consider this to be an acceptable limitation - considering that the method
may be re-run in such cases.