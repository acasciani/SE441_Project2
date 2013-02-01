package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Passenger;

public class GoToLine {
	private final Passenger passenger;
	
	public GoToLine(final Passenger passenger) {
		this.passenger = passenger;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}

}
