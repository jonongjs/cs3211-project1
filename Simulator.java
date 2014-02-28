import java.io.*;
import java.util.*;

public class Simulator {
	public static void main(String[] args) throws InterruptedException, IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Simulator sim = new Simulator();

		String str;
		System.out.print(">> ");
		while ((str=input.readLine()) != null) {
			str = str.trim();
			String[] tokens = str.split("\\s+");

			if (tokens.length > 0 && tokens[0].length() > 0) {
				String cmd = tokens[0];

				//TODO: parse inputs
				if (cmd.equals("exit")) {
					break;
				} else if (cmd.equals("run")) {
					sim.startTransactions();
				} else if (cmd.equals("stop")) {
					sim.stop();
				} else {
					System.out.println("Unrecognized command.");
				}
			}

			System.out.print(">> ");
		}
	}

	public void startTransactions() {
		//TODO: start all ATMs, CPUs, Database
		atms.add(new ATM(0, this));
		cpus.add(new CloudProcessor(0, this));
		databases.add(new Database(0, this));

		//TODO: use the command line to add records
		databases.get(0).addRecord(0, 100);
		databases.get(0).addRecord(1, 200);

		List<Thread> threads = new ArrayList<Thread>();
		for (ATM atm : atms)
			threads.add(new Thread(atm));
		for (CloudProcessor cpu : cpus)
			threads.add(new Thread(cpu));
		for (Database database : databases)
			threads.add(new Thread(database));

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

	List<ATM> atms = new ArrayList<ATM>();
	List<CloudProcessor> cpus = new ArrayList<CloudProcessor>();
	List<Database> databases = new ArrayList<Database>();

	int cpuChoice = 0;
}
