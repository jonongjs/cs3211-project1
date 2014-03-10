import java.util.*;
import java.util.concurrent.*;

public class Database implements Runnable {
	public Database(int id, Simulator simulator) {
		this.id = id;
		sim = simulator;
		records = new HashMap<Integer, Integer>();
	}

	public void run() {
		exit = false;
		messages = new LinkedBlockingQueue<TransactionMessage>();

		try {
			while (!exit) {
				TransactionMessage msg = messages.take();
				switch (msg.type) {
					case DB_CHECKUSER:
						authenticate(msg.senderID, msg.recordID);
						break;
					case DB_CHANGEBALANCE:
						withdraw(msg.senderID, msg.recordID, msg.value);
						break;
					case DB_CHECKBALANCE:
						checkBalance(msg.senderID, msg.recordID);
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

//		System.out.println("Database " + id + " ending.");
	}

	public boolean authenticate(int senderID, int recordID) throws InterruptedException {
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		if (records.containsKey(recordID)) {
			// Send success
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHECKUSER_SUCCESS, recordID));
			return true;
		} else {
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHECKUSER_FAIL, recordID));
			return false;
		}
	}

	public boolean withdraw(int senderID, int recordID, int amount) throws InterruptedException {
		int balance = records.get(recordID);
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		if (amount > balance) {
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHANGEBALANCE_FAIL, recordID));
			return false;
		} else {
			records.put(recordID, balance - amount);
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHANGEBALANCE_SUCCESS, recordID));
			return true;
		}
	}

	public boolean checkBalance(int senderID, int recordID) throws InterruptedException {
		int balance = records.get(recordID);
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHECKBALANCE_SUCCESS, recordID, balance));

		return true;
	}

	public void stop() {
		exit = true;
		messages.add(new TransactionMessage(0, TransactionMessage.Type.EXIT, 0)); //HACK: get out of infinite loop
	}

	synchronized public void pushMessage(TransactionMessage msg) {
		messages.add(msg);
	}

	public void addRecord(int id, int balance) {
		records.put(id, balance);
	}

	public void sendMessageTo(CloudProcessor cpu, TransactionMessage msg) throws InterruptedException {
		TransactionMessage errorMsg = new TransactionMessage(id, TransactionMessage.Type.SEND_SUCCESS, msg.recordID, msg.value);
		do
		{
			// Simulate unreliability
			if (rand.nextInt(10) < 1) {
				// Sending failed, so we tell ourselves it failed
				switch (msg.type) {
					case DB_CHECKUSER_FAIL:
					case DB_CHECKUSER_SUCCESS:
						errorMsg.type = TransactionMessage.Type.SEND_DB_CHECKUSER_FAIL;
						break;
					case DB_CHANGEBALANCE_FAIL:
					case DB_CHANGEBALANCE_SUCCESS:
						errorMsg.type = TransactionMessage.Type.SEND_DB_CHANGEBALANCE_FAIL;
						break;
					case DB_CHECKBALANCE_SUCCESS:
						errorMsg.type = TransactionMessage.Type.SEND_DB_CHECKBALANCE_FAIL;
						break;
				}
//				pushMessage(new TransactionMessage(id, responseType, msg.recordID, msg.value));
			} else {
//				pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_SUCCESS, msg.recordID, msg.value));
				errorMsg.type = TransactionMessage.Type.SEND_SUCCESS;
				cpu.pushMessage(msg);
			}
//			errorMsg = messages.take();
		} while (errorMsg.type != TransactionMessage.Type.SEND_SUCCESS);
	}

	public void printBalances() {
		for (Integer key: records.keySet()) {
			System.out.println("Record " + key + " balance: " + records.get(key));
		}
	}

	int id;
	Simulator sim;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	HashMap<Integer, Integer> records;

	Random rand = new Random();

	public static int TIMEOUT_INTERVAL = 1250; //TIMEOUT in milliseconds
}
