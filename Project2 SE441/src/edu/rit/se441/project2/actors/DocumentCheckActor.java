package edu.rit.se441.project2.actors;

import java.util.ArrayList;

import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToLine;
import edu.rit.se441.project2.messages.Register;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class DocumentCheckActor extends UntypedActor {
	int numLines = 0;
	int currLine = 0;
	ArrayList<ActorRef> lineList = new ArrayList<ActorRef>();
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
		//TODO HOW DO LINES INDIVIDUALLY REGISTER WITH DOC CHECK
		
		// initialization message 
		if (arg0 instanceof Register){
			Register reg = (Register) arg0;
			numLines = reg.getNumberOfLines();
			
			// create a list of the lines
			for (int x = 0; x < numLines; x++){
				lineList.add(reg.getLineActor(x));
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
		
		// receive new passenger TODO MAKE A DIFFERENT MESSAGE FOR THIS
		if (arg0 instanceof GoToLine){
			GoToLine msg = (GoToLine) arg0;
			sendPassenger(msg);
		}
	}
	
	// this method is called when a new passenger must be sent to Line.
	private void sendPassenger(GoToLine msg){
		lineList.get(currLine).tell(msg);
		if (currLine == (numLines - 1)){
			currLine = 0;
		} else {
			currLine++;
		}
	}

}
