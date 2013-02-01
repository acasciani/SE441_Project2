package edu.rit.se441.project2.actors;

import java.util.Random;

import edu.rit.se441.project2.messages.BodyCheckReport;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.CanISendYouAPassenger;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.GoToJail;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Passenger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class BodyCheckActor extends UntypedActor {
	private final int lineNumber;
	private ActorRef mySecurity = null; // set after the register message
	private boolean isAcceptingPassengers = true; // set and reset during the
													// simulation

	public BodyCheckActor(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		// initialization message
		if (arg0 instanceof Register) {
			Register myReg = (Register) arg0;
			mySecurity = myReg.getSecurityActor(this.lineNumber);
		}

		/*
		 * Message From Line
		 */
		if (arg0 instanceof GoToBodyCheck) {
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
			if (this.isAcceptingPassengers) {
				CanISendYouAPassenger cISYAP = (CanISendYouAPassenger) arg0;
				ActorRef myLine = cISYAP.getLineActor();
				myLine.tell(new BodyCheckRequestsNext());
			} else {
				// swallow the message
			}
		}

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

}
