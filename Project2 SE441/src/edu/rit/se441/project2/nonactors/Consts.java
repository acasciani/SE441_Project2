package edu.rit.se441.project2.nonactors;

public enum Consts {
	LOGGER_DEBUG_ERR_OFF		("DEBUG [%s]: %s %n"),
	LOGGER_DEBUG_ERR_ON			("DEBUG [%s]: %s %n"),
	LOGGER_ERROR				("ERROR [%s]: %s %n"),
	DEBUG_MSG_RECEIVED			("%s message received from %s");
	
	
	
	private String value;
	
	Consts(String value) {
		this.value = value;
	}
	

	public String value() {
		return value;
	}
}
