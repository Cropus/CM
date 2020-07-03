package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;

	public Client() {
		Scanner scan = new Scanner(System.in);

		try {
			socket = new Socket("127.0.0.1", 9998);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("Enter your name");
			out.println(scan.nextLine());

			Resender resend = new Resender();
			resend.start();

			String str = "";
			while (!str.equals("exit")) {
				str = scan.nextLine();
				out.println(str);
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
			socket.close();
		} catch (Exception e) {
			System.err.println("��⮪� �� �뫨 �������!");
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
					String str = in.readLine();
					System.out.println(str);
				}
			} catch (IOException e) {
				System.err.println("�訡�� �� ����祭�� ᮮ�饭��.");
				e.printStackTrace();
			}
		}
	}


	void sign() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome!");
		System.out.print("1) Login\n2) Register\nEnter number: ");
		try {
			int ans = sc.nextInt();
			if (ans == 1) {
				login();
			} else if (ans == 2) {
				register();
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
//TODO log
		}

	}

	void login() {

	}

	void register() {

	}
}
