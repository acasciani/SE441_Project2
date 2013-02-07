package edu.rit.se441.project2.actors;

import java.util.ArrayList;

import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.GoToJail;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class JailActor extends UntypedActor {
	int numLines;
	ArrayList<ActorRef> securityList = null;
	ArrayList<Passenger> prisonerList = null;
	
	@Override
	public void onReceive(Object arg0) throws Exception {
		
		//initialization message
		if (arg0 instanceof Initialize){
			Initialize init = (Initialize) arg0;
			numLines = init.getNumberOfLines();
			securityList = new ArrayList<ActorRef>();
			prisonerList = new ArrayList<Passenger>();
			
			//pass message to security and remember the security actors
			for (int x = 0; x < numLines; x++){
				securityList.add(x,init.getSecurityActor(x));
				init.getSecurityActor(x).tell(init);
			}
		}
		
		//new prisoner message
		if (arg0 instanceof GoToJail){
			GoToJail msg = (GoToJail) arg0;
			
			//extract the passenger and insert them into the list of prisoners
			Passenger pass = msg.getPassenger();
			prisonerList.add(pass);
		}
		
		//end of day message
		if (arg0 instanceof EndOfDay){
			
			//empty the prisonerList and the securityList
			securityList = null;
			prisonerList = null;
			
			//POSSIBLE TODO: DOES THE JAIL NEED TO INFORM ANYONE ELSE THAT IT IS FINISHED?
		}
	}
	
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_JAIL.value();
	}

}
