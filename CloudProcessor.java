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
//				System.out.println("CloudProcessor running.");

				TransactionMessage msg = messages.take();
				switch (msg.type) {
					case AUTHEN:
						authenticate(msg.senderID, msg.recordID);
						recordAtmMap.put(msg.recordID, msg.senderID);
						break;
					case SEND_CHECKUSERDB_RESULT_FAIL:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sim.getATM(atmID).pushMessage(new TransactionMessage(id, TransactionMessage.Type.AUTHEN_FAIL, msg.recordID));
						}
						break;
					case SEND_CHECKUSERDB_RESULT_SUCCESS:
						{
							authRecords.put(msg.recordID, true);
							int atmID = recordAtmMap.get(msg.recordID);
							sim.getATM(atmID).pushMessage(new TransactionMessage(id, TransactionMessage.Type.AUTHEN_SUCCESS, msg.recordID));
						}
						break;

					case SEND_CHECKBALANCE:
						checkBalance(msg.senderID, msg.recordID);
						break;
					case SEND_CHECKBALANCE_RESULT_SUCCESS:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sim.getATM(atmID).pushMessage(new TransactionMessage(id, TransactionMessage.Type.GET_CB_RESPOND, msg.recordID, msg.value));
						}
						break;

					case SEND_AMOUNT:
						withdraw(msg.senderID, msg.recordID, msg.value);
						break;
					case SEND_CHANGEBALANCE_RESULT_SUCCESS:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sim.getATM(atmID).pushMessage(new TransactionMessage(id, TransactionMessage.Type.WITHDRAW_SUCCESS, msg.recordID, msg.value));
						}
						break;
					case SEND_CHANGEBALANCE_RESULT_FAIL:
						{
							int atmID = recordAtmMap.get(msg.recordID);
							sim.getATM(atmID).pushMessage(new TransactionMessage(id, TransactionMessage.Type.WITHDRAW_FAIL, msg.recordID, msg.value));
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

		System.out.println("CloudProcessor ending.");
	}

	public boolean authenticate(int senderID, int recordID) {
		Database db = sim.getDatabase();
		db.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKUSERDB, recordID));
		return true;
	}

	public boolean withdraw(int senderID, int recordID, int amount) {
		if (authRecords.containsKey(recordID) && authRecords.get(recordID)) {
			Database db = sim.getDatabase();
			db.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHANGEBALANCE_RESULT, recordID, amount));
			return true;
		}
		return false;
	}

	public boolean checkBalance(int senderID, int recordID) {
		if (authRecords.containsKey(recordID) && authRecords.get(recordID)) {
			Database db = sim.getDatabase();
			db.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKBALANCE_RESULT, recordID));
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

	int id;
	Simulator sim;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	HashMap<Integer, Boolean> authRecords; // Keeps track of which records are authenticated
	HashMap<Integer, Integer> recordAtmMap; // Maps record IDs to ATM IDs

	public static int TIMEOUT_INTERVAL = 1000; //TIMEOUT in milliseconds
}
