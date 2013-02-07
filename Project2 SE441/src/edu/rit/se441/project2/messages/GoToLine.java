package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class GoToLine {
	private final Passenger passenger;
	
	public GoToLine(final Passenger passenger) {
		this.passenger = passenger;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}

	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_GO_TO_LINE.value();
	}
}
