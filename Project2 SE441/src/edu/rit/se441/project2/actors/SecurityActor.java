package edu.rit.se441.project2.actors;

import akka.actor.UntypedActor;

public class SecurityActor extends UntypedActor {
	private final int lineNumber;
	
	public SecurityActor(final int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
	}
	
}
