package edu.rit.se441.project2.actors;

import java.util.concurrent.ConcurrentLinkedQueue;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.CanISendYouAPassenger;
import edu.rit.se441.project2.messages.GoToBagCheck;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.GoToLine;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;

/**
 * The first entity in the Line sub-system. The following requirements compose
 * this actor (per REQT.)
 * 
 * LineActor knows the following
 * - Their Line number (3.a.)
 * - Must know the queue of Passengers waiting for BodyCheck (2.c., 2.d.)
 * - BodyCheckActor (per 1.b.)
 * - BagCheckActor (per 1.b.)
 * 
 * LineActor receives:
 * - Register - from BodyCheck and BagCheck (though it will ignore the second one)
 * - GoToLine - from DocumentCheck
 * - BodyCheckRequestsNext - from BodyCheck (when the BodyCheck requests another passenger)
 * 
 * LineActor sends:
 * - Register - to DocumentCheck
 * - GoToBodyCheck - to BodyCheck
 * - GoToBagCheck - to BagCheck
 * - CanISendYouAPassenger - to BodyCheck
 * 
 * @author acc1728
 */
public class LineActor extends UntypedActor {
	private static final Logger logger = new Logger(LineActor.class);
	private static final String MY_CHLDRN = Consts.NAME_ACTORS_BAG_CHECK + ", " + Consts.NAME_ACTORS_BODY_CHECK;
	private static final String MY_PARENT = Consts.NAME_ACTORS_DOCUMENT_CHECK.value();
	private final int lineNumber;
	private final ConcurrentLinkedQueue<Passenger> queue;
	private ActorRef bagCheckActor;
	private ActorRef bodyCheckActor;
	
	public LineActor(final int lineNumber) {
		logger.debug(Consts.DEBUG_MSG_INSTAT_ACTOR, Consts.NAME_ACTORS_LINE, Consts.NAME_OTHER_OBJECTS_DRIVER);
		this.lineNumber = lineNumber;
		queue = new ConcurrentLinkedQueue<Passenger>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		Consts msgReceived = Consts.DEBUG_MSG_RECEIVED;
		
		if(message instanceof Register) {
			logger.debug(msgReceived, Consts.NAME_MESSAGES_REGISTER, MY_CHLDRN);
			messageReceived((Register) message);
			
		} else if(message instanceof GoToLine) {
			logger.debug(msgReceived, Consts.NAME_MESSAGES_GO_TO_LINE, Consts.NAME_ACTORS_DOCUMENT_CHECK);
			messageReceived((GoToLine) message);
			
		} else if(message instanceof BodyCheckRequestsNext) {
			logger.debug(msgReceived, Consts.NAME_MESSAGES_BODY_CHECK_REQUESTS_NEXT, Consts.NAME_ACTORS_BODY_CHECK);
			messageReceived((BodyCheckRequestsNext) message);
			
		}
	}	
	
	
	// Helper methods to hand off when messages are received
	private void messageReceived(Register register) {		
		if(childrenAreRegistered()) {
			logger.error(Consts.DEBUG_MSG_CHLD_ALR_REG, MY_CHLDRN);
			return;
		}
		
		Consts regLbl = Consts.NAME_MESSAGES_REGISTER;
		Consts docChkLbl = Consts.NAME_ACTORS_DOCUMENT_CHECK;
		Consts lineLbl = Consts.NAME_ACTORS_LINE;
		
		logger.debug(Consts.DEBUG_MSG_REG_MY_CHILD, MY_CHLDRN);
		bagCheckActor = register.getBagCheckActor(lineNumber);
		bodyCheckActor = register.getBodyCheckActor(lineNumber);
		
		logger.debug(Consts.DEBUG_MSG_TELL_PRT_TO_REG, MY_PARENT);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, regLbl, docChkLbl, lineLbl);
		register.getDocumentCheckActor().tell(register);
	}
	
	private void messageReceived(BodyCheckRequestsNext bagCheckNext) {		
		if(!childrenAreRegistered()) {
			logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, MY_CHLDRN);
			return;
		} else if(queue.isEmpty()) {
			logger.debug(Consts.DEBUG_MSG_LINE_NO1_IN_QUEUE);
			return;
		}
		
		Consts goToBdyChkLbl = Consts.NAME_MESSAGES_GO_TO_BODY_CHECK;
		Consts bodyChkLbl = Consts.NAME_ACTORS_BODY_CHECK;
		Consts lineLbl = Consts.NAME_ACTORS_LINE;
		Consts passengerLbl = Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER;
		
		Passenger passenger = queue.poll();
		GoToBodyCheck goToBodyCheck = new GoToBodyCheck(passenger);
		logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, goToBdyChkLbl, passengerLbl, passenger, bodyChkLbl, lineLbl);
		bodyCheckActor.tell(goToBodyCheck);
	}
	
	private void messageReceived(GoToLine goToLine) {		
		if(!childrenAreRegistered()) {
			logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, MY_CHLDRN);
			return;
		}
		
		Passenger passenger = goToLine.getPassenger();
		Consts canISendYouAPassLbl = Consts.NAME_MESSAGES_CAN_I_SEND_YOU_A_PASSENG;
		Consts goToBagChkLbl = Consts.NAME_MESSAGES_GO_TO_BAG_CHECK;
		Consts bodyChkLbl = Consts.NAME_ACTORS_BODY_CHECK;
		Consts bagChkLbl = Consts.NAME_ACTORS_BAG_CHECK;
		Consts lineLbl = Consts.NAME_ACTORS_LINE;
		Consts passengerLbl = Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER;
		Consts baggageLbl = Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE;
		
		// Per Reqt 2
		// d. Passengers can go to the body scanner only when it is ready
		// e. Passengers place their baggage in the baggage scanner as soon as they enter a queue
		GoToBagCheck goToBagCheck = new GoToBagCheck(passenger.getBaggage());
		CanISendYouAPassenger canISendPassenger = new CanISendYouAPassenger(getContext());
		
		queue.add(goToLine.getPassenger()); // 2.d.
		logger.debug(Consts.DEBUG_MSG_ADD_PASS_TO_QUEUE, passenger);
		
		bagCheckActor.tell(goToBagCheck); // 2.e.
		logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, goToBagChkLbl, baggageLbl, passenger.getBaggage(), bagChkLbl, lineLbl);
				
		bodyCheckActor.tell(canISendPassenger); // checks to make sure the body check is not occupied
		logger.debug(Consts.DEBUG_MSG_ASK_IF_I_CAN_SEND_MESSAGE, bodyChkLbl, lineLbl, passenger, canISendYouAPassLbl);
		logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, canISendYouAPassLbl, passengerLbl, passenger, bodyChkLbl, lineLbl);
	}

	private boolean childrenAreRegistered() {
		return (bagCheckActor != null) && (bodyCheckActor != null);
	}
}