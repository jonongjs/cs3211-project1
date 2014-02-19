class Transfer implements Runnable {
	// Accounts
	public static int[] acc = { 1000, 1000 };

	public static final int TRANSFER_AMT = 100; 

	int fromAccount, toAccount;

	public Transfer(int fromAccount, int toAccount) {
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
	}

	public void run() {
		while (true) {
			if (acc[fromAccount] >= TRANSFER_AMT) {
				int newBalance = acc[fromAccount] - TRANSFER_AMT;
				acc[toAccount] += TRANSFER_AMT;
				acc[fromAccount] = newBalance;
			} else {
				Thread.yield();
			}

			System.out.println("acc0: " + acc[0]);
			System.out.println("acc1: " + acc[1]);
		}
	}
}

class BankTransfer {
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(new Transfer(0, 1));
		Thread t2 = new Thread(new Transfer(1, 0));

		t1.start();
		t2.start();
	}
}
