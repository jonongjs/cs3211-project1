public class TransactionMessage {
	public enum Type {
		// Messages between ATM and CloudProcessor
		AUTHEN,
		AUTHEN_SUCCESS,
		AUTHEN_FAIL,
		SEND_AUTHEN_FAIL,
		SEND_AUTHEN_SUCCESS,

		WITHDRAW_AMOUNT,
		WITHDRAW_SUCCESS,
		WITHDRAW_FAIL,
		SEND_AMOUNT_FAIL,
		SEND_AMOUNT_SUCCESS,

		CHECKBALANCE,
		SEND_CHECKBALANCE_FAIL,
		GET_CB_RESPOND,
		GET_CB_RESPOND_FAIL,

		SEND_AUTHEN_RESPOND_FAIL,
		SEND_WITHDRAW_RESPOND_FAIL,

		// Messages between CloudProcessor and Database
		DB_CHECKUSER,
		DB_CHECKUSER_FAIL,
		DB_CHECKUSER_SUCCESS,
		SEND_DB_CHECKUSER_FAIL,
		SEND_DB_CHECKUSER_SUCCESS,

		DB_CHANGEBALANCE,
		DB_CHANGEBALANCE_FAIL,
		DB_CHANGEBALANCE_SUCCESS,
		SEND_DB_CHANGEBALANCE_FAIL,
		SEND_DB_CHANGEBALANCE_SUCCESS,

		DB_CHECKBALANCE,
		DB_CHECKBALANCE_SUCCESS,
		SEND_DB_CHECKBALANCE_FAIL,
		SEND_DB_CHECKBALANCE_SUCCESS,

		// Misc messages
		SEND_SUCCESS, // Hack to signal sending passed
		EXIT
	}

	public TransactionMessage(int senderID, Type type, int recordID, int value) {
		this.senderID = senderID;
		this.type = type;
		this.recordID = recordID;
		this.value = value;
	}

	public TransactionMessage(int senderID, Type type, int recordID) {
		this.senderID = senderID;
		this.type = type;
		this.recordID = recordID;
		this.value = 0;
	}

	public int senderID;
	public Type type;
	public int recordID;
	public int value; // If withdraw or check balance, this will be amount in cents
}
