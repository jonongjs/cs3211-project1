import java.util.*;
import java.util.concurrent.*;

public class ATM implements Runnable {
	public static class Action {
		public enum Type { WITHDRAW, CHECKBALANCE }

		Type type;
		int recordID;
		int amount;

		Action(Type type, int recordID, int amount) {
			this.type = type;
			this.recordID = recordID;
			this.amount = amount;
		}
	}

	public ATM(int id, Simulator simulator) {
		this.id = id;
		sim = simulator;
		actions = new LinkedList<Action>();
	}

	public void run() {
		exit = false;
		messages = new LinkedBlockingQueue<TransactionMessage>();

		try {
			Iterator<Action> iter = actions.iterator();
			while (!exit && iter.hasNext()) {
				Action action = iter.next();
				curRecordID = action.recordID;

				cpu = sim.getCloudProcessor();
				if (authenticate()) {
					switch (action.type) {
						case WITHDRAW:
							withdraw(action.amount);
							break;

						case CHECKBALANCE:
							checkBalance();
							break;
					}
				} else {
					//TODO: go back to original state
				}
			}

			sim.notifyThreadCompleted(id);
			while (!exit) {
				// Wait for the stop or exit command
			}
		} catch (InterruptedException e) {
			System.out.println("Caught InterruptedException: " + e);
		}

//		System.out.println("ATM " + id + " ending.");
	}

	public boolean authenticate() throws InterruptedException {
		System.out.println("ATM " + id + " authenticating for Record " + curRecordID);

		TransactionMessage msg;
		do
		{
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.AUTHEN, curRecordID));

			msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
			if (msg == null) {
				System.out.println("ATM " + id + " authenticate timeout");
				return false;
			} else if (msg.type == TransactionMessage.Type.AUTHEN_FAIL) {
				System.out.println("ATM " + id + " authenticate failed");
				return false;
			} else if (msg.type == TransactionMessage.Type.AUTHEN_SUCCESS) {
				return true;
			}
		} while (msg.type == TransactionMessage.Type.SEND_AUTHEN_FAIL);
		return false;
	}

	public boolean withdraw(int amount) throws InterruptedException {
		System.out.println("ATM " + id + " attempting to withdraw " + amount + "cents from Record " + curRecordID);
		TransactionMessage msg;
		do
		{
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.WITHDRAW_AMOUNT, curRecordID, amount));

			msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
			if (msg == null) {
				System.out.println("ATM " + id + " withdraw - Timeout");
				return false;
			} else if (msg.type == TransactionMessage.Type.WITHDRAW_FAIL) {
				System.out.println("ATM " + id + " withdraw - Failed");
				return false;
			} else if (msg.type == TransactionMessage.Type.WITHDRAW_SUCCESS) {
				System.out.println("ATM " + id + " withdraw - Success");
				return true;
			}
		} while (msg.type == TransactionMessage.Type.SEND_AMOUNT_FAIL);
		return false;
	}

	public boolean checkBalance() throws InterruptedException {
		System.out.println("ATM " + id + " checking balance for Record " + curRecordID);

		TransactionMessage msg;
		do
		{
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.CHECKBALANCE, curRecordID));

			msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
			if (msg == null) {
				System.out.println("ATM " + id + " checking balance - Timeout");
				return false;
			} else if (msg.type == TransactionMessage.Type.GET_CB_RESPOND) {
				System.out.println("ATM " + id + ": Record " + curRecordID + " Balance: " + msg.value);
				return true;
			}
		} while (msg.type == TransactionMessage.Type.SEND_CHECKBALANCE_FAIL);
		return false;
	}

	public void stop() {
		exit = true;
		messages.add(new TransactionMessage(0, TransactionMessage.Type.EXIT, 0)); //HACK: get out of infinite loop
	}

	public void setActions(List<Action> newActions) {
		if (newActions != null)
			actions = newActions;
	}

	synchronized public void pushMessage(TransactionMessage msg) {
		messages.add(msg);
	}

	private void sendMessageTo(CloudProcessor cpu, TransactionMessage msg) {
		// Simulate unreliability
		if (rand.nextInt(10) < 1) {
			// Sending failed, so we tell ourselves it failed
			TransactionMessage.Type responseType = TransactionMessage.Type.SEND_AUTHEN_FAIL;
			switch (msg.type) {
				case AUTHEN:
					responseType = TransactionMessage.Type.SEND_AUTHEN_FAIL;
					break;
				case WITHDRAW_AMOUNT:
					responseType = TransactionMessage.Type.SEND_AMOUNT_FAIL;
					break;
				case CHECKBALANCE:
					responseType = TransactionMessage.Type.SEND_CHECKBALANCE_FAIL;
					break;
			}
			pushMessage(new TransactionMessage(id, responseType, curRecordID));
		} else {
			cpu.pushMessage(msg);
		}
	}

	int id;
	Simulator sim;
	CloudProcessor cpu;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	List<Action> actions;
	int curRecordID;

	Random rand = new Random();

	public static int TIMEOUT_INTERVAL = 5000; //TIMEOUT in milliseconds
}
