package gr.aueb.cs.ds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import gr.aueb.cs.ds.network.Address;

public class ConfigReader {
	
	private int k;
	private Address db;
	private String db_user;
	private String db_pass;
	private String db_name;
	private ArrayList<Address> mappers;
	private Address reducer;
	private Address client;
	

	public ConfigReader() {
		
		mappers = new ArrayList<Address>();
		try {
			Properties prop = new Properties();
			String propFileName = "gr/aueb/cs/ds/config.properties";
			
			InputStream inStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			
			if (inStream != null) {
				prop.load(inStream);
			} else {
				throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath.");
			}
			
			/*
			 * Initializing class members from config file.
			 * They are given default values if they are not found.
			 */
			k = Integer.parseInt(prop.getProperty("k", "10"));
			db = new Address(prop.getProperty("database_ip", "83.212.117.76"),
							 Integer.parseInt(prop.getProperty("database_port", "3306")));
			db_user = prop.getProperty("database_user", "omada13");
			db_pass = prop.getProperty("database_password", "omada13db");
			db_name = prop.getProperty("database_name", "ds_systems_2016");
			
			String[] clientProp = prop.getProperty("client").split(":");
			client = new Address(clientProp[0], Integer.parseInt(clientProp[1]));
			
			String[] mappersProp = prop.getProperty("mappers").split(",");
			for (String s : mappersProp) {
				String[] mapper = s.split(":");
				mappers.add(new Address(mapper[0], Integer.parseInt(mapper[1])));
			}
			
			String[] reducerProp = prop.getProperty("reducer").split(":");
			reducer = new Address(reducerProp[0], Integer.parseInt(reducerProp[1]));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public int getK() {
		return k;
	}


	public void setK(int k) {
		this.k = k;
	}


	public Address getDb() {
		return db;
	}


	public void setDb(Address db) {
		this.db = db;
	}


	public String getDb_user() {
		return db_user;
	}


	public void setDb_user(String db_user) {
		this.db_user = db_user;
	}


	public String getDb_pass() {
		return db_pass;
	}


	public void setDb_pass(String db_pass) {
		this.db_pass = db_pass;
	}


	public String getDb_name() {
		return db_name;
	}


	public void setDb_name(String db_name) {
		this.db_name = db_name;
	}


	public ArrayList<Address> getMappers() {
		return mappers;
	}


	public void setMappers(ArrayList<Address> mappers) {
		this.mappers = mappers;
	}


	public Address getReducer() {
		return reducer;
	}


	public void setReducer(Address reducer) {
		this.reducer = reducer;
	}


	public Address getClient() {
		return client;
	}


	public void setClient(Address client) {
		this.client = client;
	}
}
