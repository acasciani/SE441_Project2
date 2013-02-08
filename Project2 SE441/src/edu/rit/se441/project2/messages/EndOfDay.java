package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;

public class EndOfDay {
	private final int id;
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_END_OF_DAY.value();
	}
	
	/**
	 * used when identification will be needed with an endofday messages. 
	 * Security will use their line number as their id, while bodycheck 
	 * will use 0 and bagcheck will use 1.
	 * 
	 * @param id - the id number
	 */
	public EndOfDay(int id){
		this.id = id;
	}
	
	/**
	 * used when no identification will be needed with an endofday message
	 */
	public EndOfDay(){
		this.id = -1;
	}
	
	/**
	 * used to get the ID of who sent you the message.
	 * 
	 * @return the id number.
	 */
	public int getId(){
		return id;
	}
}
