package edu.rit.se441.project2.actors;

import java.util.ArrayList;

import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToLine;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.NewPassenger;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Passenger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class DocumentCheckActor extends UntypedActor {
	int numLines = 0;
	int currLine = 0;
	ArrayList<ActorRef> lineList = new ArrayList<ActorRef>();
	boolean[] lineStatus;
	boolean linesReady = false;
	ActorRef system;
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
		// initialization message 
		if (arg0 instanceof Initialize){
			Initialize init = (Initialize) arg0;
			numLines = init.getNumberOfLines();
			
			// create a list of the lines
			for (int x = 0; x < numLines; x++){
				lineList.add(init.getLineActor(x));
			}
			
			// create a status list for the lines
			lineStatus = new boolean[numLines];
			
			// remember the system actor reference
			system = init.getSystemActor();
		}
		
		// line register message
		if (arg0 instanceof Register){
			Register reg = (Register) arg0;
			int sender = reg.getSender();
			lineStatus[sender] = true;
			
			// are all lines ready?
			for (int x = 0; x < numLines; x++){
				if (!lineStatus[x]){
					break;
				}
				
				// if so, flip the switch and register with systemActor
				linesReady = true;
				register();
			}
		}
		
		// end of day message
		if (arg0 instanceof EndOfDay){
			
			//pass the message on to the Lines
			for (int x = 0; x < numLines; x++){
				lineList.get(x).tell(arg0);
			}
			
			//erase the lineList and reset numLines to zero.
			lineList = null;
			numLines = 0;
		}
		
		// receive new passenger
		if (arg0 instanceof NewPassenger){
			NewPassenger msg = (NewPassenger) arg0;
			Passenger pass = msg.getPassenger();
			sendPassenger(pass);
		}
	}
	
	// this method is called when a new passenger must be sent to Line.
	private void sendPassenger(Passenger pass){
		boolean atLeastOneLineOpen = false;
		
		// make sure at the lines are functioning
		for (int x = 0; x < numLines; x++){
			if (lineStatus[currLine]){
				atLeastOneLineOpen = true;
			}
		}
		
		// if not, the passenger fails to send. tell the console.
		if (!atLeastOneLineOpen){
			System.err.println("FAILED TO SEND A PASSENGER TO LINE, AS NO LINES ARE CURRENTLY OPEN!");
			return;
		}
		
		// send the passenger
		GoToLine msg = new GoToLine(pass);
		lineList.get(currLine).tell(msg);
		
		// iterate to the next line in the group
		if (currLine == (numLines - 1)){
			currLine = 0;
		} else {
			currLine++;
		}
	}

	private void register(){
		system.tell(new Register(0));
	}
}
