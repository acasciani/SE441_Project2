package edu.rit.se441.project2.actors;

import akka.actor.UntypedActor;

public class LineActor extends UntypedActor {
	private final int lineNumber;
	
	public LineActor(final int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
	}
	
}
