package Client;

import Exceptions.LoginExistsException;
import Units.UID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;

public class Client {
	static final String driver = "com.mysql.cj.jdbc.Driver";
	static final String url = "jdbc:mysql://localhost:9999/cm?serverTimezone=UTC";
	static final String login = "root";
	static final String password = "root";
	Scanner sc = new Scanner(System.in);
	String str = "";
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;

	public Client() {
		Scanner scan = new Scanner(System.in);

		try {
			socket = new Socket("127.0.0.1", 9998);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			sign();
			Resender resend = new Resender();
			resend.start();


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
					String str = in.readLine();
					System.out.println(str);
				}
			} catch (IOException e) {
//TODO log
			}
		}
	}


	void sign() {
		System.out.println("Welcome!");
		System.out.print("1) Sign In\n2) Sign Up\nEnter number: ");
		try {
			int ans = sc.nextInt();
			if (ans == 1) {
				signIn();
			} else if (ans == 2) {
				signUp();
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
//TODO log
		}

	}

	void signIn() {
		System.out.println("Start Signing In!");
		System.out.print("Enter login: ");
		String newLogin = sc.nextLine();
		System.out.print("Enter password: ");
		String newPassword = sc.nextLine();
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, login, password);
			String checkCredential = "SELECT * FROM users WHERE Login = ? AND Password = ?";
			PreparedStatement stmt = connection.prepareStatement(checkCredential);
			stmt.setString(1, newLogin);
			stmt.setString(2, newPassword);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println("You are logged in!!!");
			} else {
				str = "exit";
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	void signUp() {
		System.out.print("Enter login: ");
		String newLogin = sc.nextLine();
		System.out.print("Enter password: ");
		String newPassword = sc.nextLine();
//		System.out.print("Enter nickname: ");
//		String nickname = sc.nextLine();
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, login, password);
			String checkLogin = "SELECT * FROM users WHERE Login = ?";
			PreparedStatement stmt = connection.prepareStatement(checkLogin);
			stmt.setString(1, newLogin);
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				String newUser = "INSERT INTO users (ID, Login, Password, Hash) VALUES (?, ?, ?, ?)";
				PreparedStatement userStmt = connection.prepareStatement(newUser);
				userStmt.setInt(1, UID.generate());
				userStmt.setString(2, newLogin);
				userStmt.setString(3, newPassword);
				userStmt.setInt(4, (newLogin + newPassword).hashCode());
				userStmt.execute();
				signIn();
			} else throw new LoginExistsException();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (LoginExistsException e) {
//TODO log
		}
	}
}
