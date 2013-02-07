package edu.rit.se441.project2.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.BagCheckReport;
import edu.rit.se441.project2.messages.GoToBagCheck;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;

/**
 * One of two of the second entities in the Line sub-system. 
 * The following requirements compose this actor (per REQT.)
 * 
 * BagCheckActor knows the following
 * - Their Line number (4.a.)
 * - SecurityActor (per 1.b.)
 * - Queue of Baggage (implicitly handled through mailbox) (2.e.)
 * 
 * BagCheckActor receives:
 * - Register - from Security
 * - GoToBagCheck - from Line
 * 
 * BagCheckActor sends:
 * - Register - to Line
 * - BagCheckReport - to Security
 * 
 * @author acc1728
 */
public class BagCheckActor extends UntypedActor {
	private static final Logger logger = new Logger(BagCheckActor.class);
	private static final String MY_CHLDRN = Consts.NAME_ACTORS_SECURITY.value();
	private static final String MY_PARENT = Consts.NAME_ACTORS_LINE.value();
	private final int lineNumber;
	private ActorRef securityActor;
	
	public BagCheckActor(final int lineNumber) {
		logger.debug(Consts.DEBUG_MSG_INSTAT_ACTOR, Consts.NAME_ACTORS_BAG_CHECK, Consts.NAME_OTHER_OBJECTS_DRIVER);
		this.lineNumber = lineNumber;
	}
	
	// TODO need to add shut down procedure
	
	@Override
	public void onReceive(Object message) throws Exception {
		Consts msgReceived = Consts.DEBUG_MSG_RECEIVED;
		
		if(message instanceof Initialize) {
			if(childrenAreInitialized()) {
				logger.error(Consts.DEBUG_MSG_CHLD_ALR_INIT, MY_CHLDRN);
				return;
			}
			
			logger.debug(msgReceived, Consts.NAME_MESSAGES_INIT, MY_CHLDRN);
			messageReceived((Initialize) message);
			
		} else if(message instanceof GoToBagCheck) {
			if(!childrenAreInitialized()) {
				logger.error(Consts.ERROR_MSG_CHLD_NOT_REG, MY_CHLDRN);
				return;
			}
			
			logger.debug(msgReceived, Consts.NAME_MESSAGES_GO_TO_BAG_CHECK, Consts.NAME_ACTORS_LINE);
			messageReceived((GoToBagCheck) message);
			
		}
	}
	

	// Helper methods to hand off when messages are received
	private void messageReceived(GoToBagCheck goToBagCheck) {		
		Consts baggageLbl = Consts.NAME_TRANSFERRED_OBJECTS_BAGGAGE;
		String securityLbl = Consts.NAME_ACTORS_SECURITY + " " + lineNumber;
		
		Baggage baggage = goToBagCheck.getBaggage();
		BagCheckReport bagCheckReport = new BagCheckReport(baggage, baggage.doesBaggagePass());
		
		logger.debug(Consts.DEBUG_MSG_SEND_OBJ_TO_IN_MESS, bagCheckReport, baggageLbl, baggage, securityLbl, this);
		securityActor.tell(bagCheckReport);
	}
	
	private void messageReceived(Initialize initialize) {
		Consts lineLbl = Consts.NAME_ACTORS_LINE;
		
		Register register = new Register(1);
		
		logger.debug(Consts.DEBUG_MSG_INIT_MY_CHILD, MY_CHLDRN);
		securityActor = initialize.getSecurityActor(lineNumber);
		
		logger.debug(Consts.DEBUG_MSG_TELL_PRT_TO_REG, MY_PARENT);
		logger.debug(Consts.DEBUG_MSG_TELL_PRT_TO_INIT, MY_PARENT);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, register, lineLbl, this);
		logger.debug(Consts.DEBUG_MSG_SEND_TO_MESSAGE, initialize, lineLbl, this);
		
		initialize.getLineActor(lineNumber).tell(initialize);
		initialize.getLineActor(lineNumber).tell(register);
	}

	private boolean childrenAreInitialized() {
		return (securityActor != null);
	}
	
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_BAG_CHECK + " " + lineNumber;
	}
}
