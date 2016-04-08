package gr.aueb.cs.ds.worker.reduce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.NetworkHandler;
import gr.aueb.cs.ds.network.Message.MessageType;
import gr.aueb.cs.ds.worker.map.Checkin;

public class ReduceWorker extends Thread{


    /*
     * Will have an array/stack/hash-map here,
     * to hold data received from mappers until,
     * the corresponding master asks for them to be reduced.
     * Will use Map<String,ArrayList<String>> for now.
     */
	private ArrayList<ArrayList<Checkin>> data;
	private Message msg;
	private ConfigReader conf;
	private NetworkHandler net;


    public ReduceWorker(NetworkHandler net, Message msg, ConfigReader conf, ArrayList<ArrayList<Checkin>> mapper_data) {
    	this.data = mapper_data;
    	this.msg = msg;
    	this.conf = conf;
    	this.net = net;    
    }
    
    public void run() {
    	
    	System.out.println("\t----- REDUCER -----");
    	
    	Map<Checkin,Set<String>> results = reduce();
    	
    	System.out.println("\tReducer: finished reducing. POI's: " + results.size());
    	
//    	Network.sendRequest(con, new Message(msg.getClientId(), MessageType.ACK, results), conf.getClient());
    	System.out.println("\tReducer: Sending data to Client.");
    	net.sendMessage(new Message(msg.getClientId(), MessageType.ACK, results));
    	net.close();
    	
    }
   


    private Map<Checkin,Set<String>> reduce() {
    	/*
    	 * Get global top k
    	 */
    	List<List<Checkin>> top_data = data.stream().sorted((a1,a2) -> (-1) * Integer.compare(a1.size(), a2.size()))
    	.limit(conf.getK()).collect(Collectors.toCollection(ArrayList::new));
    	
    	/*
    	 * Convert from:
    	 * List<List<Checkin>> to Map<Checkin,Set<String>>
    	 */
    	Map<Checkin,Set<String>> results = top_data.parallelStream()
    		.collect(
				()-> new HashMap<>(),
				(c,e) -> {
					
					Checkin checkin = e.get(0);  // get a random Checkin, let's say first one.
//					e.size(); // Number of Checkins per POI.
//					TODO: Add Number of Checkins per POI to results.
					/*
					 * Add the photos from all the Checkins
					 * into a Set to remove duplicates.
					 */
					Set<String> photos = e.stream().collect(
							 ()-> new HashSet<>(),
							 (s,el) -> s.add(el.getPhotos()),
							 (s1, s2) -> s1.addAll(s2));
					c.put(checkin, photos);	 
				},
				(m1,m2) -> m1.putAll(m2));
    	
    	return results;
    }

    private void getResults(Map<Integer,Object> data) {
        /* ... */
    }

}
