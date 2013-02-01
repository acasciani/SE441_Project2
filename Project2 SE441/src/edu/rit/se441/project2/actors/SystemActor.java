package edu.rit.se441.project2.actors;

import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.Register;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class SystemActor extends UntypedActor {
	ActorRef jail = null;
	ActorRef docCheck = null;
	@Override
	public void onReceive(Object arg0) throws Exception {
		
		//Initialization message
		if (arg0 instanceof Register){
			Register reg = (Register)arg0;
			
			//Extract the actor references
			jail = reg.getJailActor();
			docCheck = reg.getDocumentCheckActor();
			
			//Begin the registration process for jail and docCheck
			jail.tell(reg);
			docCheck.tell(reg);
		}
		
		//End of day message
		else if (arg0 instanceof EndOfDay){
			// pass the message on to docCheck
			docCheck.tell(arg0);
		}
	}

}
