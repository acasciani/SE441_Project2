package edu.rit.se441.project2.actors;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;

public class SecurityActor extends UntypedActor {
	private static final Logger logger = new Logger(SecurityActor.class);
	private static final String PASSENGER = "Passenger";
	private static final String BAGGAGE   = "Baggage";
	private final int lineNumber;
	private final HashMap<Passenger, HashMap<String, Boolean>> mapping;
	private ActorRef jailActor;
	
	public SecurityActor(final int lineNumber) {
		this.lineNumber = lineNumber;
		// allows us to keep track of passengers to baggage
		// this implementation only supports one bag!
		mapping = new HashMap<Passenger, HashMap<String, Boolean>>();
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		String msgReceived = Consts.DEBUG_MSG_RECEIVED.value();
		if(message instanceof Register) {
			logger.debug(msgReceived, "Register", "Jail");
			messageReceived((Register) message);
			
		} else if(message instanceof BagCheckReport) {
			logger.debug(msgReceived, "BagCheckReport", "BagCheck");
			messageReceived((BagCheckReport) message);
			
		} else if(message instanceof BodyCheckReport) {
			logger.debug(msgReceived, "BodyCheckReport", "BodyCheck");
			messageReceived((BodyCheckReport) message);
			
		}
	}
	
	
	
	private void messageReceived(BagCheckReport bagCheckReport) {
		logger.debug("Received BagCheckReport message from BagCheck");
		
		if(!childrenAreRegistered()) {
			//TODO
			return;
		}

		if(!doesBaggageExist(bagCheckReport.getbaggage())) {
			boolean allSet = bagHasArrived(bagCheckReport.getbaggage(), bagCheckReport.didPass());
			
			if(allSet) {
				boolean goToJailAnswer = mapping.get(bagCheckReport.getbaggage().whoDoesThisBaggageBelongTo()).get(BAGGAGE);
				if(goToJailAnswer) {
					//tell person to go to jail
				} else {
					// exit system
				}
			}
		}
	}
	
	private void messageReceived(BodyCheckReport bodyCheckReport) {
		logger.debug("Received BodyCheckReport message from BodyCheck");
		
		if(!childrenAreRegistered()) {
			//TODO
			return;
		}
		
		if(!doesPassengerExist(bodyCheckReport.getPassenger())) {
			boolean allSet = passengerHasArrived(bodyCheckReport.getPassenger());
			
			if(allSet) {
				boolean goToJailAnswer = mapping.get(bodyCheckReport.getPassenger()).get(BAGGAGE);
				if(goToJailAnswer) {
					//tell person to go to jail
				} else {
					// exit system
				}
			}
		}
		
	}
	
	private void messageReceived(Register register) {
		logger.debug("Received Register message from subordinates");
		
		if(childrenAreRegistered()) {
			return;
		}
		
		jailActor = register.getJailActor();
		register.getBagCheckActor(lineNumber).tell(register);
		register.getBodyCheckActor(lineNumber).tell(register);
	}
	
	
	
	private boolean bagHasArrived(Baggage baggage, Boolean passesSecurity) {
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		if(!mapping.containsKey(passenger)) {
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER, null);
			mapping.get(passenger).put(BAGGAGE, passesSecurity);
			
			return false; // passenger not arrived yet
		} else {
			mapping.get(passenger).put(BAGGAGE, passesSecurity);
			
			return true; // both arrived
		}
	}
	
	private boolean passengerHasArrived(Passenger passenger) {
		if(!mapping.containsKey(passenger)) {
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER, true);
			mapping.get(passenger).put(BAGGAGE, null);
			
			return false; // baggage not arrived yet
		} else {
			mapping.get(passenger).put(PASSENGER, true);
			
			return true; // both arrived
		}
	}

	
	private boolean doesPassengerExist(Passenger passenger) {
		return mapping.containsKey(passenger) && 
				mapping.get(passenger).get(PASSENGER) != null;
	}
	
	private boolean doesBaggageExist(Baggage baggage) {
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		return mapping.containsKey(passenger) &&
				mapping.get(passenger).get(BAGGAGE) != null;
	}

	private boolean childrenAreRegistered() {
		return (jailActor != null);
	}

	
}
