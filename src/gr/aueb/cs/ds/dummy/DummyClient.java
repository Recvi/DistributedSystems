package gr.aueb.cs.ds.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Message.MessageType;
import gr.aueb.cs.ds.network.Network;

public class DummyClient {
	
	/*
	 * Reads from config.properties and initializes
	 * variables concerning K value, database connection
	 * information and IP and ports of different machines.
	 */
	private ConfigReader conf;
	private Map<Address, ArrayList<String>> mappersData;
	private ArrayList<Address> mapperAddresses;
	private int pendingRequests;
	private String clientId;

	public DummyClient() {
		
		/*
		 * Reads from config.properties and initializes
		 * variables concerning K value, database connection
		 * information and IP and ports of different machines.
		 */
		this.conf = new ConfigReader();
		this.mappersData = new HashMap<Address, ArrayList<String>>();
		this.mapperAddresses = conf.getMappers();
		this.pendingRequests = mapperAddresses.size();
		this.clientId = UUID.randomUUID().toString();
		
		Scanner in = new Scanner(System.in);

		/*
		 * Getting all the data to perform a search.
		 */
		System.out.println("To do a search for popular Points of Interest in an area, fill in the next fields.");
		System.out.println("Define a region by providing the lower left and top right point.");
		System.out.print("Lower left point (latitude,longitude): ");
		String[] llpoint = in.nextLine().split(",");   // Getting the lower left point
		System.out.print("Top right point (latitude,longitude): ");
		String[] trpoint = in.nextLine().split(",");   // Getting the top right point
		System.out.println("Define the time frame of your search by providing a date and time starting point and ending point.");
		System.out.print("Date and time starting point (YYYY-MM-DD hh:mm:ss): ");
		String datetimeStart = in.nextLine();  // Getting the starting datetime
		System.out.print("Date and time ending point (YYYY-MM-DD hh:mm:ss): ");
		String datetimeEnd = in.nextLine();   // Getting the ending datetime
		
		/*
		 * Starting the search.
		 */
		distributeToMappers(llpoint, trpoint, datetimeStart, datetimeEnd);
		
	}
	
	
	/*
	 * Start the search.
	 * Split data to mappers, create new Threads to handle individual connections.
	 */
	private void distributeToMappers(String[] llpoint, String[] trpoint, String datetimeStart, String datetimeEnd) {
		
		double lowerLeftLat = Double.parseDouble(llpoint[0]);
		double topRightLat = Double.parseDouble(trpoint[0]);
	    double latDiff = topRightLat - lowerLeftLat;
	    double latStep = latDiff / mapperAddresses.size();
	    
	    topRightLat = lowerLeftLat + latStep;   // reinitialize for first mapper.
	    
	    for (Address addr : mapperAddresses) {
	    	ArrayList<String> data = new ArrayList<String>();
	    	data.add(Double.toString(lowerLeftLat));
	    	data.add(llpoint[1]);
	    	data.add(Double.toString(topRightLat));
	    	data.add(trpoint[1]);
	    	data.add(datetimeStart);
	    	data.add(datetimeEnd);
	    	
	    	/*
	    	 * Change values to reflect the next mapper's data.
	    	 */
	    	lowerLeftLat += latStep;
	    	topRightLat += latStep;
	    	
	    	mappersData.put(addr, data);
	    }

	    System.out.println("Distributing to mappers with clientId: " + clientId);
	    
	    int mappers_num = mapperAddresses.size();
	    for (int i=0; i<mappers_num; i++) {
	    	new Thread() {
                public void run() {
                	Address mapperAddress = getNextMapperAddress();
                	Message msg = new Message(clientId, MessageType.MAP, mappersData.get(mapperAddress));
                	Network.sendRequest(msg, mapperAddress);
                	waitForMappers(mapperAddress);
                }
            }.start();
	    }
	}
	
	private synchronized Address getNextMapperAddress() {
		Address addr = mapperAddresses.remove(0);
		System.out.println("Client thread: Sending to " + addr.getIp() + ":" + addr.getPort());
		return addr;
	}
	
	private synchronized void waitForMappers(Address addr) {
		 pendingRequests--;
		 System.out.println("Mapper at " + addr.getIp() + ":" + addr.getPort()+ " is done.");
	        if (pendingRequests < 1){
	            ackToReducer();
	        }
	}
	
	private void ackToReducer(){
		Address reducerAddr = conf.getReducer();
		System.out.println("Notifying Reducer at " + reducerAddr.getIp() + ":" + reducerAddr.getPort());
        Message message = new Message(clientId, MessageType.REDUCE, new String("You can reduce!"));
        collectDataFromReducer((HashMap<String, ArrayList<String>>) Network.sendRequest(message, conf.getReducer()));
    }
	
	private void collectDataFromReducer(HashMap<String, ArrayList<String>> data) {
	}
}
