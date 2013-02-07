package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Consts;

/**
 * BagCheckReport.java
 * Immutable Message: contains the report from the BagCheck
 *   - Passenger
 *   - Did the Bag pass security
 */
public class BagCheckReport {
	private final boolean passed;
	private final Baggage baggage;
	
	public BagCheckReport(final Baggage baggage, final boolean passed) {
		this.passed = passed;
		this.baggage = baggage;
	}
	
	public boolean didPass() {
		return passed;
	}
	
	public Baggage getbaggage() {
		return baggage;
	}
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_BAG_CHECK_REPORT.value();
	}
}
