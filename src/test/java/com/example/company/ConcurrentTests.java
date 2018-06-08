package com.example.company;

import static org.junit.Assert.*;

import com.example.company.struct.SeatHold;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentTests {

	private static final int N_THREADS = 6;

	@Test
	public void testSeatOnlyHoldableBySingleCustomer() {
		//Create threads; all findAndHoldSeats. Only one should succeed

		//Create a venue with 1 seat
		Venue v = new Venue(1);

		//True if any of the threads succeeded
		AtomicBoolean singleThreadSucceeded = new AtomicBoolean(false);
		//True if more than one thread succeeded
		AtomicBoolean multipleThreadsSucceeded = new AtomicBoolean(false);

		Thread[] threads = new Thread[N_THREADS];
		for (int i = 0; i < N_THREADS; i++) {
			final int T_NUM = i;
			Runnable r = () -> {
				//Attempt to hold a seat
				SeatHold result = v.findAndHoldSeats(1, T_NUM + "@email.com");
				if(result != null){
					//If the thread was successful, set the relevant AtomicBoolean to true
					boolean anotherThreadSucceeded = singleThreadSucceeded.getAndSet(true);
					if(anotherThreadSucceeded){
						//If another thread was also successful, set the relevant AtomicBoolean to true
						multipleThreadsSucceeded.set(true);
					}
				}
			};
			threads[i] = new Thread(r);
			threads[i].start();
		}

		//Wait for all threads to finish
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail("Thread died");
			}
		}

		if(multipleThreadsSucceeded.get()){
			fail("Expected only one thread to succeed");
		}
		if(!singleThreadSucceeded.get()){
			fail("Expected a thread to succeed");
		}
	}

	@Test
	public void testOnlyOneThreadSucceedsHoldingLimitedSeats(){
		//Create 2n-1 available seats
		//Create 2 threads, each attempting to hold n seats. Only one should succeed

		//Create a venue with 49 seats
		Venue v = new Venue(7);

		//Variables used to keep track of which thread succeeded
		AtomicBoolean t1Succeeded = new AtomicBoolean(false);
		AtomicBoolean t2Succeeded = new AtomicBoolean(false);

		Thread[] threads = new Thread[2];
		for (int i = 0; i < 2; i++) {
			final int T_NUM = i;
			Runnable r = () -> {
				//Attempt to hold 25 seats
				SeatHold result = v.findAndHoldSeats(25, T_NUM + "@email.com");
				if(result != null){
					//If the thread was successful, set the relevant AtomicBoolean to true
					if(T_NUM == 0){
						t1Succeeded.set(true);
					}else{
						t2Succeeded.set(true);
					}
				}
			};
			threads[i] = new Thread(r);
			threads[i].start();
		}

		//Wait for both threads to finish
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail("Thread died");
			}
		}

		if(t1Succeeded.get() == t2Succeeded.get()){
			if(t1Succeeded.get()) {
				fail("Expected only one thread to succeed");
			}else{
				fail("Expected a thread to succeed");
			}
		}
	}
}
