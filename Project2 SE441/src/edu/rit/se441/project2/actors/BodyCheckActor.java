package edu.rit.se441.project2.actors;

import java.util.Random;

import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.CanISendYouAPassenger;
import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class BodyCheckActor extends UntypedActor {
	private static final Logger logger = new Logger(BodyCheckActor.class);
	private final int lineNumber;
	private ActorRef myLine = null; //set after the init message
	private ActorRef mySecurity = null; // set after the init message
	private boolean isAcceptingPassengers = true; // set and reset during the
													// simulation

	public BodyCheckActor(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		
		
		// initialization message
		if (arg0 instanceof Initialize) {
			logger.debug("Received Register message from subordinate");
			
			if(childrenAreRegistered()) {
				logger.error("My dependencies aren't registered yet!");
				//TODO what should we do here?
				//returning is fine
				return;
			}

			Initialize init = (Initialize) arg0;
			mySecurity = init.getSecurityActor(this.lineNumber);
			myLine = init.getLineActor(lineNumber);
			
			myLine.tell(new Register(1));
		}

		/*
		 * Message From Line
		 */
		if (arg0 instanceof GoToBodyCheck) {
			logger.debug("Received GoToBodyCheck message from Line");
			this.isAcceptingPassengers = false;
			
			//critical problem area below
			GoToBodyCheck GoToBodyCheck = (GoToBodyCheck) arg0;
			performBodyCheck(GoToBodyCheck.getPassenger());
			//end critical problem area
		}

		/*
		 * Message From Line
		 */
		if (arg0 instanceof CanISendYouAPassenger) {
			logger.debug("Received CanISendYouAPassenger message from Line");
			if (this.isAcceptingPassengers) {
				CanISendYouAPassenger cISYAP = (CanISendYouAPassenger) arg0;
				ActorRef myLine = cISYAP.getLineActor();
				myLine.tell(new BodyCheckRequestsNext());
			} else {
				// swallow the message
			}
		}

		
		if (arg0 instanceof EndOfDay) {
			logger.debug("Received EndOfDay message from Line");
			//TODO what to do here?
			shutDown();
			
		}
	}


	private void shutDown() {
		// TODO Auto-generated method stub
		
	}

	//Function takes a passenger and messages Security whether it passes or fails
	//BodyCheckActor is accepting passengers after this message
	private void performBodyCheck(Passenger p) {
		double n = Math.random();
		BodyCheckReport myBodyReport;
		if (n > .5) {
			// Passes scan
			myBodyReport = new BodyCheckReport(p, true);
		} else {
			// Fails scan
			myBodyReport = new BodyCheckReport(p, false);
		}
		this.mySecurity.tell(myBodyReport);
		this.isAcceptingPassengers = true;

	}
	
	/**
	 * This may be worth to do before something like a Passenger is sent!
	 */
	private boolean childrenAreRegistered() {
		return (mySecurity != null);
	}

}
