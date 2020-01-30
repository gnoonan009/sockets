import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.stream.Collectors;

public class mystery_socket_receiver {
	// Socket to receive data from TCP port
	private static Socket socket = null; 
	// ServerSocket to help socket receive data from TCP port
	private static ServerSocket server = null;
	// Stream to read input from TCP port
	private static BufferedReader input = null;
	// Boolean to continuously check for TCP data
	private static boolean online = false;


	public mystery_socket_receiver(int port) throws IOException {
		// Set up server with specified port
		server = new ServerSocket(port);
		// Makes the connection with TCP port
		socket = server.accept();
		// Sets up the stream to read input from TCP port
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// We want to continuously check for data, so leave this as true
		online = true;

	}
	/*
	 * Decrypts mysterious encoded message
	 * @return decrypted password
	 */
	public String getPassword(String message) {
		if(!message.contains("PASSWORD") || !message.contains(".")) {
			return "";
		}
		int start = message.indexOf("PASSWORD=") + 9;
		int end = message.indexOf(".", start);

		return message.substring(start,end);

	}

	public static void main(String[] args) throws IOException {
		// Create our listener on TCP port 21212
		mystery_socket_receiver listener = new mystery_socket_receiver(21212);

		// Check for text containing "PASSWORD=" and "." and decrypt the passwords contained
		while(online) {
			// Need to use a scanner as password can appear on any line
			Scanner kb = new Scanner(input);
			while(kb.hasNext()) {
				String password = listener.getPassword(kb.next());
				if(!password.equals("")) {
					System.out.println(password);
				}	
			}

		}


		// Close our sockets
		socket.close();
		input.close();
		server.close();
	}
}
