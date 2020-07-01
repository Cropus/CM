package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private Socket clientSocket = null;
	private InputStream input = null;
	private OutputStream output = null;
	public ClientHandler(Socket socket) {
		clientSocket = socket;
	}

	@Override
	public void run() {
		try {
			input = clientSocket.getInputStream();
			output = clientSocket.getOutputStream();
		} catch (IOException e) {
//TODO log
		}

		byte[] buffer = new byte[4096];
		while (true) {
			try {
				int letters = input.read(buffer);
				if (letters > 0) {
					String message = new String(buffer, 0, letters);
					sendData(message.getBytes());
					System.out.println(message);
				} else {
//TODO log
					clientSocket.close();
					break;
				}
			} catch (IOException e) {
//TODO log
			}
		}
	}

	public void sendData(byte[] message) {
		try {
			output.write(message);
			output.flush();
		} catch (IOException e) {
//TODO log
		}
	}
}
