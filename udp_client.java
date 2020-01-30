
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class udp_client {
	// Socket to send and receive data with server
	static DatagramSocket socket;
	// Network address of server
	static InetAddress address;
	// Network port of server
	static int port;
	// Name of Student
	static String name;
	// Datagram packet to send/receive data with server
	static DatagramPacket packet = null;
	// Raw packet data exchanged with server
	static byte[] buffer = new byte[50000];
	// Keep track of whether this student is at the head of the queue. Assume false.
	static boolean headOfQueue = false;


	public static void main(String[] args) {
		/*
		 * Establish connection with server
		 * args[0] = IP address
		 * args[1] = port
		 * args[2] = Student's name
		 */
		udp_client client = new udp_client(args[0], args[1], args[2]);

		// Wait until we are notified by the server that we are at the head of the queue
		while(headOfQueue == false) {
			String s = receiveMessage();
			// If we receive a forward slash from the server, we are at the head of the queue
			if(s.charAt(0) == '/') {
				headOfQueue = true;
			}
		}

		// Now we must wait until we have been helped (wait for std input)
		client.waitUntilHelped();

	}

	/*
	 * Attempts to receive a message from server
	 * @return the message received, or a blank string if no message received
	 */
	public static String receiveMessage() {
		// Clear the buffer before trying to receive any message
		buffer = new byte[50000];
		// Set up our DatagramPacket to receive the data
		packet = new DatagramPacket(buffer, buffer.length);
		String s = "";
		try {
			// Received a message, set s = message
			socket.receive(packet);
			s = new String(buffer);

		} catch (IOException e) {
			// Didn't receive a message, leave s = ""
			System.out.println("Couldn't receive data from server.");
			e.printStackTrace();
		}

		return s;
	}

	/*
	 * Sends a message to the server
	 * @message the message to be sent
	 * @isEnteringQueue whether or not the student is entering the queue
	 */
	public static void sendMessage(String message, boolean isEnteringQueue) {

		// Clear the buffer before trying to receive any message. Then set it equal to the byte form of the message String
		buffer = new byte[50000];
		buffer = message.getBytes();

		packet = new DatagramPacket(buffer, buffer.length, address, port);

		try {
			socket.send(packet);
			if(isEnteringQueue) {
				System.out.println("Welcome to the office hourse queue! You will be notified when someone is ready to help you.");
			}

		} catch (IOException e) {
			if(isEnteringQueue) {
				System.out.println("Couldn't contact server to join queue.");
			}

			e.printStackTrace();
		}
	}

	// Signal to server that we have been helped when we use the std input
	public void waitUntilHelped() {
		Scanner kb = new Scanner(System.in);

		// Prompt student to input text when they have been helped
		System.out.println("You can now ask questions in office hours! Type any message to indicate you have been helped and you will be removed from the queue.");
		String exit = kb.nextLine();

		/*
		 * Received input at this point. Sending ';' to server to signal we have been helped
		 */
		sendMessage(";", false);
	} 




	/*
	 * Establish initial connection with server using specified IP address, port and the student's name 
	 */
	public udp_client(String address, String port, String name) {
		// Attempt to convert String address to InetAddress format
		try {
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			System.out.println("Here");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set our global server port variable to the entered port
		this.port = Integer.parseInt(port);


		// Establish socket connection with server
		try {
			socket = new DatagramSocket();
			System.out.println("Established connection at:" + this.address.toString());
		} catch (SocketException e1) {
			System.out.println("Couldn't establish socket at specified port and address.");
			e1.printStackTrace();
		}

		// Add -1 prefix to the student's name to signal to the server that we'd like to enter the queue
		this.name = "-1:" + name;
		sendMessage(this.name, true);

	}

}
