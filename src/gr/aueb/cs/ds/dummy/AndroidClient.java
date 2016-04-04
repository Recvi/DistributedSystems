package gr.aueb.cs.ds.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Message.MessageType;
import gr.aueb.cs.ds.network.Network;

import java.util.UUID;

import static java.lang.Math.random;

public class AndroidClient {

    private ArrayList<Address> mapAddresses;
    private Address reducerAddress;
    private String requestId;
    private Map<Address, ArrayList<String>> mappersData;
    
    //Number of mappers that have not respond yet.
    private int pendingRequest;

    public AndroidClient(ArrayList<Address> mapAddresses, Address reducerAddress) {
        this.mapAddresses = mapAddresses;
        this.reducerAddress = reducerAddress;
        pendingRequest = mapAddresses.size();
        
        this.mappersData = new HashMap<Address, ArrayList<String>>();
        
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
		
		distributeToMappers(llpoint, trpoint, datetimeStart, datetimeEnd);
       // distributeToMappers();
    }

    private void distributeToMappers(String[] llpoint, String[] trpoint, String datetimeStart, String datetimeEnd) {
		
		double lowerLeftLat = Double.parseDouble(llpoint[0]);
		double topRightLat = Double.parseDouble(trpoint[0]);
	    double latDiff = topRightLat - lowerLeftLat;
	    double latStep = latDiff / mapAddresses.size();
	    
	    topRightLat = lowerLeftLat + latStep;   // reinitialize for first mapper.
	    
	    for (Address addr : mapAddresses) {
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

	    System.out.println("Distributing to mappers with clientId: " + requestId);
	    
	    int mappers_num = mapAddresses.size();
	    for (int i=0; i<mappers_num; i++) {
	    	new Thread() {
                public void run() {
                	Address mapperAddress = getNextMapper();
                	Message msg = new Message(requestId, MessageType.MAP, mappersData.get(mapperAddress));
                	Network.sendRequest(msg, mapperAddress);
                	waitForMappers();
                }
            }.start();
	    }
	}

    private void distributeToMappers(){
        requestId = UUID.randomUUID().toString();
        System.out.println("Master:I am distributing to mappers with requestId:"+requestId);
        for(int i=0;i<mapAddresses.size();i++){
            new Thread()
            {
                public void run() {
                    sendToMapper();
                }
            }.start();
        }
    }

    private void sendToMapper(){
        Message message=new Message(requestId, MessageType.MAP, "Area:" + random());
        Network.sendRequest(message, getNextMapper());
        waitForMappers();
    }

    private synchronized Address getNextMapper(){
        return mapAddresses.remove(0);
    }

    private synchronized void waitForMappers(){
        pendingRequest--;
        if (pendingRequest<1){
            ackToReducer();
        }
    }

    private void ackToReducer(){
        Message message=new Message(requestId, MessageType.REDUCE, "Reduce Now!");
        System.out.println("Master:I received from reducer:"+(String)Network.sendRequest(message, reducerAddress));
    }
    
}
