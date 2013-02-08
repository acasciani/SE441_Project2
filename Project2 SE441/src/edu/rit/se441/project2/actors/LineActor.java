/**
 * This actor class represents a single line in a TSA system.
 * 
 * @author Adam Meyer, Alex Casciani
 */
package edu.rit.se441.project2.actors;

import java.util.concurrent.ConcurrentLinkedQueue;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BodyCheckRequestsNext;
import edu.rit.se441.project2.messages.EndOfDay;
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
	private final int lineNumber;
	private final ConcurrentLinkedQueue<Passenger> queue;
	private ActorRef bagCheckActor;
	private ActorRef bodyCheckActor;
	private boolean bagCheckRegistered = false;
	private boolean bodyCheckRegistered = false;
	private ActorRef docCheckActor;
	private boolean isBodyCheckOccupied = false;
	private boolean isAcceptingNewPassengers = true;
	
	public LineActor(final int lineNumber) {
		this.lineNumber = lineNumber;
		queue = new ConcurrentLinkedQueue<Passenger>();
	}

	
	@Override
	public void onReceive(Object message) throws Exception {
		
		// register message
		if(message instanceof Register) {
			
			logger.debug("Line " + lineNumber + " has received a Register message.");
			messageReceived((Register) message);
			
		// initialization message
		} else if(message instanceof Initialize) {
			
			logger.debug("Line " + lineNumber + " has received an Initialize message.");
			if(childrenAreInitialized()) {
				logger.error("Line " + lineNumber + " is already initialized! Rejecting message!");
				return;
			}
			
			messageReceived((Initialize) message);
			
		// new passenger message
		} else if(message instanceof GoToLine) {
			
			logger.debug("Line " + lineNumber + " has received a GoToLine message.");
			if(!childrenAreInitialized()) {
				logger.error("Line " + lineNumber + " is not yet accepting messages! Rejecting message!");
				return;
			}
			
			messageReceived((GoToLine) message);
			
		// bodyChecker ready message
		} else if(message instanceof BodyCheckRequestsNext) {
			
			logger.debug("Line " + lineNumber + " has received a BodyCheckRequestsNext message.");
			
			if(!childrenAreInitialized() && isAcceptingNewPassengers) {
				logger.error("Line " + lineNumber + " is not yet accepting messages! Rejecting message!");
				return;
			}
			
			messageReceived((BodyCheckRequestsNext) message);
			
		} else if(message instanceof EndOfDay){
			
			logger.debug("Line " + lineNumber + " has received an EndOfDay message.");
			
			if(!childrenAreInitialized()) {
				logger.error("Line " + lineNumber + " is not yet accepting messages! Rejecting message!");
				return;
			}
			
			shutdown();
		}
	}	
	
	
	/**
	 * this method is called when a Register message is received. 
	 * 
	 * @param register - the received message
	 */
	private void messageReceived(Register register) {
		
		logger.debug("Line " + lineNumber + " has received a Register message.");
		// body check= 0; bag check=1
				
		if(bagCheckRegistered && register.getSender() == 1) {
			logger.error("Line " + lineNumber + ": error: bagCheck is already registered! Rejecting message!");
			return;
		} else if(bodyCheckRegistered && register.getSender() == 0) {
			logger.error("Line " + lineNumber + ": error: bodyCheck is already registered! Rejecting message!");
			return;
		} else if(register.getSender() != 0 && register.getSender() != 1) {
			logger.error("Line " + lineNumber + ": error: unknown entity attempting to register!");
			return;
		}
		
		if(register.getSender() == 0) {
			bodyCheckRegistered = true;
		} else if(register.getSender() == 1) {
			bagCheckRegistered = true;
		}
		
		if(bodyCheckRegistered && bagCheckRegistered) {
			logger.debug("Line " + lineNumber + " is sending a Register message to DocCheck.");
			docCheckActor.tell(new Register(lineNumber));
		}
	}
	
	/**
	 * This method is called when an Initialize message is received.
	 * 
	 * @param initialize - the message
	 */
	private void messageReceived(Initialize initialize) {		
		bagCheckActor = initialize.getBagCheckActor(lineNumber);
		bodyCheckActor = initialize.getBodyCheckActor(lineNumber);
		docCheckActor = initialize.getDocumentCheckActor();
		isAcceptingNewPassengers = true;
	}
	
	/**
	 * This method is called when a BodyCheckRequestsNext message is received.
	 * 
	 * @param bagCheckNext - the message
	 */
	private void messageReceived(BodyCheckRequestsNext bagCheckNext) {		
		if(queue.isEmpty()){
			isBodyCheckOccupied = false;
		} else {
			sendNextPassengerToBodyCheck();
		}
		
		if(!isAcceptingNewPassengers){
			shutdown();
		}
	}
	
	/**
	 * This method is called when a GoToLine message is received.
	 * 
	 * @param goToLine
	 */
	private void messageReceived(GoToLine goToLine) {		
		Passenger passenger = goToLine.getPassenger();
		
		// Per Reqt 2
		// d. Passengers can go to the body scanner only when it is ready
		// e. Passengers place their baggage in the baggage scanner as soon as they enter a queue
		GoToBagCheck goToBagCheck = new GoToBagCheck(passenger.getBaggage());
		
		logger.debug("Line " + lineNumber + ": A new passenger has been added to the queue.");
		queue.add(passenger); // 2.d.
		
		if (!isBodyCheckOccupied){
			sendNextPassengerToBodyCheck();
		}
		
		logger.debug("Line " + lineNumber + " has sent a goToBagCheck message.");		
		bagCheckActor.tell(goToBagCheck); // 2.e.
	}
	
	private void shutdown(){
		if (queue.isEmpty() && bagCheckActor != null && bodyCheckActor != null){
			
			logger.debug("Line " + lineNumber + " has sent an EndOfDay emssage to bag and body checks.");
			bagCheckActor.tell(new EndOfDay());
			bodyCheckActor.tell(new EndOfDay());
			
			bagCheckActor = null;
			bodyCheckActor = null;
			bagCheckRegistered = false;
			bodyCheckRegistered = false;
		}
		
		isAcceptingNewPassengers = false;
		
	}
	
	/**
	 * This method is called to send a passenger to the body check in 
	 * response to a new passenger added to the queue (if the body check was 
	 * currently empty) or in response to a bodyCheckRequestsNext message
	 */
	private void sendNextPassengerToBodyCheck() {
			
		Passenger passengerToSend = queue.poll();
		GoToBodyCheck goToBodyCheck = new GoToBodyCheck(passengerToSend);
			
		logger.debug("Line " + lineNumber + " has sent a GoToBodyCheck message.");
		bodyCheckActor.tell(goToBodyCheck);
			
	}

	/**
	 * This method is called to check whether or not LineActor has finished 
	 * it's initialization, including waiting on the bag and body checks' 
	 * register messages.
	 * 
	 * @return true if initialization has finished, else, false.
	 */
	private boolean childrenAreInitialized() {
		return (bagCheckRegistered == true) && (bodyCheckRegistered == true);
	}
	
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_LINE + " " + lineNumber;
	}
}