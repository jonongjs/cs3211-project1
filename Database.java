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
					case SEND_CHECKUSERDB:
						authenticate(msg.senderID, msg.recordID);
						break;
					case SEND_CHANGEBALANCE_RESULT:
						withdraw(msg.senderID, msg.recordID, msg.value);
						break;
					case SEND_CHECKBALANCE_RESULT:
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

		System.out.println("Database ending.");
	}

	public boolean authenticate(int senderID, int recordID) {
		//TODO: simulate unreliable protocol
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		if (records.containsKey(recordID)) {
			// Send success
			cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKUSERDB_RESULT_SUCCESS, recordID));
			return true;
		} else {
			cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKUSERDB_RESULT_FAIL, recordID));
			return false;
		}
	}

	public boolean withdraw(int senderID, int recordID, int amount) {
		//TODO: simulate unreliable protocol
		int balance = records.get(recordID);
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		if (amount > balance) {
			cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKUSERDB_RESULT_FAIL, recordID));
			return false;
		} else {
			records.put(recordID, balance - amount);
			cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKUSERDB_RESULT_SUCCESS, recordID));
			return true;
		}
	}

	public boolean checkBalance(int senderID, int recordID) {
		//TODO: simulate unreliable protocol
		int balance = records.get(recordID);
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKBALANCE_RESULT_SUCCESS, recordID, balance));

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

	int id;
	Simulator sim;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	HashMap<Integer, Integer> records;

	public static int TIMEOUT_INTERVAL = 1000; //TIMEOUT in milliseconds
}
