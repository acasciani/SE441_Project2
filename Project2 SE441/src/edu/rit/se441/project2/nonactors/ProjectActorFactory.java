package edu.rit.se441.project2.nonactors;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

public class ProjectActorFactory {

	public ActorRef createActorRef(final UntypedActor actor) {
		UntypedActorFactory factory = new UntypedActorFactory() {
            public UntypedActor create() {
                return actor;
            }
        };
        
        return Actors.actorOf(factory);
	}

}
