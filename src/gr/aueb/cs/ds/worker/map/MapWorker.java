package gr.aueb.cs.ds.worker.map;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
import gr.aueb.cs.ds.network.NetworkHandler;
import gr.aueb.cs.ds.network.Message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MapWorker extends Thread {

	private NetworkHandler net;
    private Message msg;
    private ConfigReader conf;

	
	private ArrayList<Checkin> checkins;
	
    
    public MapWorker (NetworkHandler net, Message msg, ConfigReader conf) {
    	
    	this.net = net;
    	this.msg = msg;
    	this.conf = conf;

    	this.checkins = new ArrayList<Checkin>();
    }

    public void run() {
    	
    	System.out.println("\t----- MAPPER ------");
    	
    	/*
    	 * Reading the data in the same order we sent them.
    	 * (could send a Map<"property_name", value> instead to make it readable)
    	 */
    	ArrayList<String> msgData = (ArrayList<String>)msg.getData();
    	
    	/*
    	 * Query db and fill checkins data structure.
    	 */
    	getDataFromDb(msgData);
   
    	System.out.println("\tMapper: Got Data from Db. Records: " + checkins.size());
    	
    	
    	List<List<Checkin>> results = map(msgData);
    	
    	System.out.println("\tMapper: finished mapping. POI's: " + results.size());
    	

    	
    	/*
    	 * Send intermediate data to reducer.
    	 */ 
    	System.out.println("\tMapper: Sending data to REDUCER.");
        sendToReducer(results);
    	
        
        
    	/*
    	 * Notify client that the work is completed.
    	 */
        System.out.println("\tMapper: notifying Master.");
        notifyMaster();
    	
    }

    /*
    * Does the actual mapping.
    * Right now signature is random,to be fixed by someone who knows map.
    */
    private List<List<Checkin>> map(ArrayList<String> msgData) {
    	
    	/*
    	 * Get the number of working cpu cores.
    	 */
    	int cores = Runtime.getRuntime().availableProcessors();
    	System.out.println("\tMapper:Splitting data to "+cores+" cores.");
    	
    	/*
    	 * Split data to fit cores.
    	 */
    	double longitudeMin = Double.parseDouble(msgData.get(1));
    	double longitudeMax = Double.parseDouble(msgData.get(3));
    	double lonStep = (longitudeMax - longitudeMin) / cores;
    	ConcurrentMap<Object, List<Checkin>> splitCheckins = checkins.parallelStream().collect(Collectors.groupingByConcurrent(
    			c -> Math.ceil((c.getLongitude() - longitudeMin) / lonStep)
    			));
    	

    	
    	/*
    	 * Map in parallel
    	 */
    	List<List<Checkin>> results = splitCheckins.values().parallelStream().collect(  
    			() -> new ArrayList<>(),  // Supplier
				(c, e) -> {				// Accumulator
					Map<String, List<Checkin>> groupedCheckins = e.stream().collect(
							Collectors.groupingBy( s -> s.getPOI()));
				 	/*
			    	 * Get top k results
			    	 */
					ArrayList<List<Checkin>> res = groupedCheckins.values().stream().sorted(
							(a1,a2) -> (-1) * Integer.compare(a1.size(), a2.size())  // get reverse order: DESC 
							).limit(conf.getK()).collect(Collectors.toCollection(ArrayList::new));
					
					
					c.addAll(res); // add all the Lists to the supplier.					
				},
				(c1, c2) -> {  // Combiner: merge intermediate results
					
					c1.addAll(c2);
					
//					System.out.println("IN");
//					if (c1.size() == 0) {
//						c1.addAll(c2);
////						System.out.println("INNNER");
//					} else {
//						
//						// NEVER GOES IN HERE
//						/*
//						 * Because individual lists are ordered we use a simple
//						 * algorithm to get the top k merged results.
//						 */
//						ArrayList<List<Checkin>> l = new ArrayList<List<Checkin>>();
//						int i = 0;
//						int j = 0;
//						while (l.size() < conf.getK()) {
////							System.out.println("i: "+i+", j: "+j+", l.size: "+l.size()+", c1.size: "+c1.size()+", c2.size: "+c2.size());
//							l.add( ((c1.get(i).size() >= c2.get(j).size()) ? c1.get(i++) : c2.get(j++)) );
//							
//						}
//						c1 = l;
//					}
				}
		);
       
    	return results.stream().sorted(
    			(a1, a2) -> (-1) * Integer.compare(a1.size(), a2.size())
    			).limit(conf.getK()).collect(Collectors.toCollection(ArrayList::new));
    }


    /*
    * Sends the mapped data to $reducerAddress.
    * Needs to return boolean for Error Handling.
    */
    private Message sendToReducer(List<List<Checkin>> results) {
//    	return Network.sendRequest(con, new Message(msg.getClientId(), MessageType.MAPPER_DATA, results), conf.getReducer());
    	NetworkHandler net_reducer = new NetworkHandler(conf.getReducer());
    	net_reducer.sendMessage(new Message(msg.getClientId(), MessageType.MAPPER_DATA, results));
    	Message reply = net_reducer.readMessage();
    	net_reducer.close();
    	return reply;
    }


    /*
     * Notifies client that called for MAP that the job is done.
     */
    private void notifyMaster() {
//        return Network.sendRequest(con, new Message(msg.getClientId(), MessageType.ACK, new String("DONE.")), conf.getClient());
    	net.sendMessage(new Message(msg.getClientId(), MessageType.ACK, new String("DONE.")));
    	net.close();
    }
    
    private void getDataFromDb(ArrayList<String> msgData) {
    	/*
    	 * Connect to db and get the data assigned to me.
    	 * jdbc:mysql://HOST:PORT/DB_NAME?user=USERNAME&password=YOURPASS
    	 */
    	Address db = conf.getDb();
    	String dbURL = "jdbc:mysql://" + db.getIp() + ":" + db.getPort() + "/"
    			+ conf.getDb_name() + "?user=" + conf.getDb_user() + "&password="
    			+ conf.getDb_pass();
    	String dbClass = "com.mysql.jdbc.Driver";
    	
    	
    	String query = "SELECT POI, POI_name, POI_category, latitude, longitude, time, photos "
    			+ "FROM " + conf.getDb_name() + ".checkins WHERE "
    			+ "latitude >= " + msgData.get(0) + " AND latitude <= " + msgData.get(2)
    			+ " AND longitude >= " + msgData.get(1) + " AND longitude <= " + msgData.get(3)
    			+ " AND time >= '" + msgData.get(4) + "' AND time <= '" + msgData.get(5)
    			+ "';";
    	/*
    	 * Execute Query
    	 */
    	try {
    		Class.forName(dbClass);
    		Connection con = DriverManager.getConnection(dbURL);
    		Statement stm = con.createStatement();
    		ResultSet rs = stm.executeQuery(query);
    		
    		
    		/*
    		 * Retrieving data from Result Set.
    		 */
    		while (rs.next()) {
    			String POI = rs.getString(1);
    			String POI_name = rs.getString(2);
    			String POI_category = rs.getString(3);
    			double latitude = rs.getDouble(4);
    			double longitude = rs.getDouble(5);
    			String time = rs.getString(6);
    			String photos = rs.getString(7);
    			
    			Checkin checkin = new Checkin(POI, POI_name, POI_category, latitude, longitude, time, photos);
    			checkins.add(checkin);
    		}
    		
    		con.close();
    	} catch (ClassNotFoundException e) {
    		e.printStackTrace();
    	} catch (SQLException sql) {
    		sql.printStackTrace();
    	}
    }

}
