/**
 * Register is a message class used to 
 * 
 * @author Adam Meyer
 */
package edu.rit.se441.project2.messages;

public class Register {
	private final int sender;
	
	/**
	 * Constructor
	 * 
	 * @param sender - the sender of this message
	 */
	public Register(int sender){
		this.sender = sender;
	}
	
	/**
	 * This method returns the # of the component which sent the message (if 
	 * the component is a line, this number is the line number. If the 
	 * component is a bag checker, the number is a 0. If the component
	 * is a body checker, the number is a 1.
	 * 
	 * @return the sender's identification number
	 */
	public int getSender(){
		return sender;
	}
	
}
