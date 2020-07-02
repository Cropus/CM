package Server;

public class Main {
	public static void main(String[] args) {
		Server server = Server.newServer();
		Thread mainThread = new Thread(server);
		mainThread.start();
	}
}
