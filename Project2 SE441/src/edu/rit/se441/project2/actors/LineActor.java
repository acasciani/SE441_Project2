package edu.rit.se441.project2.actors;

import java.util.concurrent.ConcurrentLinkedQueue;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.CanISendYouAPassenger;
import edu.rit.se441.project2.messages.GoToBagCheck;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.GoToLine;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Passenger;

public class LineActor extends UntypedActor {
	private final int lineNumber;
	private final ConcurrentLinkedQueue<Passenger> queue;
	private ActorRef bagCheckActor;
	private ActorRef bodyCheckActor;
	private ActorRef securityActor;
	
	public LineActor(final int lineNumber) {
		this.lineNumber = lineNumber;
		queue = new ConcurrentLinkedQueue<Passenger>();
	}
	
	
	
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof Register) {
			messageReceived((Register) message);
		} else if(message instanceof GoToLine) {
			messageReceived((GoToLine) message);
		} else if(message instanceof BodyCheckRequestsNext) {
			messageReceived((BodyCheckRequestsNext) message);
		}
	}	
	
	
	private void messageReceived(Register register) {
		log("Received Register message from subordinates");
		
		if(childrenAreRegistered()) {
			//TODO what should we do here?
			return;
		}
		
		// I'm going to register all my dependencies, then tell my superior to register!
		bagCheckActor = register.getBagCheckActor(lineNumber);
		bodyCheckActor = register.getBodyCheckActor(lineNumber);
		securityActor = register.getSecurityActor(lineNumber);
		register.getDocumentCheckActor().tell(register);
	}
	

	private void messageReceived(BodyCheckRequestsNext bagCheckNext) {
		log("Received BodyCheckRequestsNext message from BagCheck");
		
		if(!childrenAreRegistered()) {
			log("My children/dependencies are not registered!");
			//TODO what should we do here?
			return;
		} else if(queue.isEmpty()) {
			log("There is no one in the queue to send to BodyCheck");
			return;
		}
		
		Passenger passenger = queue.poll();
		log("Sending Passenger{%s} to BodyCheck", passenger.toString());
		
		GoToBodyCheck goToBodyCheck = new GoToBodyCheck(passenger);
		bodyCheckActor.tell(goToBodyCheck);
	}
	
	
	private void messageReceived(GoToLine goToLine) {
		log("Received GoToLine message from DocumentCheck");
		
		if(!childrenAreRegistered()) {
			//TODO what should we do here?
			log("The children (dependencies) are not registered");
			return;
		}
		
		Passenger passenger = goToLine.getPassenger();
		
		// Per Reqt 2
		// d. Passengers can go to the body scanner only when it is ready
		// e. Passengers place their baggage in the baggage scanner as soon as they enter a queue
		log("Adding Passenger{%s} to my queue so he can wait for body scanner", passenger.toString());
		log("Sending Baggage{%s} to bag check", passenger.getBaggage().toString());
		
		GoToBagCheck goToBagCheck = new GoToBagCheck(passenger.getBaggage());
		CanISendYouAPassenger canISendPassenger = new CanISendYouAPassenger(getContext());
		
		queue.add(goToLine.getPassenger()); // 2.d.
		bagCheckActor.tell(goToBagCheck); // 2.e.
		bodyCheckActor.tell(canISendPassenger); // checks to make sure the body check is not occupied
	}
	

	
	/**
	 * This may be worth to do before something like a Passenger is sent!
	 */
	private boolean childrenAreRegistered() {
		return (bagCheckActor != null) && (bodyCheckActor != null) && (securityActor != null);
	}
	
	private void log(String message, String... args) {
		String className = this.getClass().getCanonicalName();
		System.err.printf("LOG[%s]: %s %n", className, args);
	}
	
}
