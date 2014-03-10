public class DatabaseDeadlock extends Database {
	public DatabaseDeadlock(int id, Simulator simulator) {
		super(id, simulator);
	}

	public boolean authenticate(int senderID, int recordID) throws InterruptedException {
		CloudProcessor cpu = sim.getCloudProcessorByID(senderID);
		if (records.containsKey(recordID)) {
			// Send success
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHECKUSER_SUCCESS, recordID));
			return true;
		} else {
			//sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.DB_CHECKUSER_FAIL, recordID));
			return false;
		}
	}
}
