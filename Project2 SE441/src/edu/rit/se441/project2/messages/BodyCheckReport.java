package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

/**
 * BodyCheckReport.java
 * Immutable Message: contains the report from the BodyCheck
 *   - Passenger
 *   - Did the Body pass security
 */
public class BodyCheckReport {
	private final boolean passed;
	private final Passenger passenger;
	
	public BodyCheckReport(final Passenger passenger, final boolean passed) {
		this.passed = passed;
		this.passenger = passenger;
	}
	
	public boolean didPass() {
		return passed;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_BODY_CHECK_REPORT.value();
	}
}
