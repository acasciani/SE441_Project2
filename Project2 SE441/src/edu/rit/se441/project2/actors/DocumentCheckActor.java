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
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
		//TODO HOW DO LINES INDIVIDUALLY REGISTER WITH DOCUMENT CHECK
		
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
		}
		
		// line register message
		if (arg0 instanceof Register){
			Register reg = (Register) arg0;
			int sender = reg.getSender();
			lineStatus[sender] = true;
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
		
		// receive new passenger TODO MAKE A DIFFERENT MESSAGE FOR THIS
		if (arg0 instanceof NewPassenger){
			NewPassenger msg = (NewPassenger) arg0;
			Passenger pass = msg.getPassenger();
			sendPassenger(pass);
		}
	}
	
	// this method is called when a new passenger must be sent to Line.
	private void sendPassenger(Passenger pass){
		boolean atLeastOneLineOpen = false;
		
		// make sure at least one line is functioning
		for (int x = 0; x < numLines; x++){
			if (lineStatus[currLine]){
				atLeastOneLineOpen = true;
			}
		}
		
		// if not, the passenger fails to send. tell the console
		if (!atLeastOneLineOpen){
			System.err.println("FAILED TO SEND A PASSENGER TO LINE, AS NO LINES ARE CURRENTLY OPEN!");
			return;
		}
		
		// choose the next available functioning line
		while (!lineStatus[currLine]){
			if (currLine == (numLines - 1)){
				currLine = 0;
			} else {
				currLine++;
			}
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

}
