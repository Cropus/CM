package Client;

import Units.Credential;
import Units.Message;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.Scanner;

public class Client {
	Scanner sc = new Scanner(System.in);
	String str = "";
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private SSLSocket sslSocket;

	public Client() {
		Scanner scan = new Scanner(System.in);

		try {
			final char[] password = "password".toCharArray();
			final KeyStore keyStore = KeyStore.getInstance(new File("C:\\Users\\Admin\\IdeaProjects\\CM\\src\\keystore.jks"), password);
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
			keyManagerFactory.init(keyStore, password);
			final SSLContext context = SSLContext.getInstance("SSL");
			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			final SSLSocketFactory factory = context.getSocketFactory();
			System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\Admin\\IdeaProjects\\CM\\src\\keystore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
			sslSocket = (SSLSocket) factory.createSocket("127.0.0.1", 9998);
			out = new ObjectOutputStream(sslSocket.getOutputStream());
			in = new ObjectInputStream(sslSocket.getInputStream());


			sign();
			Resender resend = new Resender();
			resend.start();


			while (!str.equals("exit")) {
				str = scan.nextLine();
				Message message = new Message(str, 0);
				out.writeObject(message);

			}
			resend.setStop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private void close() {
		try {
			in.close();
			out.close();
			sslSocket.close();
		} catch (Exception e) {
//TODO log
		}
	}

	private class Resender extends Thread {

		private boolean stoped;
		public void setStop() {
			stoped = true;
		}

		@Override
		public void run() {
			try {
				while (!stoped) {
					String str = (String) in.readObject();
					System.out.println(str);
				}
			} catch (IOException e) {
//TODO log
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	void sign() {
		System.out.println("Welcome!");
		System.out.print("1) Sign In\n2) Sign Up\nEnter number: ");
		int ans = sc.nextInt();
		System.out.print("Enter login: ");
		String newLogin = sc.next();
		System.out.print("Enter password: ");
		String newPassword = sc.next();

		Credential credential = null;
		try {
			if (ans == 1) {
				credential = new Credential(newLogin, newPassword, true);
			} else if (ans == 2) {
				credential = new Credential(newLogin, newPassword, false);
			} else {
				throw new IllegalArgumentException();
			}
			out.writeObject(credential);
		} catch (IllegalArgumentException e) {
//TODO log
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
