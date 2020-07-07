package Server;

import Units.Message;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Server implements Runnable {
	private final String driver = "com.mysql.cj.jdbc.Driver";
	private final String url = "jdbc:mysql://localhost:9999/cm?serverTimezone=UTC";
	private final String login = "root";
	private final String sqlPassword = "root";
	Connection connection;
	List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
	HashMap<Integer, ArrayList<Message>> chats = new HashMap<>();
	private static volatile Server server = null;
	private final int port = 9998;
	private SSLServerSocket serverSocket = null;
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
			final char[] password = "password".toCharArray();
			final KeyStore keyStore = KeyStore.getInstance(new File(".\\src\\keystore.jks"), password);
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
			keyManagerFactory.init(keyStore, password);
			final SSLContext context = SSLContext.getInstance("SSL");
			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			final SSLServerSocketFactory factory = context.getServerSocketFactory();
			System.setProperty("javax.net.ssl.trustStore", ".\\src\\keystore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
			serverSocket = (SSLServerSocket) factory.createServerSocket(port);
			Class.forName(driver);
			connection = DriverManager.getConnection(url, login, sqlPassword);
			ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM messages");
			while (rs.next()) {
				if (!chats.containsKey(rs.getInt(4))) {
					chats.put(rs.getInt(4), new ArrayList<>());
				}
				Message curMessage = new Message(rs.getInt(1), rs.getString(2),
						rs.getInt(3), rs.getInt(4), rs.getString(5));
				chats.get(rs.getInt(4)).add(curMessage);
			}

			while (true) {
				ClientHandler client;
				try {
					client = new ClientHandler((SSLSocket) serverSocket.accept(), this);
					clients.add(client);
					Thread thread = new Thread(client);
					thread.start();
				} catch (IOException e) {
					System.out.println("Connection error");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException | NoSuchAlgorithmException |
				UnrecoverableKeyException | KeyStoreException |
				KeyManagementException | ClassNotFoundException |
				SQLException e) {
			System.out.println("Problems with Certification");
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("Problems with server.close()");
					e.printStackTrace();
				}
			}
		}
	}
}
