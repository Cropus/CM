package Server;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Server implements Runnable {
	List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
	HashMap<Integer, ArrayList<Integer>> chats = new HashMap<>();
	private static volatile Server server = null;
	private final int port = 9998;
	//private SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
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
			final KeyStore keyStore = KeyStore.getInstance(new File("C:\\Users\\Admin\\IdeaProjects\\CM\\src\\keystore.jks"), password);
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
			keyManagerFactory.init(keyStore, password);
			final SSLContext context = SSLContext.getInstance("SSL");
			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			final SSLServerSocketFactory factory = context.getServerSocketFactory();
			System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\Admin\\IdeaProjects\\CM\\src\\keystore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
			serverSocket = (SSLServerSocket) factory.createServerSocket(port);
			while (true) {
				ClientHandler client;
				try {
					client = new ClientHandler((SSLSocket) serverSocket.accept(), this);
					clients.add(client);
					Thread thread = new Thread(client);
					thread.start();
				} catch (IOException e) {
//TODO log
				}
			}
		} catch (IOException e) {
//TODO log
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
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
