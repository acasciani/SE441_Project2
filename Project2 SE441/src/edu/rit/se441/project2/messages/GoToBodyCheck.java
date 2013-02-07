package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class GoToBodyCheck {
	private final Passenger passenger;
	
	public GoToBodyCheck(final Passenger passenger) {
		this.passenger = passenger;
	}
	
	public Passenger getPassenger() {
		return passenger;
	}
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_GO_TO_BODY_CHECK.value();
	}

}
