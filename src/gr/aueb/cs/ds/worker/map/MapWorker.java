package gr.aueb.cs.ds.worker.map;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
import gr.aueb.cs.ds.network.NetworkListener;
import gr.aueb.cs.ds.worker.Worker;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MapWorker extends Thread {

    private Socket con;
    private Message msg;
    private ConfigReader conf;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private ArrayList<Checkin> checkins;
	
    
    public MapWorker (Socket con, Message msg, ConfigReader conf) {
    	
    	this.con = con;
    	this.msg = msg;
    	this.conf = conf;
    	try {
			this.out = new ObjectOutputStream(con.getOutputStream());
			this.in = new ObjectInputStream(con.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	checkins = new ArrayList<Checkin>();
    }

    public void run() {
    	
    	/*
    	 * Connect to db and get the data assigned to me.
    	 */
    	Address db = conf.getDb();
    	String dbURL = "jdbc:mysql://" + db.getIp() + ":" + db.getPort() + "/"
    			+ conf.getDb_name() + "?user=" + conf.getDb_user() + "&password="
    			+ conf.getDb_pass();
    	String dbClass = "com.mysql.jdbc.Driver";
    	
    	/*
    	 * Reading the data in the same order we sent them.
    	 * (could send a Map<"property_name", value> instead to make it readable)
    	 */
    	ArrayList<String> data = (ArrayList<String>)msg.getData();
    	String query = "SELECT POI, POI_name, POI_category, latitude, longitude, time, photos"
    			+ "FROM checkins WHERE "
    			+ "latitude >= " + data.get(0) + " AND latitude <= " + data.get(2)
    			+ " AND longitude >= " + data.get(1) + " AND longitude <= " + data.get(3)
    			+ " AND time >= '" + data.get(4) + "' AND time <= '" + data.get(5)
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
    	
    	
    	/*
    	 * Get the number of working cpu cores.
    	 */
    	int cores = Runtime.getRuntime().availableProcessors();
    	
    	/*
    	 * Split data to fit cores.
    	 */
    	double longitudeMin = Double.parseDouble(data.get(1));
    	double longitudeMax = Double.parseDouble(data.get(3));
    	double lonStep = (longitudeMax - longitudeMin) / cores;
    	ConcurrentMap<Object, List<Checkin>> splitCheckins = checkins.parallelStream().collect(Collectors.groupingByConcurrent(
    			c -> Math.ceil((c.getLongitude() - longitudeMin) / lonStep)
    			));
    	

    	/*
    	 * Map in parallel
    	 */
    	splitCheckins.values().parallelStream().forEach(p -> p.stream().collect(Collectors.groupingBy(c -> c.getPOI(), counting())));
//    	splitCheckins.values().stream().forEach(p -> p.stream().count());
    	// Input: ConcurrentMap<Double cpu_group, List<Checkin> checkins>
    	// Output: Map< Checkin , ArrayList<String>>
    	
    	/*
    	 * Send intermediate data to reducer.
    	 */
    	
    	/*
    	 * Notify client that the work is completed.
    	 */
    	
    }


   

    /*
    * Is called by network listener when it receives new data.
    * Parses objects received and does task.
    * Task will most likely be:getShitFromDatabase(),map(shit),sendToReducers(shit),notifyMaster();
    * Should most likely break to separate functions.
    */
    public Object onNewTask(Object message) {

        System.out.println("Worker " + listeningPort + ":I got:" +((Message) message).data);

        //Get data from database
        /* ... */

        //Map data
        /* ... */

        //Send mapped data to reducer
        String requestId = ((Message) message).clientId;
        int requestType = 1;
        String data = "[" + ((Message) message).data + ":Mapped by " + listeningPort + "]";
        sendToReducers(new Message(requestId, requestType, data));

        //Notify Master that task was completed.
        return true;
    }


    /*
    * Does the actual mapping.
    * Right now signature is random,to be fixed by someone who knows map.
    */
    private Map<Integer,Object> map(Object object1, Object object2) {
        /* ... */
        return null;
    }


    /*
    * Sends the mapped data to $reducerAddress.
    * Most likely through a new network object or static method.
    * Needs to return boolean for Error Handling.
    * Parameter should be of type Map<Integer,Object> instead of Object.
    */
    private void sendToReducer(Object data) {
        Network.sendRequest(data,reducerAddress);
    }


    /*
    * At current branch this method will not be used.
    * Master gets notified through a reply to his initial request.
    */
    @SuppressWarnings("unused")
    private void notifyMaster() {
        
    }

}
