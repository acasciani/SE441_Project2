package edu.rit.se441.project2.actors;

import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.CanISendYouAPassenger;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.Register;
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

}
