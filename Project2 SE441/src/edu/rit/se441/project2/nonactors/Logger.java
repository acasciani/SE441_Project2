package edu.rit.se441.project2.nonactors;

import java.io.PrintStream;

/**
 * Handles all Logging. Must know if we are in sandbox mode or
 * production mode. If in production mode, and the passed in
 * Consts is printable to Console, it forwards that on.
 * 
 * Assumes Errors should always be printed.
 * 
 * @author acc1728
 */
public class Logger {
	private static final boolean debugEnabled = true;
	private static final boolean debugAsErr = true;
	private static final boolean productionMode = true;
	private final Class clazz;
	
	public Logger(final Class clazz) {
		this.clazz = clazz;
	}
	public void debug(String message, Object... args) {
		if(debugEnabled && !productionMode) {
			String template = String.format(Consts.LOGGER_DEBUG_ERR_OFF.value(), clazz.getCanonicalName(), message);
			PrintStream ps = System.out;
			
			if(debugAsErr) {
				ps = System.err;
				template = String.format(Consts.LOGGER_DEBUG_ERR_ON.value(), clazz.getCanonicalName(), message);
			}

			ps.printf(template, args);
		}
	}
	
	/**
	 * Checks if we are in production mode and if so, use Console.
	 */
	public void debug(Consts constants, Object... args) {
		if(productionMode) {
			if(constants.availableToConsole()) {
				ProjectConsole.printLine(constants.value(), args);
			}
		} else {
			debug(constants.value(), args);
		}
	}
	
	public void debug(String str){
		ProjectConsole.printLine(str);
	}
	
	public void error(String message, Object... args) {
		System.out.printf(Consts.LOGGER_ERROR.value(), clazz.getCanonicalName(), args);
	}
	
	public void error(String str){
		System.out.println("ERROR: " + str);
	}

	public void error(Consts constants, Object... args) {
		error(constants.value(), args);
	}
}
