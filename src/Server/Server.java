package Server;

import java.io.IOException;
import java.net.ServerSocket;

public class Server implements Runnable {
	private static volatile Server server = null;
	private final int port = 9999;
	private ServerSocket serverSocket = null;
	private Server(){}
	public static Server newServer() {
		if (server == null) {
			synchronized (Server.class) {
				if (server == null) {
					server = new Server();
				}
			}
		}
		return server;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				ClientThreadContext client;
				try {
					client = new ClientThreadContext(serverSocket.accept());
					Thread thread = new Thread(client);
					thread.start();
				} catch (IOException e) {
//TODO log
				}
			}
		} catch (IOException e) {
//TODO log
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
//TODO log
				}
			}
		}
	}
}
