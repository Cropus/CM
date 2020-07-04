package Server;

import Units.UID;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		Server server = Server.newServer();
		UID uid = new UID();
		Thread mainThread = new Thread(server);
		mainThread.start();
		if (scan.nextLine().equals("exit")) {
			UID.save();
		}
	}
}
