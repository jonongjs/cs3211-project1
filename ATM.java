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

			while (!exit) {
				// Wait for the stop or exit command
			}
		} catch (InterruptedException e) {
			System.out.println("Caught InterruptedException: " + e);
		}

		System.out.println("ATM ending.");
	}

	public boolean authenticate() throws InterruptedException {
		System.out.println("ATM authenticating");

		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.AUTHEN, curRecordID));

		TransactionMessage msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
		if (msg == null) {
			System.out.println("ATM authenticate timeout");
			return false;
		} else if (msg.type != TransactionMessage.Type.AUTHEN_SUCCESS) {
			System.out.println("ATM authenticate failed");
			return false;
		}
		return true;
	}

	public boolean withdraw(int amount) throws InterruptedException {
		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_AMOUNT, curRecordID, amount));

		TransactionMessage msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
		if (msg == null) {
			System.out.println("ATM withdraw - Timeout");
			return false;
		} else if (msg.type == TransactionMessage.Type.WITHDRAW_FAIL) {
			System.out.println("ATM withdraw - Failed");
			return false;
		} else if (msg.type == TransactionMessage.Type.WITHDRAW_SUCCESS) {
			System.out.println("ATM withdraw - Success");
			return true;
		}
		return false;
	}

	public boolean checkBalance() throws InterruptedException {
		System.out.println("ATM checking balance");

		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKBALANCE, curRecordID));

		TransactionMessage msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
		if (msg == null) {
			System.out.println("ATM checking balance - Timeout");
			return false;
		} else if (msg.type == TransactionMessage.Type.GET_CB_RESPOND) {
			System.out.println("Record " + curRecordID + " Balance: " + msg.value);
			return true;
		}
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

	int id;
	Simulator sim;
	CloudProcessor cpu;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	List<Action> actions;
	int curRecordID;

	public static int TIMEOUT_INTERVAL = 1000; //TIMEOUT in milliseconds
}
