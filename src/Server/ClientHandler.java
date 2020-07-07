package Server;

import Exceptions.ChatException;
import Exceptions.ChatNotFoundException;
import Exceptions.InfoException;
import Exceptions.LoginExistsException;
import Units.Chats.PublicChat;
import Units.Credential;
import Units.Message;
import Units.Requests.ChangeRequest;
import Units.Requests.CreateRequest;
import Units.Requests.EnterRequest;
import Units.UID;
import Units.User;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ClientHandler implements Runnable {
	private ArrayList<Integer> myChats = new ArrayList<>();
	private String name;
	private Server server;
	private int currentID = 0;
	private int currentChatID;
	private final String driver = "com.mysql.cj.jdbc.Driver";
	private final String url = "jdbc:mysql://localhost:9999/cm?serverTimezone=UTC";
	private final String login = "root";
	private final String password = "root";
	Connection connection;
	private SSLSocket clientSocket;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private String str = "";
	public ClientHandler(SSLSocket socket, Server server) {
		this.server = server;
		clientSocket = socket;
		currentChatID = 0;

		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, login, password);
			input = new ObjectInputStream(clientSocket.getInputStream());
			output = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Socket problem");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Problems with DB");
			e.printStackTrace();
		}
	}
	private void handleCredential(Credential cred) {
		if (!cred.isReg()) {
			signUp(cred);
		} else {
			signIn(cred);
		}
		name = cred.getLogin();
	}
	private void handleChangeRequest(ChangeRequest changeRequest) throws IOException {
		if (!myChats.contains(changeRequest.getChatID())) {
			output.writeObject(new ChatNotFoundException(true, changeRequest));
		} else {
			currentChatID = changeRequest.getChatID();
			output.writeObject(new ChatNotFoundException(false, changeRequest));
		}
		output.writeObject(server.chats.get(currentChatID));
	}
	private void handleCreateRequest(CreateRequest createRequest) throws SQLException {
		PublicChat newChat = new PublicChat(((CreateRequest) createRequest).getName());
		myChats.add(newChat.getID());
		String addChatAndUsers = "INSERT INTO chatsandusers (ChatID, UserID) VALUES (?, ?)";
		PreparedStatement userStmt = connection.prepareStatement(addChatAndUsers);
		userStmt.setInt(1, newChat.getID());
		userStmt.setInt(2, currentID);
		userStmt.execute();
		String addChatAndNames = "INSERT INTO chatsandnames (ChatID, Name) VALUES (?, ?)";
		PreparedStatement nameStmt = connection.prepareStatement(addChatAndNames);
		nameStmt.setInt(1, newChat.getID());
		nameStmt.setString(2, newChat.getName());
		nameStmt.execute();
		currentChatID = newChat.getID();
	}
	private void handleEnterRequest(EnterRequest enterRequest) throws SQLException, ChatException, IOException {
		String searchChat = "SELECT * FROM chatsandnames WHERE Name = ?";
		PreparedStatement searchStmt = connection.prepareStatement(searchChat);
		searchStmt.setString(1, enterRequest.getChatName());
		ResultSet searchResult = searchStmt.executeQuery();
		if (searchResult.next()) {
			currentChatID = searchResult.getInt(1);
			String enterChat = "INSERT INTO chatsandusers (ChatID, UserID) VALUES (?, ?)";
			PreparedStatement enterChatStmt = connection.prepareStatement(enterChat);
			enterChatStmt.setInt(1, currentChatID);
			enterChatStmt.setInt(2, currentID);
			enterChatStmt.execute();

			myChats.add(currentChatID);
		} else {
			throw new ChatException();
		}
		output.writeObject(server.chats.get(currentChatID));
	}
	private void addMessage(Message message) throws SQLException {
		String newMessage = "INSERT INTO messages (ID, text, `from`, `to`, fromName) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement preparedStmt = connection.prepareStatement(newMessage);
		preparedStmt.setInt(1, message.getID());
		preparedStmt.setString(2, str);
		preparedStmt.setInt(3, currentID);
		preparedStmt.setInt(4, currentChatID);
		preparedStmt.setString(5, name);
		preparedStmt.execute();
		String connectMessageToChat = "INSERT INTO chatsandmessages (ChatID, MessageID) VALUES (?, ?)";
		PreparedStatement connectMtoC = connection.prepareStatement(connectMessageToChat);
		connectMtoC.setInt(1, currentChatID);
		connectMtoC.setInt(2, message.getID());
		connectMtoC.execute();
		if (!server.chats.containsKey(currentChatID)) {
			server.chats.put(currentChatID, new ArrayList<>());
		}
		message.setFrom(currentID);
		message.setName(name);
		server.chats.get(currentChatID).add(message);
	}

	@Override
	public void run() {
		while (true) {
			try {

				Object in = input.readObject();
				if (in instanceof Credential) {
					handleCredential((Credential) in);
				} else if (in instanceof ChangeRequest) {
					handleChangeRequest((ChangeRequest) in);
				} else if (in instanceof CreateRequest) {
					handleCreateRequest((CreateRequest) in);
				} else if (in instanceof EnterRequest) {
					handleEnterRequest((EnterRequest) in);
				} else if (in instanceof Message) {
					Message message = (Message) in;
					message.setID(UID.generate());
					message.setFrom(currentID);
					str = message.getText();
					if (str.equals("exit")) break;
					addMessage(message);

					synchronized (server.clients) {
						Iterator<ClientHandler> iter = server.clients.iterator();
						while (iter.hasNext()) {
							ClientHandler cl = iter.next();
							if (cl != null && cl.myChats.contains(currentChatID) && (cl.currentChatID == currentChatID)) {
								if (cl != this) {
									cl.output.writeObject(name + ": " + str);
									cl.output.flush();
								}
							}
						}
					}

				} else throw new IllegalArgumentException();
			} catch (IOException | NullPointerException |
					ClassNotFoundException | SQLException |
					ChatException e) {
				e.printStackTrace();
			}
		}
	}

	void signIn(Credential credential) {
		try {
			String checkCredential = "SELECT * FROM users WHERE Hash = ?";
			PreparedStatement stmt = connection.prepareStatement(checkCredential);
			stmt.setInt(1, credential.getHash());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				currentID = rs.getInt(1);
				String getChats = "SELECT * FROM chatsandusers WHERE UserID = ?";
				PreparedStatement chatStmt = connection.prepareStatement(getChats);
				chatStmt.setInt(1, currentID);
				ResultSet chats = chatStmt.executeQuery();
				while (chats.next()) {
					myChats.add(chats.getInt(1));
				}
				output.writeObject(new User(name));
				output.flush();
				output.writeObject(server.chats.get(currentChatID));
			} else {
				str = "exit";
				output.writeObject(new InfoException("Wrong data"));
				output.flush();
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	void signUp(Credential credential) {
		try {
			String checkLogin = "SELECT * FROM users WHERE Hash = ?";
			PreparedStatement stmt = connection.prepareStatement(checkLogin);
			stmt.setInt(1, credential.getHash());
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				String newUser = "INSERT INTO users (ID, Login, Password, Hash) VALUES (?, ?, ?, ?)";
				PreparedStatement userStmt = connection.prepareStatement(newUser);
				currentID = UID.generate();
				userStmt.setInt(1, currentID);
				userStmt.setString(2, credential.getLogin());
				userStmt.setString(3, credential.getPassword());
				userStmt.setInt(4, credential.getHash());
				userStmt.execute();
				String addToGeneral = "INSERT INTO chatsandusers (ChatID, UserID) VALUES (?, ?)";
				PreparedStatement generalStmt = connection.prepareStatement(addToGeneral);
				generalStmt.setInt(1, 0);
				generalStmt.setInt(2, currentID);
				generalStmt.execute();
				myChats.add(0);
				output.writeObject("Registration complete");
				output.writeObject(new User(name));
				output.flush();
				output.writeObject(server.chats.get(currentChatID));
				output.flush();
			} else {
				output.writeObject(new InfoException("Login not available"));
				output.flush();
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
}
