
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class udp_server {
	// List to store queue of students 
	static ArrayList<Student> queue;
	// Port for server to listen on
	int port;
	// Datagram socket for packets to travel
	static DatagramSocket socket;
	// IP address of the server (whatever address is running program). Not super necessary but included it 
	InetAddress address = null;
	// Datagram packet to send and receive data with clients
	static DatagramPacket packet = null;
	// Raw packet data received from client
	static byte[] buffer = new byte[50000];


	public static void main(String[] args) {
		// Create our server instance with specified port
		udp_server server = new udp_server(args[0]);

		boolean online = true;

		while(online) {
			// Message received by client
			String s = receiveMessage();

			// Message proceeded by -1 means they want to be added to queue
			if(s.substring(0,3).equals("-1:")) {
				// Add student to queue
				String studentName = s.substring(3);
				Student student = new Student(studentName, packet);
				queue.add(student);
				// Output current queue status to console
				System.out.println(student.getName()+" joined the queue.");
				printQueue();
			}

			// Check for students leaving queue
			updateQueue(packet, s);

			// Clear buffer before receiving more packets
			buffer = new byte[50000];
		}

	}

	/*
	 * Attempts to receive a message from client
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
	 * Sends a message to the client
	 * @message the message to be sent
	 * @student the student for the message to be sent to
	 */
	public static void sendMessage(String message, Student student, boolean headOfQueue) {

		// Clear the buffer before trying to receive any message. Then set it equal to the byte form of the message String
		buffer = new byte[50000];
		buffer = message.getBytes();

		packet = new DatagramPacket(buffer, buffer.length, student.getAddress(), student.getPort());

		try {
			socket.send(packet);
			if(headOfQueue) {
				System.out.println("Successfully notified "+student.getName()+" at head of queue.");
			}
		} catch (IOException e) {
			if(headOfQueue) {
				System.out.println("Failed to notify client at head of queue.");
			}
			e.printStackTrace();
		}
	}


	public udp_server(String p) {
		// Set up the office hours queue and connection for communicating with students
		queue = new ArrayList<Student>();
		port = Integer.parseInt(p);

		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set up socket to send and receive data
		try {
			socket = new DatagramSocket(port, address);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		System.out.println("Server connection established at " +address.toString());
	}

	public static void updateQueue(DatagramPacket packet, String message) {
		// If the queue is empty we don't need to make changes to the queue
		if(queue.size() == 0)
			return;

		// Get current student at head of queue
		Student current = queue.get(0);

		// Has that student been notified that they are at the head of the queue?
		if(current.isCurrent() == false) {
			current.beingHelped();

			// We send the forward slash as a code to tell clients they are at the head of the queue
			sendMessage("/", current, true);
		}
		// If student is at top of queue and has finished being helped
		else 
		{
			// Code to exit the queue is ';'
			if(current.getAddress().equals(packet.getAddress()) && message.charAt(0) == ';') {
				System.out.println(current.getName()+" has been helped and removed from the queue.");
				queue.remove(0);
				// Output updated status of queue
				printQueue();
				// Call method again so it will notify new student at head of queue
				updateQueue(packet, "");
			}
		}

	}

	// Prints queue data
	public static void printQueue() {
		System.out.println("Students in queue: "+queue.size());
		for(int i = 0; i < queue.size(); i++) {
			System.out.println("Queue["+i+"] = "+ queue.get(i).getName());
		}
	}

	// Helper class to manage students on the queue
	static class Student {
		String name;
		boolean current;
		InetAddress address;
		int port;

		public Student(String name, DatagramPacket packet){
			this.name = name;
			this.current = false;
			this.address = packet.getAddress();
			this.port = packet.getPort();
		}

		public String getName() {
			return name;
		}

		public void beingHelped() {
			current = true;
		}

		public boolean isCurrent() {
			return current;
		}

		public int getPort() {
			return port;
		}

		public InetAddress getAddress() {
			return address;
		}
	}
}
