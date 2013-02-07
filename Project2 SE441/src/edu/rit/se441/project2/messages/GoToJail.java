package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class GoToJail {
	private final Passenger pass;
	
	public GoToJail(Passenger passenger){
		this.pass = passenger;
	}

	public Passenger getPassenger(){
		return pass;
	}
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_GO_TO_JAIL.value();
	}
}
