/**
 * This message is used by System to send a new passenger to the document 
 * check.
 * 
 * @author Adam Meyer
 */

package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class NewPassenger {
	private final Passenger pass; 
	
	/**
	 * Constructor
	 * 
	 * @param pass - the immutable Passenger object
	 */
	public NewPassenger(Passenger pass){
		this.pass = pass;
	}
	
	/**
	 * This method returns the immutable object for the new passenger.
	 * 
	 * @return the passenger object
	 */
	public Passenger getPassenger(){
		return pass;
	}
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_NEW_PASSENGER.value();
	}
	
}
