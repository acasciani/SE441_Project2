package edu.rit.se441.project2.actors;

import java.util.ArrayList;

import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.Register;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class DocumentCheckActor extends UntypedActor {
	int numLines = 0;
	ArrayList<ActorRef> lineList = new ArrayList<ActorRef>();
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
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
		
		// TODO NEW PASSENGER MESSAGE
	}

}
