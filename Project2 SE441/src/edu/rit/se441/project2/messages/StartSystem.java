package edu.rit.se441.project2.messages;

public class StartSystem {
	private final Register registry;
	
	public StartSystem(final Register registry) {
		this.registry = registry;
	}
	
	public Register getRegister() {
		return registry;
	}
}
