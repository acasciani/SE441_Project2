package edu.rit.se441.project2.nonactors;

/**
 * This utility will print stuff to the console.
 * It should ONLY be called from the logger.
 * @author acc1728
 */
class ProjectConsole {	
	private ProjectConsole() { }
	
	static void printLine(String line, Object... arguments) {
		System.out.printf(line, arguments);
	}

}
