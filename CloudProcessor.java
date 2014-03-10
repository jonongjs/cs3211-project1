import java.util.*;
import java.util.concurrent.*;

public class CloudProcessor implements Runnable {
	public CloudProcessor(int id, Simulator simulator) {
		this.id = id;
		sim = simulator;
	}

	public void run() {
		exit = false;
		messages = new LinkedBlockingQueue<TransactionMessage>();
		authRecords = new HashMap<Integer, Boolean>();
		recordAtmMap = new HashMap<Integer, Integer>();

		try {
			while (!exit) {
				TransactionMessage msg = messages.take();
				switch (msg.type) {
					case AUTHEN:
						authenticate(msg.senderID, msg.recordID);
						break;
					case DB_CHECKUSER_FAIL:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sendMessageToATM(sim.getATM(atmID), new TransactionMessage(id, TransactionMessage.Type.AUTHEN_FAIL, msg.recordID));
						}
						break;
					case DB_CHECKUSER_SUCCESS:
						{
							authRecords.put(msg.recordID, true);
							int atmID = recordAtmMap.get(msg.recordID);
							sendMessageToATM(sim.getATM(atmID), new TransactionMessage(id, TransactionMessage.Type.AUTHEN_SUCCESS, msg.recordID));
						}
						break;

					case CHECKBALANCE:
						checkBalance(msg.senderID, msg.recordID);
						break;
					case DB_CHECKBALANCE_SUCCESS:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sendMessageToATM(sim.getATM(atmID), new TransactionMessage(id, TransactionMessage.Type.GET_CB_RESPOND, msg.recordID, msg.value));
							authRecords.remove(msg.recordID);
						}
						break;

					case WITHDRAW_AMOUNT:
						withdraw(msg.senderID, msg.recordID, msg.value);
						break;
					case DB_CHANGEBALANCE_SUCCESS:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sendMessageToATM(sim.getATM(atmID), new TransactionMessage(id, TransactionMessage.Type.WITHDRAW_SUCCESS, msg.recordID, msg.value));
							authRecords.remove(msg.recordID);
						}
						break;
					case DB_CHANGEBALANCE_FAIL:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sendMessageToATM(sim.getATM(atmID), new TransactionMessage(id, TransactionMessage.Type.WITHDRAW_FAIL, msg.recordID, msg.value));
							authRecords.remove(msg.recordID);
						}
						break;

					case EXIT:
						exit = true;
						break;

					default:
						break;
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Caught InterruptedException: " + e);
		}

//		System.out.println("CloudProcessor " + id + " ending.");
	}

	public boolean authenticate(int senderID, int recordID) {
		Database db = sim.getDatabase();
		db.pushMessage(new TransactionMessage(id, TransactionMessage.Type.DB_CHECKUSER, recordID));
		recordAtmMap.put(recordID, senderID);
		return true;
	}

	public boolean withdraw(int senderID, int recordID, int amount) {
		if (authRecords.containsKey(recordID) && authRecords.get(recordID)) {
			Database db = sim.getDatabase();
			db.pushMessage(new TransactionMessage(id, TransactionMessage.Type.DB_CHANGEBALANCE, recordID, amount));
			return true;
		}
		return false;
	}

	public boolean checkBalance(int senderID, int recordID) {
		if (authRecords.containsKey(recordID) && authRecords.get(recordID)) {
			Database db = sim.getDatabase();
			db.pushMessage(new TransactionMessage(id, TransactionMessage.Type.DB_CHECKBALANCE, recordID));
			return true;
		}
		return false;
	}

	public void stop() {
		exit = true;
		messages.add(new TransactionMessage(0, TransactionMessage.Type.EXIT, 0)); //HACK: get out of infinite loop
	}

	synchronized public void pushMessage(TransactionMessage msg) {
		messages.add(msg);
	}

	private void sendMessageToDB(Database obj, TransactionMessage msg) throws InterruptedException {
		TransactionMessage errorMsg = new TransactionMessage(id, TransactionMessage.Type.SEND_SUCCESS, msg.recordID, msg.value);
		do {
			// Simulate unreliability
			if (rand.nextInt(10) < 1) {
				// Sending failed, so we tell ourselves it failed
//				TransactionMessage.Type responseType = TransactionMessage.Type.DB_CHECKUSER_FAIL;
				switch (msg.type) {
					case DB_CHECKUSER:
						errorMsg.type = TransactionMessage.Type.SEND_DB_CHECKUSER_FAIL;
						break;
					case DB_CHANGEBALANCE:
						errorMsg.type = TransactionMessage.Type.SEND_DB_CHANGEBALANCE_FAIL;
						break;
					case DB_CHECKBALANCE:
						errorMsg.type = TransactionMessage.Type.SEND_DB_CHECKBALANCE_FAIL;
						break;
				}
//				pushMessage(new TransactionMessage(id, responseType, msg.recordID, msg.value));
			} else {
//				pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_SUCCESS, msg.recordID, msg.value));
				errorMsg.type = TransactionMessage.Type.SEND_SUCCESS;
				obj.pushMessage(msg);
			}

//			errorMsg = messages.take();
		} while (errorMsg.type != TransactionMessage.Type.SEND_SUCCESS);
	}

	private void sendMessageToATM(ATM obj, TransactionMessage msg) throws InterruptedException {
		TransactionMessage errorMsg = new TransactionMessage(id, TransactionMessage.Type.SEND_SUCCESS, msg.recordID, msg.value);
		do {
			// Simulate unreliability
			if (rand.nextInt(10) < 1) {
				// Sending failed, so we tell ourselves it failed
//				TransactionMessage.Type responseType = TransactionMessage.Type.SEND_AUTHEN_RESPOND_FAIL;
				switch (msg.type) {
					case AUTHEN_SUCCESS:
					case AUTHEN_FAIL:
						errorMsg.type = TransactionMessage.Type.SEND_AUTHEN_RESPOND_FAIL;
						break;
					case WITHDRAW_SUCCESS:
					case WITHDRAW_FAIL:
						errorMsg.type = TransactionMessage.Type.SEND_WITHDRAW_RESPOND_FAIL;
						break;
					case GET_CB_RESPOND:
						errorMsg.type = TransactionMessage.Type.GET_CB_RESPOND_FAIL;
						break;
				}
//				pushMessage(new TransactionMessage(id, responseType, msg.recordID, msg.value));
			} else {
//				pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_SUCCESS, msg.recordID, msg.value));
				errorMsg.type = TransactionMessage.Type.SEND_SUCCESS;
				obj.pushMessage(msg);
			}

//			errorMsg = messages.take();
		} while (errorMsg.type != TransactionMessage.Type.SEND_SUCCESS);
	}

	int id;
	Simulator sim;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	HashMap<Integer, Boolean> authRecords; // Keeps track of which records are authenticated
	HashMap<Integer, Integer> recordAtmMap; // Maps record IDs to ATM IDs

	Random rand = new Random();

	public static int TIMEOUT_INTERVAL = 2500; //TIMEOUT in milliseconds
}
