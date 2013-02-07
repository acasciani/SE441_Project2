package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class Exit {
	private final Passenger passenger;
	
	public Exit(final Passenger passenger) {
		this.passenger = passenger;
	}

	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_EXIT.value();
	}
}
