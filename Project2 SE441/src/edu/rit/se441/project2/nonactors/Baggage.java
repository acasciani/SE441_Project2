package edu.rit.se441.project2.nonactors;

public class Baggage {
	private final boolean passes;
	private final Passenger whoBelongsTo;
	
	public Baggage(final Passenger whoBelongsTo) {
		// Just for now but should be random.
		this.passes = false;
		this.whoBelongsTo = whoBelongsTo;
	}
	
	public Baggage(final Passenger whoBelongsTo, final boolean passes) {
		this.passes = passes;
		this.whoBelongsTo = whoBelongsTo;
	}
	
	public boolean doesBaggagePass() {
		return passes;
	}
	
	public Passenger whoDoesThisBaggageBelongTo() {
		return whoBelongsTo;
	}
}
