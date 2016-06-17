package gr.aueb.cs.ds.worker.database;

import gr.aueb.cs.ds.ConfigReader;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
import gr.aueb.cs.ds.network.NetworkHandler;
import gr.aueb.cs.ds.network.Message.MessageType;
import gr.aueb.cs.ds.worker.map.Checkin;

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

public class Insert extends Thread {

	private NetworkHandler net;
    private Message msg;
    private ConfigReader conf;

    public Insert (NetworkHandler net, Message msg, ConfigReader conf) {

    	this.net = net;
    	this.msg = msg;
    	this.conf = conf;

    	net.close();
    }

    public void run() {

    	ArrayList<String> msgData = (ArrayList<String>)msg.getData();
    	postNewCheckin(msgData);
    }
    
    private void postNewCheckin(ArrayList<String> msgData) {
    	/*
    	 * Connect to db and get the data assigned to me.
    	 * jdbc:mysql://HOST:PORT/DB_NAME?user=USERNAME&password=YOURPASS
    	 */
    	Address db = conf.getDb();
    	String dbURL = "jdbc:mysql://" + db.getIp() + ":" + db.getPort() + "/"
    			+ conf.getDb_name() + "?user=" + conf.getDb_user() + "&password="
    			+ conf.getDb_pass();
    	String dbClass = "com.mysql.jdbc.Driver";
    	
    	
    	String query = "Insert into checkins (user, POI, POI_name, POI_category, POI_category_id, latitude, longitude, time, photos)"
    			+ "Select 6666, POI, POI_name, POI_category, POI_category_id, latitude, longitude, '"
    			+ msgData.get(2) + "' , '" + msgData.get(3) + "' From checkins "
    			+ "Where latitude = " + msgData.get(0) + " AND longitude = " + msgData.get(1) + " limit 1;";
    	
    	/*
    	 * Execute Query
    	 */
    	try {
    		Class.forName(dbClass);
    		Connection con = DriverManager.getConnection(dbURL);
    		Statement stm = con.createStatement();
    		stm.executeUpdate(query);  
    		con.close();
    	} catch (ClassNotFoundException e) {
    		e.printStackTrace();
    	} catch (SQLException sql) {
    		sql.printStackTrace();
    	}
    }

}