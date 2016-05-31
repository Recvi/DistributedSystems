package gr.aueb.cs.ds.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException; 

public class NetworkHandler {
	
	private Socket connection;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	
	
	/*
	 * Network handler that gets an existing socket.
	 */
	public NetworkHandler(Socket connection) {
		this.connection = connection;
		this.out = null;
		this.in = null;
		 
		try {
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
		} catch(UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch(IOException io) {
			io.printStackTrace();
		}
	}
	
	/*
	 * Network handler that creates new socket from an Address.
	 */
	public NetworkHandler(Address addr) {
		this.connection = null;
		this.out = null;
		this.in = null;
		 
		try {
			this.connection = new Socket(InetAddress.getByName(addr.getIp()), addr.getPort());
			this.out = new ObjectOutputStream(connection.getOutputStream());
			this.in = new ObjectInputStream(connection.getInputStream());
		} catch(UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch(IOException io) {
			io.printStackTrace();
		}
	}
	
	public void sendMessage(Message msg) {
		try {
			System.out.println("\t\tNetwork Handler: Sending message.");
			out.writeObject(msg);
			out.flush();
		} catch(IOException io) {
			io.printStackTrace();
		}
	}
	
	public Message readMessage() {
		Message msg = null;
		try {
			msg = (Message)in.readObject();
			System.out.println("\t\tNetwork Handler: Read message.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	public void close() {
		try {
			in.close();
			out.close();
			connection.close();
			System.out.println("\t\tNetwork Handler: Closed socket.");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

}
