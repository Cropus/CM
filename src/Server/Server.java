package Server;

import Units.UID;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server implements Runnable {
	List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
	private static volatile Server server = null;
	private final int port = 9998;
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
				ClientHandler client;
				try {
					client = new ClientHandler(serverSocket.accept(), this);
					clients.add(client);
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
