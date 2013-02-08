package edu.rit.se441.project2.actors;

import java.util.Arrays;
import java.util.Stack;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.NewPassenger;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class SystemActor extends UntypedActor {
	ActorRef jail = null;
	ActorRef docCheck = null;

	/*
	 * Function creates and sends four passengers to Document Checking at two
	 * second intervals until there are no passenger naems left to go through.
	 */
	private void sendPassengers() throws InterruptedException {
		String[] names = { "Randy", "Michael", "Tony", "Jim", "Anthony",
				"Andre", "Steven", "Tyson", "Boomer", "Scott", "Mick", "Nick",
				"Les", "Will", "Adam", "Conor", "Ian", "Alex", "John", "James",
				"Tom", "Vick", "Adrian", "Greg", "Cortland", "Fred", "Stevie",
				"Vince", "Robert", "Eli", "Peyton", "Renee", "Amy", "Jess",
				"Ryan", "Amber", "Sam", "Emily", "Heather", "Monica", "Suzy",
				"Melissa", "Jill", "Courtney", "Chelsea", "Anne", "Eva",
				"Rebecca", "Sarah", "Laura", "Liana", "Molly", "Morgan",
				"Alexa", "Ashley", "Briana", "Alice", "Connie", "Tessa",
				"Elise", "Lindsay" };

		Stack<String> stack = new Stack<String>();
		stack.addAll(Arrays.asList(names));

		int times = 0;

		while (!stack.isEmpty()) {
			if (times > 5) {
				break;
			}

			for (int i = 0; i < 4; i++) {
				times++;
				NewPassenger newPass = new NewPassenger(new Passenger(
						stack.pop()));
				docCheck.tell(newPass);
				if (stack.isEmpty()) {
					break;
				} else {
					Thread.sleep(1000L);
				}
			}

			Thread.sleep(2000L);
		}

	}

	/*
	 * Function processes incoming message types in form of an Object class.
	 * 
	 * @param arg0
	 */
	public void onReceive(Object arg0) throws Exception {

		// Initialization message
		if (arg0 instanceof Initialize) {
			Initialize init = (Initialize) arg0;

			// Extract the actor references
			jail = init.getJailActor();
			docCheck = init.getDocumentCheckActor();

			// Begin the registration process for jail and docCheck
			jail.tell(init);
			docCheck.tell(init);
		}

		// End of day message
		else if (arg0 instanceof EndOfDay) {
			// pass the message on to docCheck
			docCheck.tell(arg0);
		}
		// Register message
		else if (arg0 instanceof Register) {
			// pass the message on to docCheck
			sendPassengers();
		}
	}

	/*
	 * Function returns the class's equivalent CONST from constants.java
	 * */
	public String toString() {
		return Consts.NAME_ACTORS_SYSTEM.value();
	}

}
