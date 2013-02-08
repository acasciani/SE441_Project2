package edu.rit.se441.project2.actors;

import java.util.ArrayList;

import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToLine;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.NewPassenger;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class DocumentCheckActor extends UntypedActor {
	private static final Logger logger = new Logger(LineActor.class);
	int numLines = 0;
	int currLine = 0;
	private boolean acceptMessages = false;
	ArrayList<ActorRef> lineList = new ArrayList<ActorRef>();
	boolean[] lineStatus;
	boolean linesReady = false;
	ActorRef system;

	@Override
	public void onReceive(Object arg0) throws Exception {

		// initialization message
		if (arg0 instanceof Initialize) {
			logger.debug("DocCheck has received an Initialize method.");

			Initialize init = (Initialize) arg0;
			numLines = init.getNumberOfLines();

			// create a list of the lines
			for (int x = 0; x < numLines; x++) {
				lineList.add(init.getLineActor(x));
			}

			// create a status list for the lines
			lineStatus = new boolean[numLines];

			// remember the system actor reference
			system = init.getSystemActor();
			
			// start accepting messages
			acceptMessages = true;
		}

		// line register message
		if (arg0 instanceof Register) {
			
			logger.debug("DocCheck has receieved a Register message.");
			
			// if this message is premature
			if (!acceptMessages){
				logger.debug("DocCheck is not yet accepting messages! Message rejected!");
				return;
			}
						
			Register reg = (Register) arg0;
			int sender = reg.getSender();
			lineStatus[sender] = true;

			// are all lines ready?
			for (int x = 0; x < numLines; x++) {
				if (!lineStatus[x]) {
					linesReady = false;
					break;
				}

				// if so, flip the switch and register with systemActor
				linesReady = true;
			}
			if(linesReady) {
				register();
			}
		}

		// end of day message
		if (arg0 instanceof EndOfDay) {
			
			logger.debug("DocCheck has received an EndOfDay message.");
			
			// if this message is premature
			if (!acceptMessages){
				logger.debug("DocumentChecker is not yet accepting messages! Message rejected!");
				return;
			}
			
			shutdown();
			
		}

		// receive new passenger
		if (arg0 instanceof NewPassenger) {
			
			logger.debug("DocChecker has received a NewPassenger message.");
			
			// if this message is premature
			if (!acceptMessages){
				logger.debug("DocumentChecker is not yet accepting messages! Message rejected!");
				return;
			}
			
			// see sendPassenger message
			NewPassenger msg = (NewPassenger) arg0;
			Passenger pass = msg.getPassenger();
			sendPassenger(pass);
		}
	}

	// this method is called when a new passenger must be sent to Line.
	private void sendPassenger(Passenger pass) {

		// send the passenger
		GoToLine msg = new GoToLine(pass);
		logger.debug("DocCheck is sending a GoToLine message to line " + currLine);
		lineList.get(currLine).tell(msg);

		// iterate to the next line in the group
		if (currLine == (numLines - 1)) {
			currLine = 0;
		} else {
			currLine++;
		}
	}

	private void register() {
		logger.debug("DocCheck is sending a Register message to System.");
		system.tell(new Register(0));
	}
	
	private void shutdown(){
		
		
		
		// stopAccepting all messages
		acceptMessages = false;
				
		// pass the message on to the Lines
		for (int x = 0; x < numLines; x++) {
			logger.debug("DocCheck is sending an EndOfDay message to Line " + x);
			lineList.get(x).tell(new EndOfDay());
		}
		
		// erase the lineList and reset numLines to zero.
		lineList = null;
		numLines = 0;
		
	}
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_DOCUMENT_CHECK.value();
	}
}