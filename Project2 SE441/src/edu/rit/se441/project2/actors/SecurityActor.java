package edu.rit.se441.project2.actors;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.Exit;
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
 * SecurityActor knows the following - Their Line number (6.a.) - Must remember
 * reports sent in from Bag/Body check (6.b.) - JailActor (per 1.b. and 2.f.) -
 * SystemActor (per 2.f.)
 * 
 * SecurityActor receives: - Register - from Jail - BagCheckReport - from
 * BagCheck - BodyCheckReport - from BodyCheck
 * 
 * SecurityActor sends: - Register - to BodyCheck and BagCheck - GoToJail - to
 * Jail - Exit - to System
 * 
 * @author acc1728
 */
public class SecurityActor extends UntypedActor {
	private static final Logger logger = new Logger(SecurityActor.class);
	private static final String PASSENGER_LBL = Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER
			.value();
	private static final String BAGGAGE_LBL = Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE
			.value();
	private static final String MY_PARENT = Consts.NAME_ACTORS_BAG_CHECK + ", "
			+ Consts.NAME_ACTORS_BODY_CHECK;
	private final int lineNumber;
	private final HashMap<Passenger, HashMap<String, Boolean>> mapping;
	private ActorRef jailActor;
	private ActorRef systemActor;

	/*
	 * Class constructor
	 */
	public SecurityActor(final int lineNumber) {
		logger.debug("Security has coupled to its Line.");
		this.lineNumber = lineNumber;
		// allows us to keep track of passengers to baggage
		// this implementation only supports one bag!
		// But could theoretically support many with some modification
		mapping = new HashMap<Passenger, HashMap<String, Boolean>>();
	}

	/**
	 * Function processes incoming message types in form of an Object class.
	 * 
	 * @param message
	 * 
	 */
	public void onReceive(Object message) throws Exception {
		if (message instanceof Initialize) {
			if (childrenAreInitialized()) {
				logger.error("Security confirms that its children are initialized.");
				return;
			}
			logger.debug("Security has received an Initialize message.");
			messageReceived((Initialize) message);

		} else if (message instanceof BagCheckReport) {
			if (!childrenAreInitialized()) {
				logger.error("Security confirms that its children are not initialized.");
				return;
			}

			logger.debug("Security has received a BagCheckReport message.");
			messageReceived((BagCheckReport) message);

		} else if (message instanceof BodyCheckReport) {
			if (!childrenAreInitialized()) {
				logger.error("Security confirms that its children are not initialized.");
				return;
			}

			logger.debug("Security has received a BodyCheckReport message.");
			messageReceived((BodyCheckReport) message);

		}
	}

