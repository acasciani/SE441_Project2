package edu.rit.se441.project2.nonactors;

public class Passenger {
	private final Baggage baggage;
	private final boolean passes;
	
	public Passenger() {
		// This should allow many bags in the future.
		this.baggage = new Baggage(this);
		this.passes = false; // Sould be random
	}
	
	public Passenger(final boolean passes) {
		// This should allow many bags in the future.
		this.baggage = new Baggage(this);
		this.passes = passes; // Sould be random
	}
	
	public Baggage getBaggage() {
		return baggage;
	}
	
	public boolean doesPassengerPass() {
		return passes;
	}
	

}
