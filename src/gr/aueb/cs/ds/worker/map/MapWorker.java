package gr.aueb.cs.ds.worker.map;

import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Message.MessageType;
import gr.aueb.cs.ds.network.Network;
import gr.aueb.cs.ds.network.NetworkListener;
import gr.aueb.cs.ds.worker.Worker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

public class MapWorker implements Worker{

    private final int listeningPort;
    private NetworkListener networkListener;

    //Might be replaced with array for multiple reducers
    private Address reducerAddress;    


    public MapWorker(int listeningPort, Address reducerAddress) {
        this.listeningPort = listeningPort;
        this.reducerAddress = reducerAddress;

        //Initialize & waitForTasksThread should be called @Main
        initialize();
        waitForTasksThread();
    }


    /*
    * Creates a new network object that listens on listeningPort,
    * gives it onNewTask() as "callback" in a java way.
    */
    public void initialize(){
        networkListener=new NetworkListener(listeningPort,this);
    }


    /*
     * Tells the networkListener to start listening for new tasks
     * and create a new thread for each task it receives. 
     */
    @Override
    public void waitForTasksThread() {
        networkListener.listen();
    }


    /*
    * Is called by network listener when it receives new data.
    * Parses objects received and does task.
    * Task will most likely be:getShitFromDatabase(),map(shit),sendToReducers(shit),notifyMaster();
    * Should most likely break to separate functions.
    */
    public Object onNewTask(Object message) {

        System.out.println("Worker " + listeningPort + ":I got:" +((Message) message).data);
        MapTask task = (MapTask) ((Message) message).data;
        
        //Get data from database
        ArrayList<Checkin> checkins = queryDatabase(task.minLatitude,  task.maxLatitude, task.minLongitude,
        		task.maxLongitude, task.minTime, task.maxTime);

        //Map data
        Map<Integer,Object> mappedData = map(checkins);

        //Send mapped data to reducer
        String requestId = ((Message) message).getClientId();
        sendToReducers(new Message(requestId, MessageType.MAP, mappedData));

        //Notify Master that task was completed.
        return true;
    }


    /*
    * Does the actual mapping.
    * Right now signature is random,to be fixed by someone who knows map.
    */
    private Map<Integer,Object> map(ArrayList<Checkin> checkins) {
        /* ... */
        return null;
    }


    /*
    * Sends the mapped data to $reducerAddress.
    * Most likely through a new network object or static method.
    * Needs to return boolean for Error Handling.
    * Parameter should be of type Map<Integer,Object> instead of Object.
    */
    private void sendToReducers(Object data) {
        Network.sendRequest(data,reducerAddress);
    }


    /*
    * At current branch this method will not be used.
    * Master gets notified through a reply to his initial request.
    */
    @SuppressWarnings("unused")
    private void notifyMaster() {
        
    }
    
    private ArrayList<Checkin> queryDatabase(String minLatitude, String maxLatitude, String minLongitude,
    		String maxLongitude, String minTime, String maxTime) {
    	
    	ArrayList<Checkin> checkins = new ArrayList<Checkin>();
    	
    	String query = "SELECT POI, POI_name, POI_category, latitude, longitude, time, photos"
    			+ "FROM checkins WHERE "
    			+ "latitude >= " + minLatitude + " AND latitude <= " + maxLatitude
    			+ " AND longitude >= " + minLongitude + " AND longitude <= " + maxLongitude
    			+ " AND time >= '" + minTime + "' AND time <= '" + maxTime
    			+ "';";
    	
    	Connection conn = null;
        String dbClass = "com.mysql.jdbc.Driver";
        
        try {
            Class.forName(dbClass);
            conn = DriverManager.getConnection("jdbc:mysql://83.212.117.76/ds_systems_2016?" +
                                           "user=omada13&password=omada13db");
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getString("POI_name"));
                
                checkins.add(new Checkin(rs.getString("POI"), rs.getString("POI_name"),rs.getString("POI_category"),
                		rs.getDouble("latitude"),rs.getDouble("longitude"),rs.getString("time"),
                		rs.getString("photos")));
            }
            conn.close();
        
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
		return checkins;
    	
    }

}
