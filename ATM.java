public class ATM implements Runnable {
	//TODO:

	public void run() {
		System.out.println("ATM running.");
		if (authenticate()) {
			//TODO: withdraw or check balance
		} else {
			//TODO: go back to original state
		}
	}

	public boolean authenticate() {
		//TODO:
		return false;
	}

	public boolean withdraw(int amount) {
		//TODO:
		return false;
	}

	public boolean checkBalance(int amount) {
		//TODO: sendCheckBalance
		// result: success or fail
		return false;
	}
}
