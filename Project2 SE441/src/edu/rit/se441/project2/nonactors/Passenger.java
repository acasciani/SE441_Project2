package edu.rit.se441.project2.nonactors;

public class Passenger {
	private final Baggage baggage;
	private final boolean passes;
	private final String name;
	
	public Passenger(final String name) {
		// This should allow many bags in the future.
		this.baggage = new Baggage(this);
		this.passes = false; // Sould be random
		this.name = name;
	}
	
	public Passenger(final String name, final boolean passes) {
		// This should allow many bags in the future.
		this.baggage = new Baggage(this);
		this.passes = passes; // Sould be random
		this.name = name;
	}
	
	public Baggage getBaggage() {
		return baggage;
	}
	
	public boolean doesPassengerPass() {
		return passes;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
