package edu.rit.se441.project2.nonactors;

public class Logger {
	private static final boolean debugEnabled = true;
	private static final boolean debugAsErr = true;
	private final Class clazz;
	
	public Logger(final Class clazz) {
		this.clazz = clazz;
	}
	
	public void debug(String message, String... args) {
		if(debugEnabled) {
			if(debugAsErr) {
				System.err.printf("DEBUG [%s]: %s %n", clazz.getCanonicalName(), args);
			} else { 
				System.out.printf("DEBUG [%s]: %s %n", clazz.getCanonicalName(), args);
			}
		}
	}
	
	public void error(String message, String... args) {
		System.out.printf("ERROR [%s]: %s %n", clazz.getCanonicalName(), args);
	}
}
