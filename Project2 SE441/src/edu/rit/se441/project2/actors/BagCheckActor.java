package edu.rit.se441.project2.actors;

import java.util.concurrent.ConcurrentLinkedQueue;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.GoToBagCheck;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;

public class BagCheckActor extends UntypedActor {
	private static final Logger logger = new Logger(BagCheckActor.class);
	private final int lineNumber;
	//private final ConcurrentLinkedQueue<Baggage> queue;
	private ActorRef securityActor;
	
	public BagCheckActor(final int lineNumber) {
		this.lineNumber = lineNumber;
		//queue = new ConcurrentLinkedQueue<Baggage>();
	}
	
	// TODO need to add shut down procedure
	
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof Register) {
			messageReceived((Register) message);
		} else if(message instanceof GoToBagCheck) {
			messageReceived((GoToBagCheck) message);
		}
	}
	
	
	private void messageReceived(GoToBagCheck goToBagCheck) {
		logger.debug("Received GoToBagCheck message from Line");
		
		if(!childrenAreRegistered()) {
			logger.error("My dependencies aren't registered yet!");
			//TODO what should we do here?
			return;
		}
		
		//queue.add(goToBagCheck.getBaggage());
		
		//Baggage baggage = queue.poll();
		Baggage baggage = goToBagCheck.getBaggage();
		BagCheckReport bagCheckReport = new BagCheckReport(baggage, baggage.doesBaggagePass());
		
		securityActor.tell(bagCheckReport);
		
	}
	
	private void messageReceived(Register register) {
		logger.debug("Received Register message from subordinate");
		
		if(childrenAreRegistered()) {
			//TODO what should we do here?
			return;
		}
		
		// I'm going to register all my dependencies, then tell my superior to register!
		securityActor = register.getSecurityActor(lineNumber);
		register.getLineActor(lineNumber).tell(register);
	}
	
	
	
	

	
	/**
	 * This may be worth to do before something like a Passenger is sent!
	 */
	private boolean childrenAreRegistered() {
		return (securityActor != null);
	}


}
