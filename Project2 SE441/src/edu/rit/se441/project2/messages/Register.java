package edu.rit.se441.project2.messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;

public class Register {
	private final ActorRef systemActor;
	private final ActorRef jailActor;
	private final ActorRef documentCheckActor;
	private final List<List<ActorRef>> lineActors;
	
	public Register(
			final ActorRef systemActor, 
			final ActorRef jailActor, 
			final ActorRef documentCheckActor,
			final List<List<ActorRef>> lineActors) {
		this.systemActor = systemActor;
		this.jailActor = jailActor;
		this.documentCheckActor = documentCheckActor;
		
		// The following guarantees this is ALWAYS immutable.
		List<List<ActorRef>> newList = new ArrayList<List<ActorRef>>();
		for(List<ActorRef> lineActor : lineActors) {
			newList.add(Collections.unmodifiableList(lineActor));
		}
		
		this.lineActors = Collections.unmodifiableList(newList);
	}
	
	public ActorRef getSystemActor() {
		return systemActor;
	}
	
	public ActorRef getJailActor() {
		return jailActor;
	}
	
	public ActorRef getDocumentCheckActor() {
		return documentCheckActor;
	}
	
	public ActorRef getBagCheckActor(final int lineNumber) {
		return lineActors.get(lineNumber).get(1);
	}
	
	public ActorRef getBodyCheckActor(final int lineNumber) {
		return lineActors.get(lineNumber).get(2);
	}
	
	public ActorRef getLineActor(final int lineNumber) {
		return lineActors.get(lineNumber).get(0);
	}
	
	public ActorRef getSecurityActor(final int lineNumber) {
		return lineActors.get(lineNumber).get(3);
	}
}
