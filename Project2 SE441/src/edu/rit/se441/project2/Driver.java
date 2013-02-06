package edu.rit.se441.project2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import edu.rit.se441.project2.actors.BagCheckActor;
import edu.rit.se441.project2.actors.BodyCheckActor;
import edu.rit.se441.project2.actors.DocumentCheckActor;
import edu.rit.se441.project2.actors.JailActor;
import edu.rit.se441.project2.actors.LineActor;
import edu.rit.se441.project2.actors.SecurityActor;
import edu.rit.se441.project2.actors.SystemActor;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.messages.StartSystem;
import edu.rit.se441.project2.nonactors.ProjectActorFactory;

public class Driver {
	
	public static void main(String[] args) {
		ProjectActorFactory actorFactory = new ProjectActorFactory();
		int n = 4;
		
		ActorRef systemActor = Actors.actorOf(SystemActor.class);
		ActorRef jailActor = Actors.actorOf(JailActor.class);
		ActorRef documentCheckActor = Actors.actorOf(DocumentCheckActor.class);
				
		List<List<ActorRef>> lineActors = new ArrayList<List<ActorRef>>();
		
		for(int i=0; i<n; i++) {
			List<ActorRef> insideLineActors = new ArrayList<ActorRef>();
			final int lineNumber = i;
			
			ActorRef lineActor = Actors.actorOf(
					new UntypedActorFactory() {
			            public UntypedActor create() {
			                return new LineActor(lineNumber);
			            }
			        });
			
			ActorRef bagCheckActor = Actors.actorOf(
					new UntypedActorFactory() {
			            public UntypedActor create() {
			                return new BagCheckActor(lineNumber);
			            }
			        });
			
			ActorRef bodyCheckActor = Actors.actorOf(
					new UntypedActorFactory() {
			            public UntypedActor create() {
			                return new BodyCheckActor(lineNumber);
			            }
			        });
			
			ActorRef securityActor = Actors.actorOf(
					new UntypedActorFactory() {
			            public UntypedActor create() {
			                return new SecurityActor(lineNumber);
			            }
			        });
						
			insideLineActors.add(lineActor.start());
			insideLineActors.add(bagCheckActor.start());
			insideLineActors.add(bodyCheckActor.start());
			insideLineActors.add(securityActor.start());
			
			lineActors.add(insideLineActors);
		}
		
		Register register = new Register(systemActor.start(), jailActor.start(), documentCheckActor.start(), lineActors);
		
		systemActor.tell(register);
	}

}
