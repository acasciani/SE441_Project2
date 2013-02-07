package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Baggage;
import edu.rit.se441.project2.nonactors.Consts;
import edu.rit.se441.project2.nonactors.Passenger;

public class GoToBagCheck {
	private final Baggage baggage;
	
	public GoToBagCheck(final Baggage baggage) {
		this.baggage = baggage;
	}
	
	public Baggage getBaggage() {
		return baggage;
	}

	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_GO_TO_BAG_CHECK.value();
	}
}
