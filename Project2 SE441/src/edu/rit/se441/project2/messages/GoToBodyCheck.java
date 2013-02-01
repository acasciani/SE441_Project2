package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Passenger;

public class GoToBodyCheck {
	private final Passenger passenger;
	
	public GoToBodyCheck(final Passenger passenger) {
		this.passenger = passenger;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}

}
