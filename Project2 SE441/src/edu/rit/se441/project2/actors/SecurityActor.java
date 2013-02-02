package edu.rit.se441.project2.actors;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.Exit;
import edu.rit.se441.project2.messages.GoToJail;
import edu.rit.se441.project2.messages.Register;
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
	private static final String PASSENGER_LBL = Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER.value();
	private static final String BAGGAGE_LBL   = Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE.value();
	private static final String MY_PARENT = Consts.NAME_ACTORS_BAG_CHECK + ", " + Consts.NAME_ACTORS_BODY_CHECK;
	private final int lineNumber;
	private final HashMap<Passenger, HashMap<String, Boolean>> mapping;
	private ActorRef jailActor;
	private ActorRef systemActor;
	
	public SecurityActor(final int lineNumber) {
		logger.debug(Consts.DEBUG_MSG_INSTAT_ACTOR, Consts.NAME_ACTORS_SECURITY, Consts.NAME_OTHER_OBJECTS_DRIVER);
		this.lineNumber = lineNumber;
		// allows us to keep track of passengers to baggage
		// this implementation only supports one bag!
		// But could theoretically support many with some modification
		mapping = new HashMap<Passenger, HashMap<String, Boolean>>();
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		Consts msgReceived = Consts.DEBUG_MSG_RECEIVED;
		if(message instanceof Register) {
			logger.debug(msgReceived, Consts.NAME_MESSAGES_REGISTER, Consts.NAME_ACTORS_JAIL);
			messageReceived((Register) message);
			
		} else if(message instanceof BagCheckReport) {
			logger.debug(msgReceived, Consts.NAME_MESSAGES_BAG_CHECK_REPORT, Consts.NAME_ACTORS_BAG_CHECK);
			messageReceived((BagCheckReport) message);
			
		} else if(message instanceof BodyCheckReport) {
			logger.debug(msgReceived, Consts.NAME_MESSAGES_BODY_CHECK_REPORT, Consts.NAME_ACTORS_BODY_CHECK);
			messageReceived((BodyCheckReport) message);
			
		}
	}
	

	// Helper methods to hand off when messages are received
	private void messageReceived(BagCheckReport bagCheckReport) {
		if(!childrenAreRegistered()) {
			logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, Consts.NAME_ACTORS_JAIL);
			return;
		}

		Baggage baggage = bagCheckReport.getbaggage();
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		Consts securityLbl = Consts.NAME_ACTORS_SECURITY;
		Consts jailLbl = Consts.NAME_ACTORS_JAIL;
		Consts sysLbl = Consts.NAME_ACTORS_SYSTEM;
		
		if(!doesBaggageExist(baggage)) {
			logger.debug("Adding %s [%s] to HashMap.", BAGGAGE_LBL, baggage);
			boolean allSet = bagHasArrived(baggage, bagCheckReport.didPass());
			
			if(allSet) {
				logger.debug("%s [%s] and %s [%s] have arrived at %s.", BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger, securityLbl);
				boolean goToJailAnswer = mapping.get(passenger).get(BAGGAGE_LBL);
				
				if(goToJailAnswer) {
					GoToJail goToJail = new GoToJail(passenger);
					logger.debug("Either %s [%s] or %s [%s] didn't pass. Going to %s.", BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger, jailLbl);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, Consts.NAME_MESSAGES_GO_TO_JAIL, PASSENGER_LBL, passenger, jailLbl, securityLbl);
					jailActor.tell(goToJail);
					
				} else {
					Exit exitSystem = new Exit(passenger);
					logger.debug("Both %s [%s] and %s [%s] passed. Leaving %s.", BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger, sysLbl);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, Consts.NAME_MESSAGES_EXIT, PASSENGER_LBL, passenger, sysLbl, securityLbl);
					systemActor.tell(exitSystem);
					
				}
			} else {
				logger.debug("%s [%s] has been added to the HashMap but %s [%s] has not (arrived) yet.", BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger);
			}
		} else {
			logger.debug("%s [%s] was already in HashMap.", BAGGAGE_LBL, baggage);
		}
	}
	
	private void messageReceived(BodyCheckReport bodyCheckReport) {
		if(!childrenAreRegistered()) {
			logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, Consts.NAME_ACTORS_JAIL);
			return;
		}
		
		Passenger passenger = bodyCheckReport.getPassenger();
		Consts securityLbl = Consts.NAME_ACTORS_SECURITY;
		Consts jailLbl = Consts.NAME_ACTORS_JAIL;
		Consts sysLbl = Consts.NAME_ACTORS_SYSTEM;
		
		if(!doesPassengerExist(passenger)) {
			logger.debug("Adding %s [%s] to HashMap.", PASSENGER_LBL, passenger);
			boolean allSet = passengerHasArrived(passenger);
			
			if(allSet) {
				logger.debug("%s and %s [%s] have arrived at Security.", BAGGAGE_LBL, PASSENGER_LBL, passenger);
				boolean goToJailAnswer = mapping.get(bodyCheckReport.getPassenger()).get(BAGGAGE_LBL);
				
				if(goToJailAnswer) {
					GoToJail goToJail = new GoToJail(passenger);
					logger.debug("Either %s or %s [%s] didn't pass. Going to %s.", BAGGAGE_LBL, PASSENGER_LBL, passenger);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, Consts.NAME_MESSAGES_GO_TO_JAIL, PASSENGER_LBL, passenger, jailLbl, securityLbl);
					jailActor.tell(goToJail);
					
				} else {
					Exit exitSystem = new Exit(passenger);
					logger.debug("Both %s and %s [%s] passed. Leaving %s.", BAGGAGE_LBL, PASSENGER_LBL, passenger, sysLbl);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, Consts.NAME_MESSAGES_EXIT, PASSENGER_LBL, passenger, sysLbl, securityLbl);
					systemActor.tell(exitSystem);
					
				}
			}
		}
	}
	
	private void messageReceived(Register register) {		
		if(childrenAreRegistered()) {
			logger.error(Consts.DEBUG_MSG_CHLD_ALR_REG, Consts.NAME_ACTORS_JAIL);
			return;
		}
		
		Consts regLbl = Consts.NAME_MESSAGES_REGISTER;
		Consts bagChkLbl = Consts.NAME_ACTORS_BAG_CHECK;
		Consts bdyChkLbl = Consts.NAME_ACTORS_BODY_CHECK;
		Consts securiLbl = Consts.NAME_ACTORS_SECURITY;
		
		logger.debug(Consts.DEBUG_MSG_REG_MY_CHILD, Consts.NAME_ACTORS_JAIL);
		jailActor = register.getJailActor();
		systemActor = register.getSystemActor();
		
		logger.debug(Consts.DEBUG_MSG_TELL_PRT_TO_REG, MY_PARENT);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, regLbl, bagChkLbl, securiLbl);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, regLbl, bdyChkLbl, securiLbl);
		register.getBagCheckActor(lineNumber).tell(register);
		register.getBodyCheckActor(lineNumber).tell(register);
	}
	
	// Helper methods to do sub routine work
	private boolean bagHasArrived(Baggage baggage, Boolean passesSecurity) {
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		
		if(!mapping.containsKey(passenger)) {
			logger.debug("%s [%s] is the first to arrive in the HashMap", BAGGAGE_LBL, baggage);
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER_LBL, null);
			mapping.get(passenger).put(BAGGAGE_LBL, passesSecurity);
			return false; // passenger not arrived yet
		} else {
			logger.debug("%s [%s] and %s [%s] are now in the HashMap", BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger);
			mapping.get(passenger).put(BAGGAGE_LBL, passesSecurity);
			return true; // both arrived
		}
	}
	
	private boolean passengerHasArrived(Passenger passenger) {		
		if(!mapping.containsKey(passenger)) {
			logger.debug("%s [%s] is the first to arrive in the HashMap", PASSENGER_LBL, passenger);
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER_LBL, true);
			mapping.get(passenger).put(BAGGAGE_LBL, null);
			return false; // baggage not arrived yet
		} else {
			logger.debug("Passenger [%s] and %s are now in the HashMap", PASSENGER_LBL, passenger, BAGGAGE_LBL);
			mapping.get(passenger).put(PASSENGER_LBL, true);
			return true; // both arrived
		}
	}
	
	private boolean doesPassengerExist(Passenger passenger) {
		logger.debug("Checking if %s [%s] is in the HashMap", Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER, passenger);
		return mapping.containsKey(passenger) && 
				mapping.get(passenger).get(PASSENGER_LBL) != null;
	}
	
	private boolean doesBaggageExist(Baggage baggage) {
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		logger.debug("Checking if %s [%s] is in the HashMap", Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE, baggage);
		return mapping.containsKey(passenger) &&
				mapping.get(passenger).get(BAGGAGE_LBL) != null;
	}

	private boolean childrenAreRegistered() {
		return (jailActor != null);
	}
}
