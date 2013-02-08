package edu.rit.se441.project2.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToBagCheck;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;

/**
 * One of two of the second entities in the Line sub-system. The following
 * requirements compose this actor (per REQT.)
 * 
 * BagCheckActor knows the following - Their Line number (4.a.) - SecurityActor
 * (per 1.b.) - Queue of Baggage (implicitly handled through mailbox) (2.e.)
 * 
 * BagCheckActor receives: - Register - from Security - GoToBagCheck - from Line
 * 
 * BagCheckActor sends: - Register - to Line - BagCheckReport - to Security
 * 
 * @author acc1728
 */
public class BagCheckActor extends UntypedActor {
	private static final Logger logger = new Logger(BagCheckActor.class);
	private final int lineNumber;
	private ActorRef securityActor;

	/*
	 * Class constructor 
	 */
	public BagCheckActor(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Function processes incoming message types in form of an Object class.
	 * 
	 * @param message
	 */
	public void onReceive(Object message) throws Exception {

		/*
		 * reception of initialization message
		 */
		if (message instanceof Initialize) {
			logger.debug("BagCheck "+ this.lineNumber+" has received an Initialization message.");
			if (childrenAreInitialized()) {
				logger.error("BagCheck "+ this.lineNumber+" is confirming that its children are initialized.");
				return;
			}

			messageReceived((Initialize) message);

		/*
		 * reception of GoToBagCheck message
		 */
		} else if (message instanceof GoToBagCheck) {
			logger.debug("BagCheck "+ this.lineNumber+" has received a GoToBagCheck message.");
			if (!childrenAreInitialized()) {
				logger.error("BagCheck "+ this.lineNumber+" is confirming that its children are not initialized.");
				return;
			}
			messageReceived((GoToBagCheck) message);

		/*
		 * Reception of EndOfDay message
		 */
		} else if (message instanceof EndOfDay) {
			logger.debug("BagCheck "+ this.lineNumber+" has received an EndOfDay message.");
			shutDown();
		}
	}
	
	/*
	 * Function clears internal references and sends an EndOfDay Message to children.
	 */
	private void shutDown() {
		
		// send shutdown to children
		logger.debug("BagCheck "+ this.lineNumber+" has sent an EndOfDay message to it's Security.");
		this.securityActor.tell(new EndOfDay());

		// clear all references
		this.securityActor = null;
	}

	/**
	 * Helper methods to hand off when messages are received
	 */
	/*
	 * Function takes a GoToBagCheck message, generates a BagCheckReport, and
	 * tells Security the BagCheckReport.
	 */
	private void messageReceived(GoToBagCheck goToBagCheck) {
		Baggage baggage = goToBagCheck.getBaggage();
		BagCheckReport bagCheckReport = new BagCheckReport(baggage,
				baggage.doesBaggagePass());

		logger.debug("BagCheck "+ this.lineNumber +" sent a bagCheckReport message to its Security.");
		securityActor.tell(bagCheckReport);

	}

	/*
	 * Function takes an Initialize message and passes Initialize messages to
	 * the class's internal references (children).
	 */
	private void messageReceived(Initialize initialize) {
		Register register = new Register(1);

		logger.debug("BagCheck "+ this.lineNumber+" sent an Initialize message to its Security.");
		securityActor = initialize.getSecurityActor(lineNumber);

		//logger.debug("BagCheck "+ this.lineNumber+" sent an Initialize message to its Line "+ this.lineNumber+ ".");
		logger.debug("BagCheck "+ this.lineNumber+" sent a Register message to its Line "+ this.lineNumber+ ".");

		//initialize.getLineActor(lineNumber).tell(initialize);
		initialize.getLineActor(lineNumber).tell(register);
	}

	/*
	 * Function checks for the registration of its internal references.
	 */
	private boolean childrenAreInitialized() {
		return (securityActor != null);
	}

	/*
	 * Function returns the class's equivalent CONST from constants.java
	 */
	public String toString() {
		return Consts.NAME_ACTORS_BAG_CHECK + " " + lineNumber;
	}
}
