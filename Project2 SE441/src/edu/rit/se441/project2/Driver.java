package edu.rit.se441.project2;

import java.util.ArrayList;
import java.util.List;

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
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.nonactors.Logger;

/**
 * Starts all actors and passages initialize message
 * @author acc1728, ajm8774, clc1774, img4583
 */
public class Driver {
	public static void main(String[] args) {
		Logger logger = new Logger(Driver.class);
		
		int n=2;
		
		logger.debug("Usage: java Driver [n]");
		logger.debug("(n = integer number of lines) is optional. Deafult is 2");
		
		if(args.length >= 1) {
			try {
				n = Integer.valueOf(args[0].trim()).intValue();
			} catch(NumberFormatException e) {
				logger.debug(String.format("n={%s} is not an integer.", args[0].trim()));
			}
		}
		logger.debug(String.format("Using n={%s}", n));
		logger.debug("");
		
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
		
		Initialize register = new Initialize(systemActor.start(), jailActor.start(), documentCheckActor.start(), lineActors);
		
		systemActor.tell(register);
	}

}
