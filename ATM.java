import java.util.concurrent.*;

public class ATM implements Runnable {
	public ATM(int id, Simulator simulator) {
		this.id = id;
		sim = simulator;
	}

	public void run() {
		exit = false;
		messages = new LinkedBlockingQueue<TransactionMessage>();

		try {
			while (!exit) {
//				System.out.println("ATM running.");
				if (authenticate()) {
					//TODO: withdraw or check balance
					withdraw(10);
					checkBalance();
				} else {
					//TODO: go back to original state
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Caught InterruptedException: " + e);
		}

		System.out.println("ATM ending.");
	}

	public boolean authenticate() throws InterruptedException {
		System.out.println("ATM authenticating");

		cpu = sim.getCloudProcessor();
		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.AUTHEN, id));

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
		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_AMOUNT, id, amount));

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

		cpu.pushMessage(new TransactionMessage(id, TransactionMessage.Type.SEND_CHECKBALANCE, id));

		TransactionMessage msg = messages.poll(TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
		if (msg == null) {
			System.out.println("ATM checking balance - Timeout");
			return false;
		} else if (msg.type == TransactionMessage.Type.GET_CB_RESPOND) {
			System.out.println("Balance: " + msg.value);
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
	CloudProcessor cpu;
	boolean exit;
	BlockingQueue<TransactionMessage> messages;

	public static int TIMEOUT_INTERVAL = 1000; //TIMEOUT in milliseconds
}
