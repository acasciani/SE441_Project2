/**
 * Security Actor represents the security station in a TSA station.
 * 
 * @author Adam Meyer, Alex Casciani
 */

package edu.rit.se441.project2.actors;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.GoToJail;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;

/**
 * The last entity in the Line sub-system. The following requirements compose
 * this system (per REQT.)
 * 
 * SecurityActor knows the following
 * - Their Line number (6.a.)
 * - Must remember reports sent in from Bag/Body check (6.b.)
 * - JailActor (per 1.b. and 2.f.)
 * - SystemActor (per 2.f.)
 * 
 * SecurityActor receives:
 * - Register - from Jail
 * - BagCheckReport - from BagCheck
 * - BodyCheckReport - from BodyCheck
 * 
 * SecurityActor sends:
 * - Register - to BodyCheck and BagCheck
 * - GoToJail - to Jail
 * - Exit - to System
 * 
 * @author acc1728
 */
public class SecurityActor extends UntypedActor {
	private static final Logger logger = new Logger(SecurityActor.class);
	private final int lineNumber;
	private final HashMap<Passenger, HashMap<String, Boolean>> mapping;
	private static final String PASSENGER = Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER.value(); 
	private static final String BAGGAGE = Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE.value(); 

	private ActorRef jailActor;
	
	public SecurityActor(final int lineNumber) {
		this.lineNumber = lineNumber;
		
		// allows us to keep track of passengers to baggage
		// this implementation only supports one bag!
		// But could theoretically support many with some modification
		mapping = new HashMap<Passenger, HashMap<String, Boolean>>();
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		
		// initialization message
		if(message instanceof Initialize) {
			
			logger.debug("Security " + lineNumber + " has received an Initialize message.");
			
			if(childrenAreInitialized()) {
				logger.error("Security " + lineNumber + " is already initialized! Rejecting message!");
				return;
			}
		
			messageReceived((Initialize) message);
			
		// completed bag check message
		} else if(message instanceof BagCheckReport) {
			
			logger.debug("Security " + lineNumber + " has received a BagCheckReport message.");
			
			if(!childrenAreInitialized()) {
				logger.error("Security " + lineNumber + " is not yet accepting messages! Rejecting message!");
				return;
			}
			
			messageReceived((BagCheckReport) message);
			
		// completed body check message
		} else if(message instanceof BodyCheckReport) {

			logger.debug("Security " + lineNumber + " has received a BodyCheckReport message.");
			
			if(!childrenAreInitialized()) {
				logger.error("Security " + lineNumber + " is not yet accepting messages! Rejecting message!");
				return;
			}
			
			messageReceived((BodyCheckReport) message);
		}
	}
	

	/**
	 * This method is called when a BagCheckReport message is received.
	 * 
	 * @param bagCheckReport - the message
	 */
	private void messageReceived(BagCheckReport bagCheckReport) {
		Baggage baggage = bagCheckReport.getbaggage();
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		
		if(!doesBaggageExist(baggage)) {
			logger.debug("Security " + lineNumber + ": Adding " + baggage.toString() + " to hashMap.");
			
			if(bagHasArrived(baggage, bagCheckReport.didPass())) {
				boolean goToJailAnswer = mapping.get(passenger).get(BAGGAGE) && 
						mapping.get(passenger).get(PASSENGER);
				
				if(goToJailAnswer) {
					GoToJail goToJail = new GoToJail(passenger);
					logger.debug("Security " + lineNumber + ": " + passenger.toString() + "'s bag or body check has failed.");
					logger.debug("Security " + lineNumber + " has sent a GoToJail message.");
					jailActor.tell(goToJail);
					
				} else {
					logger.debug("Security " + lineNumber + ": " + passenger.toString() + "'s bag and body check have passed.");
					logger.debug("Security " + lineNumber + ": " + passenger.toString() + " has left the system.");

					//TODO does this work?
					mapping.remove(passenger);
				}
			} else {
				logger.debug("Security " + lineNumber + ": " + baggage.toString() + "'s results have arrived. They have been added to the hashmap." );
			}
			
		// this should never happen and would be very bad
		} else {
			logger.debug("Security " + lineNumber + ": " + baggage.toString() + "'s results were already in the db! Rejecting these results!");
		}
	}
	
	/**
	 * This method is called when a BodyCheckReport is received.
	 * 
	 * @param bodyCheckReport - the message
	 */
	private void messageReceived(BodyCheckReport bodyCheckReport) {		
		Passenger passenger = bodyCheckReport.getPassenger();
		
		if(!doesPassengerExist(passenger)) {
			logger.debug("Security " + lineNumber + ": Adding " + passenger.toString() + " to hashMap.");
			
			
			if(passengerHasArrived(passenger, passenger.doesPassengerPass())) {
				boolean goToJailAnswer = mapping.get(bodyCheckReport.getPassenger()).get(BAGGAGE) && 
						mapping.get(bodyCheckReport.getPassenger()).get(PASSENGER);
				
				if(goToJailAnswer) {
					GoToJail goToJail = new GoToJail(passenger);
					logger.debug("Security " + lineNumber + ": " + passenger.toString() + "'s bag or body check has failed.");
					logger.debug("Security " + lineNumber + " has sent a GoToJail message.");
					jailActor.tell(goToJail);
					
				} else {
					logger.debug("Security " + lineNumber + ": " + passenger.toString() + "'s bag and body check have passed.");
					logger.debug("Security " + lineNumber + ": " + passenger.toString() + " has left the system.");
					
					//TODO does this work?
					mapping.remove(passenger);
				}
			}
		// this should never happen and would be very bad
		} else {
			logger.debug("Security " + lineNumber + ": " + passenger.toString() + "'s results were already in the db! Rejecting these results!");
		}
	}
	
	/**
	 * This method is called when an initialize message is received.
	 * 
	 * @param initalize
	 */
	private void messageReceived(Initialize initalize) {	
		
		jailActor = initalize.getJailActor();
		
		logger.debug("Security " + lineNumber + " has sent an initialize message to bag and body check!");
		initalize.getBagCheckActor(lineNumber).tell(initalize);
		initalize.getBodyCheckActor(lineNumber).tell(initalize);
	}
	
	/**
	 * This method is called when a bag check result is received via a 
	 * BagCheckReport message.
	 * 
	 * @param baggage - the baggage item
	 * @param passesSecurity - a boolean representing the outcome of the test
	 * 
	 * @return a boolean representing whether or not the passenger has also 
	 * 		arrived.
	 */
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
	
	/**
	 * This method is called when a body check result is received via a 
	 * BodyCheckReport message.
	 * 
	 * @param passenger - the passenger item
	 * @param passesSecurity - a boolean representing the outcome of the test
	 * 
	 * @return a boolean representing whether or not the baggage test results 
	 * 		of the passenger has also arrived.
	 */
	private boolean passengerHasArrived(Passenger passenger, Boolean passesSecurity) {		
		if(!mapping.containsKey(passenger)) {
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER, passesSecurity);
			mapping.get(passenger).put(BAGGAGE, null);
			return false; // baggage not arrived yet
		} else {
			mapping.get(passenger).put(PASSENGER, passesSecurity);
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

	private boolean childrenAreInitialized() {
		return (jailActor != null);
	}
	
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_SECURITY + " " + lineNumber;
	}
}
