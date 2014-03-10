import java.io.*;
import java.util.*;

public class Simulator {
	public static void main(String[] args) throws InterruptedException, IOException {
		Boolean useStdin = true;

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		if (args.length > 0) {
			useStdin = false;
			try {
				input = new BufferedReader(new FileReader(args[0]));
			} catch (Exception e) {
				System.err.println("Error opening file. " + e);
				System.exit(-1);
			}
		}

		Simulator sim = new Simulator();

		String str;
		if (useStdin)
			System.out.print(">> ");
		while ((str=input.readLine()) != null) {
			str = str.trim();
			String[] tokens = str.split("\\s+");

			if (tokens.length > 0 && tokens[0].length() > 0) {
				String cmd = tokens[0];

				if (cmd.equals("exit")) {
					sim.stop();
					break;
				} else if (cmd.equals("addrecord")) {
					try {
						int balance = Integer.parseInt(tokens[1]);
						sim.addRecord(balance);
					} catch (Exception e) {
						System.err.println("Bad input. Expected integer");
						printHelp();
					}
				} else if (cmd.equals("checkbalance")) {
					try {
						int atmID = Integer.parseInt(tokens[1]);
						int recordID = Integer.parseInt(tokens[2]);
						sim.addAction(atmID, new ATM.Action(ATM.Action.Type.CHECKBALANCE, recordID, 0));
					} catch (Exception e) {
						System.err.println("Bad input. Expected integers");
						printHelp();
					}
				} else if (cmd.equals("withdraw")) {
					try {
						int atmID = Integer.parseInt(tokens[1]);
						int recordID = Integer.parseInt(tokens[2]);
						int amount = Integer.parseInt(tokens[3]);
						sim.addAction(atmID, new ATM.Action(ATM.Action.Type.WITHDRAW, recordID, amount));
					} catch (Exception e) {
						System.err.println("Bad input. Expected integers");
						printHelp();
					}
				} else if (cmd.equals("numatm")) {
					try {
						int numAtms = Integer.parseInt(tokens[1]);
						sim.setNumAtms(numAtms);
					} catch (Exception e) {
						System.err.println("Bad input. Expected integer");
						printHelp();
					}
				} else if (cmd.equals("type")) {
					try {
						String simType = tokens[1];
						if (simType.equals("proper") || simType.equals("deadlock") || simType.equals("incorrect")) {
							sim.setSimType(simType);
						} else {
							throw new Exception("Bad input");
						}
					} catch (Exception e) {
						System.err.println("Bad input. Expected argument 'proper', 'deadlock', or 'incorrect'");
						printHelp();
					}
				} else if (cmd.equals("run")) {
					sim.startTransactions();
				} else if (cmd.equals("stop")) {
					sim.stop();
				} else if (cmd.equals("help")) {
					printHelp();
				} else {
					System.out.println("Unrecognized command.");
				}
			}
 
			if (useStdin)
				System.out.print(">> ");
		}
	}

	public static void printHelp() {
		System.out.println(
			"Commands:\n"
			+ "addrecord <balance>\t\t\tAdds a record and set its current balance to `balance` cents.\n"
			+ "checkbalance <atmID> <recordID>\t\tAdd a checkbalance action to the ATM `atmID` on the record `recordID`.\n"
			+ "withdraw <atmID> <recordID> <amount>\tAdd a withdraw action to the ATM `atmID` on record `recordID` and withdraw `amount` cents.\n"
			+ "numatm <num_of_atm>\t\t\tCreate `num_of_atm` ATMs and CloudProcessors.\n"
			+ "type [proper|deadlock|incorrect]\tChoose the type of simulator to run: proper; with deadlock; or with incorrect calculations.\n"
			+ "run\t\t\t\t\tRun the system.\n"
			+ "stop\t\t\t\t\tStop the system.\n"
			+ "exit\t\t\t\t\tQuit the simulator.\n"
			+ "help\t\t\t\t\tDisplay this message.\n"
		);
	}

	public Simulator() {
		reset();
	}

	public void startTransactions() {
		System.out.println("Simulation type: " + simType);
		atmsCompleted = 0;

		//NOTE: assume we only have one database for now
		// Start all ATMs, CPUs, Database
		for (int i=0; i<numAtms; ++i) {
			if (simType.equals("deadlock")) {
				atms.add(new ATMDeadlock(i, this));
			} else {
				atms.add(new ATM(i, this));
			}

			cpus.add(new CloudProcessor(i, this));
		}
		if (simType.equals("deadlock")) {
			databases.add(new DatabaseDeadlock(0, this));
		} else if (simType.equals("incorrect")) {
			databases.add(new DatabaseIncorrect(0, this));
		} else {
			databases.add(new Database(0, this));
		}

		for (int i=0; i<records.size(); ++i) {
			databases.get(0).addRecord(i, records.get(i));
		}

		for (Map.Entry<Integer, List<ATM.Action>> entry : atmActionMap.entrySet()) {
			if (entry.getKey() < numAtms) {
				atms.get(entry.getKey()).setActions(entry.getValue());
			}
		}

		List<Thread> threads = new ArrayList<Thread>();
		for (ATM atm : atms)
			threads.add(new Thread(atm));
		for (CloudProcessor cpu : cpus)
			threads.add(new Thread(cpu));
		for (Database database : databases)
			threads.add(new Thread(database));

		System.out.println("System starting. Initial balances:");
		databases.get(0).printBalances();
		System.out.println();

		for (Thread t : threads) {
			t.start();
		}
	}

	public void stop() {
		for (ATM atm : atms)
			atm.stop();
		for (CloudProcessor cpu : cpus)
			cpu.stop();
		for (Database database : databases)
			database.stop();

		reset();
	}

	public CloudProcessor getCloudProcessor() {
		// Use a round-robin allocation scheme
		cpuChoice += 1;
		if (cpuChoice >= cpus.size())
			cpuChoice = 0;
		return cpus.get(cpuChoice);
	}

	public CloudProcessor getCloudProcessorByID(int id) {
		return cpus.get(id);
	}

	public Database getDatabase() {
		// Only one database for now
		return databases.get(0);
	}

	public ATM getATM(int id) {
		return atms.get(id);
	}

	public void reset() {
		numAtms = 1;
		simType = "proper";
		atmActionMap = new HashMap<Integer, List<ATM.Action>>();
		records = new LinkedList<Integer>();

		atms = new ArrayList<ATM>();
		cpus = new ArrayList<CloudProcessor>();
		databases = new ArrayList<Database>();
	}

	public void setNumAtms(int numAtms) {
		this.numAtms = numAtms;
	}

	public void setSimType(String simType) {
		this.simType = simType;
	}

	public void addRecord(int balance) {
		records.add(balance);
	}

	public void addAction(int atmID, ATM.Action action) {
		if (!atmActionMap.containsKey(atmID)) {
			atmActionMap.put(atmID, new LinkedList<ATM.Action>());
		}
		atmActionMap.get(atmID).add(action);
	}

	synchronized public void notifyThreadCompleted(int atmID) {
		atmsCompleted += 1;
		if (atmsCompleted >= numAtms) {
			System.out.println();
			System.out.println("System stopping. Final balances:");
			databases.get(0).printBalances();
			stop();
			System.exit(0);
		}
	}

	List<ATM> atms = new ArrayList<ATM>();
	List<CloudProcessor> cpus = new ArrayList<CloudProcessor>();
	List<Database> databases = new ArrayList<Database>();

	int numAtms;
	String simType;
	HashMap<Integer, List<ATM.Action>> atmActionMap;
	List<Integer> records;

	int cpuChoice = 0;
	int atmsCompleted;
}
