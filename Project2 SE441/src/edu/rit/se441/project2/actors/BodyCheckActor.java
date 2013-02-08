package edu.rit.se441.project2.actors;

import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class BodyCheckActor extends UntypedActor {
	private static final Logger logger = new Logger(BodyCheckActor.class);
	private final int lineNumber;
	private ActorRef myLine = null; // set after the init message
	private ActorRef mySecurity = null; // set after the init message

	public BodyCheckActor(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/*
	 * Function processes incoming message types in form of an Object class.
	 * 
	 * @param arg0
	 */
	public void onReceive(Object arg0) throws Exception {

		/*
		 * reception of initialization message
		 */
		if (arg0 instanceof Initialize) {
			logger.debug("BodyCheck " + lineNumber + " has received an Initialization message.");

			if (childrenAreRegistered()) {
				logger.debug("BodyCheck " + lineNumber +"'s children are confirmed to be registered.");
				return;
			}

			Initialize init = (Initialize) arg0;
			mySecurity = init.getSecurityActor(this.lineNumber);
			myLine = init.getLineActor(lineNumber);
			
			logger.debug("BodyCheck " + lineNumber + " sends a Register message to its Line.");
			myLine.tell(new Register(0));
		}

		/*
		 * Reception of GoToBodyCheck message
		 */
		if (arg0 instanceof GoToBodyCheck) {
			logger.debug("BodyCheck " + lineNumber + " has received a GoToBodyCheck message.");

			if (!childrenAreRegistered()) {
				logger.error("BodyCheck " + lineNumber + " is confirming that its children are not registered.");
				return;
			}

			GoToBodyCheck GoToBodyCheck = (GoToBodyCheck) arg0;
			performBodyCheck(GoToBodyCheck.getPassenger());
		}

		/*
		 * Reception of EndOfDay message
		 */
		if (arg0 instanceof EndOfDay) {
			logger.debug("BodyCheck " + lineNumber + " has received an EndOfDay message.");
			shutDown();
		}
	}

	/*
	 * Function is invoked by class when an EndOfDay message is received.
	 * Function clears all internal references and sends an EndOfDay message to
	 * its Security.
	 */
	private void shutDown() {
		// clear all references
		this.mySecurity = null;
		this.myLine = null;

		// send shutdown to children
		this.mySecurity.tell(new EndOfDay());
		logger.debug("BodyCheck " + lineNumber + " has sent an EndOfDay message to its Security.");
	}

	/*
	 * Function takes a passenger and messages Security whether it passes or
	 * fails. BodyCheckActor is accepting passengers after this message
	 */
	private void performBodyCheck(Passenger p) {
		BodyCheckReport myBodyReport;
		double n = Math.random();

		if (n > .5) {
			// Passes scan
			myBodyReport = new BodyCheckReport(p, true);
		} else {
			// Fails scan
			myBodyReport = new BodyCheckReport(p, false);
		}

		this.mySecurity.tell(myBodyReport);
		logger.debug("BodyCheck " + lineNumber + " has sent a BodyReport to its Security.");
		this.myLine.tell(new BodyCheckRequestsNext());
		logger.debug("BodyCheck " + lineNumber + " has sent a BodyCheckRequestsNext message to its line.");
	}

	/*
	 * This may be worth to do before something like a Passenger is sent!
	 */
	private boolean childrenAreRegistered() {
		return (mySecurity != null);
	}

	/*
	 * Function returns the class's equivalent CONST from constants.java
	 * */
	public String toString() {
		return Consts.NAME_ACTORS_BODY_CHECK + " " + lineNumber;
	}
}
