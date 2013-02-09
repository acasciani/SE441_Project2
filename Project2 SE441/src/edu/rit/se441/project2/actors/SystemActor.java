/**
 * SystemActor represents the main administrative system in the TSA station.
 * 
 * @author Adam Meyer, Conor Craig, Alex Casciani, Ian Graves
 */
package edu.rit.se441.project2.actors;

import java.util.Arrays;
import java.util.Stack;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import edu.rit.se441.project2.messages.EndOfDay;
import edu.rit.se441.project2.messages.Initialize;
import edu.rit.se441.project2.messages.NewPassenger;
import edu.rit.se441.project2.messages.Register;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Logger;
import edu.rit.se441.project2.nonactors.Passenger;

public class SystemActor extends UntypedActor {
	private static final Logger logger = new Logger(SystemActor.class);
	private ActorRef jail = null;
	private ActorRef docCheck = null;
	private String[] names = { "Randy", "Michael", "Tony", "Jim", "Anthony", "Andre",
			"Steven", "Tyson", "Boomer", "Scott", "Mick", "Nick", "Les",
			"Will", "Adam", "Conor", "Ian", "Alex", "John", "James", "Tom",
			"Vick", "Adrian", "Greg", "Cortland", "Fred", "Stevie", "Vince",
			"Robert", "Eli", "Peyton", "Renee", "Amy", "Jess", "Ryan", "Amber",
			"Sam", "Emily", "Heather", "Monica", "Suzy", "Melissa", "Jill",
			"Courtney", "Chelsea", "Anne", "Eva", "Rebecca", "Sarah", "Laura",
			"Liana", "Molly", "Morgan", "Alexa", "Ashley", "Briana", "Alice",
			"Connie", "Tessa", "Elise", "Lindsay" };

	/**
	 * This method creates and sends four passengers to Document Checking at two
	 * second intervals until there are no passenger names left to go through.
	 */
	@Deprecated
	private void sendPassengers() throws InterruptedException {

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

				logger.debug("System sends a NewPassenger message.");
				docCheck.tell(newPass);

				if (stack.isEmpty()) {
					break;
				} else {
					Thread.sleep(1000L);
				}
			}

			Thread.sleep(2000L);
			times = 0;
		}

	}

	/**
	 * Function processes incoming message types in form of an Object class.
	 * 
	 * @param arg0
	 */
	public void onReceive(Object arg0) throws Exception {

		// Initialization message
		if (arg0 instanceof Initialize) {

			logger.debug("System has received an Initialize message.");
			Initialize init = (Initialize) arg0;

			// Extract the actor references
			jail = init.getJailActor();
			docCheck = init.getDocumentCheckActor();

			// Begin the registration process for jail and docCheck
			logger.debug("System has sent an initialize method to jail and docCheck.");

			jail.tell(init);
			docCheck.tell(init);
		}

		// End of day message
		else if (arg0 instanceof EndOfDay) {

			logger.debug("System has received an EndOfDay message.");

			// pass the message on to docCheck
			logger.debug("System has killed all processes");

			jail.stop();
			docCheck.stop();
			self().tell(Actors.poisonPill());
		}

		// Register message
		else if (arg0 instanceof Register) {

			logger.debug("System has received a Register message.");

			// pass the message on to docCheck
			//sendPassengers();
			sendFewPassengers();
		}
	}

	private void sendFewPassengers() {
		int times = 0;
		for (;;) {
			if (times == 2) {
				break;
			}
			NewPassenger newPass = new NewPassenger(new Passenger(names[times]));
			logger.debug("System sends a NewPassenger message to DocCheck.");
			docCheck.tell(newPass);
			times++;
		}
		logger.debug("System sends an EndOfDay message to DocCheck.");
		docCheck.tell(new EndOfDay());
	}

	/**
	 * Function returns the class's equivalent CONST from constants.java
	 */
	@Override
	public String toString() {
		return Consts.NAME_ACTORS_SYSTEM.value();
	}

}
