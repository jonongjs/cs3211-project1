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

			//TODO: parse inputs
			if (str.equals("exit"))
				break;
			if (str.equals("run"))
				sim.startTransactions();

			System.out.print(">> ");
		}
	}

	public void startTransactions() {
		//TODO: start all ATMs, CPUs, Database
		List<Thread> threads = new ArrayList<Thread>();

		threads.add(new Thread(new ATM()));
		threads.add(new Thread(new CloudProcessor()));
		threads.add(new Thread(new Database()));

		for (Thread t : threads) {
			t.start();
		}
	}
}
