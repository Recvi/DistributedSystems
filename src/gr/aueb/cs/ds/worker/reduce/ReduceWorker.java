package gr.aueb.cs.ds.worker.reduce;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
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
	private Socket con;
	private Message msg;
	private ConfigReader conf;
	private ObjectInputStream in;
	private ObjectOutputStream out;


    public ReduceWorker(Socket con, Message msg, ConfigReader conf, ArrayList<ArrayList<Checkin>> mapper_data) {
    	this.con = con;
    	this.data = mapper_data;
    	this.msg = msg;
    	this.conf = conf;
    	
    	try {
			this.out = new ObjectOutputStream(con.getOutputStream());
			this.in = new ObjectInputStream(con.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
    
    }
    
    public void run() {
    	
    	Map<Checkin,Set<String>> results = reduce();
    	
    	Network.sendRequest(con, new Message(msg.getClientId(), MessageType.ACK, results), conf.getClient());
    	
    }
   


    private Map<Checkin,Set<String>> reduce() {
    	/*
    	 * Get global top k
    	 */
    	List<List<Checkin>> top_data = data.stream().sorted((a1,a2) -> Integer.compare(a1.size(), a2.size()))
    	.limit(conf.getK()).collect(Collectors.toList());
    	
    	/*
    	 * Convert from:
    	 * List<List<Checkin> to Map<Checkin,Set<String>>
    	 */
    	Map<Checkin,Set<String>> results = top_data.parallelStream()
    		.collect(
    				()-> new HashMap<>(),
    				(c,e) -> {
    				 Set<String> photos = e.stream().collect(
    						 ()-> new HashSet<>(),
    						 (s,el) -> s.add(el.getPhotos()),
    						 (s1, s2) -> s1.addAll(s2));
    				 c.put(e.get(0), photos);	 
    			 },
    			 (m1,m2) -> m1.putAll(m2));
    	
    	return results;
    }

    private void getResults(Map<Integer,Object> data) {
        /* ... */
    }

}
