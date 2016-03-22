package gr.aueb.cs.ds;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.io.*;
import java.net.*;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.worker.map.MapWorker;

public class TestFile {

	
	public static void main(String[] args) {
		//Einai se new thread epeidi ftiaxnw kai client kai server apo idio instance.
		new Thread()
		{
		    public void run() {
		    	MapWorker mapwork=new MapWorker(2323,new Address("localhost",5666));
		    }
		}.start();
		
		//Kanw read epeidi prepei na perimenw ligo na aniksei o server.
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    System.out.println("Press enter to send request.");
	    try {
			String s = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        fakeClient();
	}
	
	private static void fakeClient(){
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			requestSocket = new Socket("localhost", 2323);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("fakeClient:Sending YOLO");
			out.writeObject("YOLO");
			out.flush();

			try {
				System.out.println("fakeClient:I received " + (String)in.readObject());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				in.close();	out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	
	
	private void testProp(){
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("src/config.properties");
			prop.load(input);

			System.out.println(prop.getProperty("database_ip"));
			System.out.println(prop.getProperty("database_port"));
			System.out.println(prop.getProperty("database_user"));
			System.out.println(prop.getProperty("database_password"));

		} catch (IOException e) {
			System.out.println("Config file not found.");
		}
	}
}
