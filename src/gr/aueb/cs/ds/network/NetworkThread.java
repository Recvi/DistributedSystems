package gr.aueb.cs.ds.network;

import gr.aueb.cs.ds.worker.Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkThread extends Thread{
	
	//Could use boolean/bit answer codes;
	private final static String successMessage="All Good";
	private final static String errorMessage="Yo mama is fat";
	
	private Worker worker;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public NetworkThread(Socket connection, Worker worker) {
		this.worker=worker;
		try {
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Reads data from input connections,
	 * passes them to worker,waits for answer from worker,
	 * replies to connection.
	 */
	@Override
	public void run() {
		try {
			Object data = in.readObject();
			boolean allGood=(boolean)worker.onNewTask(data);
			if (allGood){
				out.writeObject(successMessage);
			}else{
				out.writeObject(errorMessage);
			}
			out.flush();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}
