package gr.aueb.cs.ds.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
import gr.aueb.cs.ds.worker.map.Checkin;
import gr.aueb.cs.ds.worker.map.MapWorker;
import gr.aueb.cs.ds.worker.reduce.ReduceWorker;
import gr.aueb.cs.ds.network.Message.MessageType;

public class WorkerSpawner {

	public static void main(String[] args) {
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		HashMap<String, ArrayList<ArrayList<Checkin>>> mapper_data = new HashMap<String, ArrayList<ArrayList<Checkin>>>();
		
		ConfigReader conf = new ConfigReader();
		Address addr = null;
		if (args[0].equals("REDUCE")) {
			addr = conf.getReducer();
		} else if (args[0].equals("MAP")){
			addr = conf.getMappers().get(Integer.parseInt(args[1]));
		}
		try {
			ServerSocket providerSocket = new ServerSocket(addr.getPort(), 10, InetAddress.getByName(addr.getIp()));
			
			while (true) {
				Socket connection = providerSocket.accept();
				///////////////////////////////////////
				System.out.println("Got connection.");
				//////////////////////////////////////
				ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
				Message msg = (Message)in.readObject();
				
				System.out.println("I read message.");
				
				switch (msg.getMsgType()) {
				case MAP:
					///////////////////////////////////////
					System.out.println("Got MAP Message.");
					//////////////////////////////////////
					Thread map = new MapWorker(connection, msg, conf);
					threads.add(map);
					map.start();
					break;
				case REDUCE:
					///////////////////////////////////////
					System.out.println("Got REDUCE Message.");
					//////////////////////////////////////
					Thread reduce = new ReduceWorker(connection, msg, conf, mapper_data.get(msg.getClientId()));
					threads.add(reduce);
					reduce.start();
					break;
				case MAPPER_DATA:
					///////////////////////////////////////
					System.out.println("Got MAPPER_DATA.");
					//////////////////////////////////////
//					Thread iw = new IntermediateWorker(connection, msg);
//					threads.add(iw);
//					iw.start();
					mapper_data.put(msg.getClientId(),(ArrayList<ArrayList<Checkin>>)msg.getData());
					break;
				}
			}
		} catch (UnknownHostException e2) {
			System.err.println("You are trying to connect to an unknown host");
		} catch (ClassNotFoundException cnf) {
			cnf.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {			
			waitForTasksThread(threads);
		}
	}
	
	public static void waitForTasksThread(ArrayList<Thread> threads) {
		try {
//			threads.stream().forEach(t -> t.join());
			for (Thread t : threads) {
				t.join();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
