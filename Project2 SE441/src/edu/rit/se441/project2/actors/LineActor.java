package edu.rit.se441.project2.actors;

import java.util.concurrent.ConcurrentLinkedQueue;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.GoToBagCheck;
import edu.rit.se441.project2.messages.GoToBodyCheck;
import edu.rit.se441.project2.messages.GoToLine;
import edu.rit.se441.project2.messages.Initialize;
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
	private boolean bagCheckRegistered = false;
	private boolean bodyCheckRegistered = false;
	private ActorRef docCheckActor;
	private boolean isBodyCheckOccupied = false;
	
	public LineActor(final int lineNumber) {
		logger.debug(Consts.DEBUG_MSG_INSTAT_ACTOR, Consts.NAME_ACTORS_LINE, Consts.NAME_OTHER_OBJECTS_DRIVER);
		this.lineNumber = lineNumber;
		queue = new ConcurrentLinkedQueue<Passenger>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		Consts msgReceived = Consts.DEBUG_MSG_RECEIVED;
		
		if(message instanceof Register) {
			logger.debug(msgReceived, message, MY_CHLDRN);
			messageReceived((Register) message);
			
		} else if(message instanceof Initialize) {
			if(childrenAreInitialized()) {
				logger.error(Consts.DEBUG_MSG_CHLD_ALR_INIT, MY_CHLDRN);
				return;
			}
			
			logger.debug(msgReceived, message, MY_CHLDRN);
			messageReceived((Initialize) message);
			
		} else if(message instanceof GoToLine) {
			if(!childrenAreInitialized()) {
				logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, MY_CHLDRN);
				return;
			}
			
			logger.debug(msgReceived, message, Consts.NAME_ACTORS_DOCUMENT_CHECK);
			messageReceived((GoToLine) message);
			
		} else if(message instanceof BodyCheckRequestsNext) {
			if(!childrenAreInitialized()) {
				logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, MY_CHLDRN);
				return;
			}
			
			logger.debug(msgReceived, message, Consts.NAME_ACTORS_BODY_CHECK);
			messageReceived((BodyCheckRequestsNext) message);
			
		}
	}	
	
	
	// Helper methods to hand off when messages are received
	private void messageReceived(Register register) {
		// body check= 0; bag check=1
		Consts[] children = {Consts.NAME_ACTORS_BODY_CHECK, Consts.NAME_ACTORS_BAG_CHECK};
		
		if(bagCheckRegistered && bodyCheckRegistered) {
			logger.error(Consts.DEBUG_MSG_CHLD_ALR_REG, MY_CHLDRN);
			return;
		} else if(bagCheckRegistered && register.getSender() == 1) {
			logger.error(Consts.DEBUG_MSG_CHLD_ALR_REG, children[1]);
			return;
		} else if(bodyCheckRegistered && register.getSender() == 0) {
			logger.error(Consts.DEBUG_MSG_CHLD_ALR_REG, children[0]);
			return;
		} else if(register.getSender() != 0 && register.getSender() != 1) {
			logger.error("The sender{%s} is unknown", register.getSender());
			return;
		}
		
		Consts regLbl = Consts.NAME_MESSAGES_REGISTER;
		Consts docChkLbl = Consts.NAME_ACTORS_DOCUMENT_CHECK;
		Consts lineLbl = Consts.NAME_ACTORS_LINE;
		
		logger.debug(Consts.DEBUG_MSG_REG_MY_CHILD, children[register.getSender()]);
		if(register.getSender() == 0) {
			bodyCheckRegistered = true;
		} else if(register.getSender() == 1) {
			bagCheckRegistered = true;
		}
		
		if(bodyCheckRegistered && bagCheckRegistered) {
			logger.debug(Consts.DEBUG_MSG_TELL_PRT_TO_REG, MY_PARENT);
			logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, regLbl, docChkLbl, lineLbl);
			docCheckActor.tell(new Register(lineNumber));
		}
	}
	
	private void messageReceived(Initialize initialize) {		
		logger.debug(Consts.DEBUG_MSG_INIT_MY_CHILD, MY_CHLDRN);
		bagCheckActor = initialize.getBagCheckActor(lineNumber);
		bodyCheckActor = initialize.getBodyCheckActor(lineNumber);
		docCheckActor = initialize.getDocumentCheckActor();
	}
	
	private void messageReceived(BodyCheckRequestsNext bagCheckNext) {		
		if(queue.isEmpty()) {
			logger.debug(Consts.DEBUG_MSG_LINE_NO1_IN_QUEUE);
			return;
		}
		
		isBodyCheckOccupied = false;
		sendNextPassengerToBodyCheck();
	}
	
	private void messageReceived(GoToLine goToLine) {		
		Passenger passenger = goToLine.getPassenger();
		Consts baggageLbl = Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE;
		
		// Per Reqt 2
		// d. Passengers can go to the body scanner only when it is ready
		// e. Passengers place their baggage in the baggage scanner as soon as they enter a queue
		GoToBagCheck goToBagCheck = new GoToBagCheck(passenger.getBaggage());
		
		logger.debug("Adding passenger to the queue for %s", this);
		queue.add(passenger); // 2.d.
		sendNextPassengerToBodyCheck();
		
		bagCheckActor.tell(goToBagCheck); // 2.e.
		logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, goToBagCheck, baggageLbl, passenger.getBaggage(), bagCheckActor, this);
	}
	
	private void sendNextPassengerToBodyCheck() {
		if(isBodyCheckOccupied) {
			logger.debug("Cannot send passenger to body check, it is occupied");
			
		} else {			
			Passenger passengerToSend = queue.poll();
			Consts passengerLbl = Consts.NAME_TRANSFERRED_OBJECTS_PASSENGER;
			
			GoToBodyCheck goToBodyCheck = new GoToBodyCheck(passengerToSend);
			logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, goToBodyCheck, passengerLbl, passengerToSend, bodyCheckActor, this);
			bodyCheckActor.tell(goToBodyCheck);
			
		}
	}

	private boolean childrenAreInitialized() {
		return (bagCheckRegistered == true) && (bodyCheckRegistered == true);
	}
	
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_LINE + " " + lineNumber;
	}
}