public class ATMDeadlock extends ATM {
	public ATMDeadlock(int id, Simulator simulator) {
		super(id, simulator);
	}

	public boolean authenticate() throws InterruptedException {
		System.out.println("ATM " + id + " authenticating for Record " + curRecordID);

		TransactionMessage msg;
		do
		{
			sendMessageTo(cpu, new TransactionMessage(id, TransactionMessage.Type.AUTHEN, curRecordID));

			msg = messages.take();
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
}