	/**
	 * Helper methods to hand off when messages are received
	 * */
	/*
	 * Function takes a BagCheckReport and determines whether the BagCheck's
	 * passenger passes security or not.
	 */
	private void messageReceived(BagCheckReport bagCheckReport) {
		Baggage baggage = bagCheckReport.getbaggage();
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		Consts jailLbl = Consts.NAME_ACTORS_JAIL;
		Consts sysLbl = Consts.NAME_ACTORS_SYSTEM;

		if (!doesBaggageExist(baggage)) {
			logger.debug("Adding %s [%s] to HashMap.", BAGGAGE_LBL, baggage);
			boolean allSet = bagHasArrived(baggage, bagCheckReport.didPass());

			if (allSet) {
				logger.debug("%s [%s] and %s [%s] have arrived at %s.",
						BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger, this);
				boolean goToJailAnswer = mapping.get(passenger)
						.get(BAGGAGE_LBL)
						&& mapping.get(passenger).get(PASSENGER_LBL);

				if (goToJailAnswer) {
					GoToJail goToJail = new GoToJail(passenger);
					logger.debug(
							"Either %s [%s] or %s [%s] didn't pass. Going to %s.",
							BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger,
							jailLbl);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS,
							goToJail, PASSENGER_LBL, passenger, jailLbl, this);
					jailActor.tell(goToJail);

				} else {
					Exit exitSystem = new Exit(passenger);
					logger.debug(
							"Both %s [%s] and %s [%s] passed. Leaving %s.",
							BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger,
							sysLbl);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS,
							exitSystem, PASSENGER_LBL, passenger, sysLbl, this);
					systemActor.tell(exitSystem);

				}
			} else {
				logger.debug(
						"%s [%s] has been added to the HashMap but %s [%s] has not (arrived) yet.",
						BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger);
			}
		} else {
			logger.debug("%s [%s] was already in HashMap.", BAGGAGE_LBL,
					baggage);
		}
	}

	/*
	 * Function takes a BodyCheckReport and determines whether the BodyCheck's
	 * passenger passes security or not.
	 */
	private void messageReceived(BodyCheckReport bodyCheckReport) {
		Passenger passenger = bodyCheckReport.getPassenger();
		Consts jailLbl = Consts.NAME_ACTORS_JAIL;
		Consts sysLbl = Consts.NAME_ACTORS_SYSTEM;

		if (!doesPassengerExist(passenger)) {
			logger.debug("Adding %s [%s] to HashMap.", PASSENGER_LBL, passenger);
			boolean allSet = passengerHasArrived(passenger,
					passenger.doesPassengerPass());

			if (allSet) {
				logger.debug("%s and %s [%s] have arrived at Security.",
						BAGGAGE_LBL, PASSENGER_LBL, passenger);
				boolean goToJailAnswer = mapping.get(
						bodyCheckReport.getPassenger()).get(BAGGAGE_LBL)
						&& mapping.get(bodyCheckReport.getPassenger()).get(
								PASSENGER_LBL);

				if (goToJailAnswer) {
					GoToJail goToJail = new GoToJail(passenger);
					logger.debug(
							"Either %s or %s [%s] didn't pass. Going to %s.",
							BAGGAGE_LBL, PASSENGER_LBL, passenger);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS,
							goToJail, PASSENGER_LBL, passenger, jailLbl, this);
					jailActor.tell(goToJail);

				} else {
					Exit exitSystem = new Exit(passenger);
					logger.debug("Both %s and %s [%s] passed. Leaving %s.",
							BAGGAGE_LBL, PASSENGER_LBL, passenger, sysLbl);
					logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS,
							exitSystem, PASSENGER_LBL, passenger, sysLbl, this);
					systemActor.tell(exitSystem);

				}
			}
		}
	}

	/*
	 * Function takes an Initialize message, sets its internal references, and
	 * tells its body and bag checks to initialize.
	 */
	private void messageReceived(Initialize initalize) {
		Consts bagChkLbl = Consts.NAME_ACTORS_BAG_CHECK;
		Consts bdyChkLbl = Consts.NAME_ACTORS_BODY_CHECK;

		logger.debug(Consts.DEBUG_MSG_INIT_MY_CHILD, Consts.NAME_ACTORS_JAIL);
		jailActor = initalize.getJailActor();
		systemActor = initalize.getSystemActor();

		logger.debug(Consts.DEBUG_MSG_TELL_PRT_TO_INIT, MY_PARENT);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, initalize, bagChkLbl,
				this);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, initalize, bdyChkLbl,
				this);
		initalize.getBagCheckActor(lineNumber).tell(initalize);
		initalize.getBodyCheckActor(lineNumber).tell(initalize);
	}

	/**
	 * Helper methods to do sub routine work
	 * */
	/*
	 * Function takes Baggage and determines whether the Baggage passes security
	 * or not.
	 */
	private boolean bagHasArrived(Baggage baggage, Boolean passesSecurity) {
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();

		if (!mapping.containsKey(passenger)) {
			logger.debug("%s [%s] is the first to arrive in the HashMap",
					BAGGAGE_LBL, baggage);
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER_LBL, null);
			mapping.get(passenger).put(BAGGAGE_LBL, passesSecurity);
			return false; // passenger not arrived yet
		} else {
			logger.debug("%s [%s] and %s [%s] are now in the HashMap",
					BAGGAGE_LBL, baggage, PASSENGER_LBL, passenger);
			mapping.get(passenger).put(BAGGAGE_LBL, passesSecurity);
			return true; // both arrived
		}
	}

	private boolean passengerHasArrived(Passenger passenger,
			Boolean passesSecurity) {
		if (!mapping.containsKey(passenger)) {
			logger.debug("%s [%s] is the first to arrive in the HashMap",
					PASSENGER_LBL, passenger);
			mapping.put(passenger, new HashMap<String, Boolean>());
			mapping.get(passenger).put(PASSENGER_LBL, passesSecurity);
			mapping.get(passenger).put(BAGGAGE_LBL, null);
			return false; // baggage not arrived yet
		} else {
			logger.debug("Passenger [%s] and %s are now in the HashMap",
					PASSENGER_LBL, passenger, BAGGAGE_LBL);
			mapping.get(passenger).put(PASSENGER_LBL, passesSecurity);
			return true; // both arrived
		}
	}

	/*
	 * Function takes a Passenger and determines whether it exists or not.
	 */
	private boolean doesPassengerExist(Passenger passenger) {
		logger.debug("Checking if %s [%s] is in the HashMap",
				Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER, passenger);
		return mapping.containsKey(passenger)
				&& mapping.get(passenger).get(PASSENGER_LBL) != null;
	}

	/*
	 * Function takes a Baggage object and determines whether it exists or not.
	 */
	private boolean doesBaggageExist(Baggage baggage) {
		Passenger passenger = baggage.whoDoesThisBaggageBelongTo();
		logger.debug("Checking if %s [%s] is in the HashMap",
				Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE, baggage);
		return mapping.containsKey(passenger)
				&& mapping.get(passenger).get(BAGGAGE_LBL) != null;
	}

	/*
	 * Function checks the class's children for initialization success
	 * */
	private boolean childrenAreInitialized() {
		return (jailActor != null);
	}

	/*
	 * Function returns the class's equivalent CONST from constants.java
	 */
	public String toString() {
		return Consts.NAME_ACTORS_SECURITY + " " + lineNumber;
	}
}
