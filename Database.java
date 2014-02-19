public class Database implements Runnable {
	//TODO:

	public void run() {
		System.out.println("Database running.");
	}

	public boolean authenticate(int id) {
		//TODO:
		// sendCheckUserDBResult
		// -> (sendCheckUserDBResultSuccess
		//    -> getUserDBRespond)
		// []
		// -> sendCheckUserDBResultFail
		return false;
	}

	public boolean withdraw(int amount) {
		//TODO:
		// sendChangeBalanceResult
		// -> (sendChangeBalanceResultSuccess
		//    -> getChangeBalanceRespond)
		// []
		// -> sendChangeBalanceResultFail -> witdraw()?
		return false;
	}

	public boolean checkBalance() {
		//TODO:
		// sendCheckBalanceResult
		// -> (sendCheckBalanceResultSuccess
		//    -> getCheckBalanceRespond)
		// []
		// -> sendCheckBalanceRespondFail -> checkBalance()?
		return false;
	}
}
