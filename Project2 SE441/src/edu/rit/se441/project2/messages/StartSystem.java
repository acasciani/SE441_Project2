package edu.rit.se441.project2.messages;

import edu.rit.se441.project2.nonactors.Consts;

public class StartSystem {
	private final Register registry;
	
	public StartSystem(final Register registry) {
		this.registry = registry;
	}
	
	public Register getRegister() {
		return registry;
	}
	
	@Override
	public String toString() {
		return Consts.NAME_MESSAGES_START_SYSTEM.value();
	}
}
