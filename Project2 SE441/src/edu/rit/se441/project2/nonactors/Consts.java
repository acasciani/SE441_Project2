package edu.rit.se441.project2.nonactors;

public enum Consts {
	LOGGER_DEBUG_ERR_OFF					("DEBUG [%s]: %s %n"),
	LOGGER_DEBUG_ERR_ON						("DEBUG [%-47s]: %s %n"),
	LOGGER_ERROR							("ERROR [%s]: %s %n"),
	DEBUG_MSG_RECEIVED						("%s message received from %s", true),
	ERROR_MSG_CHLD_NOT_REG					("My children [%s] are not registered"),
	DEBUG_MSG_CHLD_ALR_REG					("My children [%s] were already registered"),
	DEBUG_MSG_REG_MY_CHILD					("Registering my children [%s]"),
	DEBUG_MSG_TELL_PRT_TO_REG				("Telling my parent to register [%s]"),
	DEBUG_MSG_INSTAT_ACTOR					("Instantiating [%s] from [%s]"),
	DEBUG_MSG_LINE_NO1_IN_QUEUE				("There is no one in the queue to send to BodyCheck"),
	DEBUG_MSG_SEND_OBJ_TO_IN_MESS			("Sending message [%s] with %s [%s] to [%s] from [%s]", true),
	DEBUG_MSG_SEND_TO_MESSAGE				("Sending message [%s] to [%s] from [%s]", true),
	DEBUG_MSG_ASK_IF_I_CAN_SEND_MESSAGE		("Asking [%s] if I ([%s]) can send [%s] in message [%s]"),
	DEBUG_MSG_ADD_PASS_TO_QUEUE 			("Adding Passenger [%s] to my queue so he can wait for the BodyCheck"),
	
	
	NAME_ACTORS_DOCUMENT_CHECK				("Document Check"),
	NAME_ACTORS_LINE						("Line"),
	NAME_ACTORS_BAG_CHECK					("Bag Check"),
	NAME_ACTORS_BODY_CHECK					("Body Check"),
	NAME_ACTORS_SECURITY					("Security"),
	NAME_ACTORS_JAIL						("Jail"),
	NAME_ACTORS_SYSTEM						("System"),
	
	NAME_MESSAGES_BAG_CHECK_REPORT			("BagCheckReport"),
	NAME_MESSAGES_BODY_CHECK_REPORT			("BodyCheckReport"),
	NAME_MESSAGES_BODY_CHECK_REQUESTS_NEXT	("BodyCheckRequestsNext"),
	NAME_MESSAGES_CAN_I_SEND_YOU_A_PASSENG	("CanISendYouAPassenger"),
	NAME_MESSAGES_END_OF_DAY				("EndOfDay"),
	NAME_MESSAGES_EXIT						("Exit"),
	NAME_MESSAGES_GO_TO_BAG_CHECK			("GoToBagCheck"),
	NAME_MESSAGES_GO_TO_BODY_CHECK			("GoToBodyCheck"),
	NAME_MESSAGES_GO_TO_JAIL				("GoToJail"),
	NAME_MESSAGES_GO_TO_LINE				("GoToLine"),
	NAME_MESSAGES_REGISTER					("Register"),
	NAME_MESSAGES_START_SYSTEM				("StartSystem"),
	
	NAME_TRANSFERRED_OBJECTS_BAGGAGE		("Baggage"),
	NAME_TRANSFERRED_OBJECTS_PASSENGER		("Passenger"),
	
	NAME_OTHER_OBJECTS_DRIVER				("Driver"),
	

	
	PREFIX_MSG_REC_SUB_ROUT		("|__");
	
	
	
	
	
	
	private String value;
	private boolean availableToConsole;
	
	Consts(String value, boolean availableToConsole) {
		this.availableToConsole = availableToConsole;
		this.value = value;
	}
	
	Consts(String value) {
		this.availableToConsole = false;
		this.value = value;
	}
	

	public String value() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public boolean availableToConsole() {
		return availableToConsole;
	}
}
