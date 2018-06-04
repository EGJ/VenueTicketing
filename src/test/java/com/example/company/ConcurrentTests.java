package com.example.company;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTests {

	private static final int N_THREADS = 6;

	@Test
	public void testSeatOnlyHoldableBySingleCustomer() {
		Venue v = new Venue(1);
		//Create threads
		//All findAndHoldSeats, only one should succeed
	}

	@Test
	public void testConcurrentModificationAllowed(){

	}



	@Test
	public void testSomething(){
		//The number of exceptions that occurred
		AtomicInteger nExceptions = new AtomicInteger(0);

		Thread[] threads = new Thread[N_THREADS];
		for (int i = 0; i < N_THREADS; i++) {
			Runnable r = () -> {
				try {
					//TODO: Test something
				} catch (Throwable t) {
					t.printStackTrace();
					nExceptions.incrementAndGet();
				}
			};
			threads[i] = new Thread(r);
			threads[i].start();
		}
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail("Thread died");
			}
		}
		assertEquals("Expected no exceptions", 0, nExceptions.get());
	}
}
