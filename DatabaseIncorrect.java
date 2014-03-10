import java.util.concurrent.*;

public class DatabaseIncorrect extends Database {
	public DatabaseIncorrect(int id, Simulator simulator) {
		super(id, simulator);
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
					case DB_UPDATE_AMOUNT:
						updateAmount(msg.recordID, msg.value);
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

	public boolean withdraw(int senderID, int recordID, int amount) throws InterruptedException {
		int balance = records.get(recordID);
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		if (amount > balance) {
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHANGEBALANCE_FAIL, recordID));
			return false;
		} else {
			//records.put(recordID, balance - amount);
			pushMessage(new TransactionMessage(id, TransactionMessage.Type.DB_UPDATE_AMOUNT, recordID, balance-amount));
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHANGEBALANCE_SUCCESS, recordID));
			return true;
		}
	}

	private void updateAmount(int recordID, int amount) {
		records.put(recordID, amount);
	}
}
