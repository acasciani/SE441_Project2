package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Passenger;

/**
 * BagCheckReport.java
 * Immutable Message: contains the report from the BagCheck
 *   - Passenger
 *   - Did the Bag pass security
 */
public class BagCheckReport {
	private final boolean passed;
	private final Passenger passenger;
	
	public BagCheckReport(final Passenger passenger, final boolean passed) {
		this.passed = passed;
		this.passenger = passenger;
	}
	
	public boolean didPass() {
		return passed;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}
}
