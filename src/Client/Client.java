package Client;

import Exceptions.ChatNotFoundException;
import Exceptions.InfoException;
import Units.Credential;
import Units.Message;
import Units.Requests.ChangeRequest;
import Units.Requests.CreateRequest;
import Units.Requests.EnterRequest;
import Units.User;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
	Scanner sc = new Scanner(System.in);
	String str = "";
	private String name;
	private int currentChatID = 0;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private SSLSocket sslSocket;
	private void initializeSSL() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
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
	}

	public Client() {
		Scanner scan = new Scanner(System.in);

		try {
			initializeSSL();

			Resender resend = new Resender();
			resend.start();
			sign();

			while (!str.equals("exit")) {
				str = scan.nextLine();
				switch (str) {
					case "change chat":
						System.out.print("Enter chat ID: ");
						int chatID = scan.nextInt();
						out.writeObject(new ChangeRequest(chatID));
						break;
					case "create chat":
						System.out.print("Enter chat name: ");
						out.writeObject(new CreateRequest(scan.next()));
						break;
					case "enter chat":
						System.out.print("Enter chat name: ");
						out.writeObject(new EnterRequest(scan.next()));
						break;
					default:
						Message message = new Message(str, currentChatID);
						out.writeObject(message);
						break;
				}
			}
			resend.setStop();
		} catch (Exception e) {
			System.out.println("Something went wrong...");
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
			System.out.println("Problems with closing");
			//e.printStackTrace();
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
					Object object = in.readObject();
					if (object instanceof String) {
						String str = (String) object;
						System.out.println(str);
					} else if (object instanceof User) {
						System.out.println("You are logged in!");
						name = ((User) object).getLogin();
					} else if (object instanceof ArrayList) {
						ArrayList<Message> messages = (ArrayList<Message>) object;
						for (Message message : messages) {
							if (message.getName().equals(name)) {
								System.out.println("You: " + message.getText());
							} else {
								System.out.println(message.getName() + ": " + message.getText());
							}
						}
					} else if (object instanceof ChatNotFoundException) {
						ChatNotFoundException e = (ChatNotFoundException) object;
						if (e.isFlag()) {
							System.out.println("Your are not in that chat");
						} else {
							currentChatID = e.getChangeRequest().getChatID();
							System.out.println("Chat changed");
						}
					} else if (object instanceof InfoException) {
						System.out.println(((InfoException) object).getMessage());
					}
				}
			} catch (IOException | ClassNotFoundException e) {
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

		Credential credential;
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
			System.out.println("Wrong arguments!");
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}

	}


}
