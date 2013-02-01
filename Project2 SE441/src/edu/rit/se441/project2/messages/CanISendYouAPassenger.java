package edu.rit.se441.project2.messages;

import akka.actor.ActorRef;

public class CanISendYouAPassenger {
	private final ActorRef lineActor;
	
	public CanISendYouAPassenger(final ActorRef lineActor) {
		this.lineActor = lineActor;
	}
	
	public ActorRef getLineActor() {
		return lineActor;
	}

}
