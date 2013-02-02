package edu.rit.se441.project2.nonactors;

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
	private static final boolean productionMode = false;
	private final Class clazz;
	
	public Logger(final Class clazz) {
		this.clazz = clazz;
	}
	public void debug(String message, Object... args) {
		if(debugEnabled && !productionMode) {
			if(debugAsErr) {
				System.err.printf(Consts.LOGGER_DEBUG_ERR_ON.value(), clazz.getCanonicalName(), args);
			} else { 
				System.out.printf(Consts.LOGGER_DEBUG_ERR_OFF.value(), clazz.getCanonicalName(), args);
			}
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
	
	public void error(String message, Object... args) {
		System.out.printf(Consts.LOGGER_ERROR.value(), clazz.getCanonicalName(), args);
	}

	public void error(Consts constants, Object... args) {
		error(constants.value(), args);
	}
}
