public class TransactionMessage {
	public enum Type {
		// Messages between ATM and CloudProcessor
		AUTHEN,
		AUTHEN_SUCCESS,
		AUTHEN_FAIL,

		SEND_AMOUNT,
		SEND_AMOUNT_SUCCESS,
		WITHDRAW_SUCCESS,
		WITHDRAW_FAIL,

		SEND_CHECKBALANCE,
		SEND_CB_SUCCESS,
		GET_CB_RESPOND,

		SEND_CHECKUSERDB,
		SEND_CHECKUSERDB_SUCCESS,
		GET_USERDB_RESPOND,

		// Messages between CloudProcessor and Database
		SEND_CHECKUSERDB_RESULT,
		SEND_CHECKUSERDB_RESULT_SUCCESS,
		SEND_CHECKUSERDB_RESULT_FAIL,
		SEND_CHECKUSERDB_RESPOND,
		SEND_CHANGEBALANCE_RESULT,
		SEND_CHANGEBALANCE_RESULT_SUCCESS,
		SEND_CHANGEBALANCE_RESULT_FAIL,
		SEND_CHECKBALANCE_RESULT,
		SEND_CHECKBALANCE_RESULT_SUCCESS,
		SEND_CHECKBALANCE_RESULT_FAIL,

		// Misc messages
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
