package gr.aueb.cs.ds.worker.map;

import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.NetworkListener;
import gr.aueb.cs.ds.worker.Worker;

import java.util.Map;

public class MapWorker implements Worker{

	private final int listeningPort;
	//Might be replaced with array for multiple reducers
	private Address reducerAddress;
	//private Database databaseObject; ??
	private NetworkListener networkListener;
	
	public MapWorker(int listeningPort, Address reducerAddress) {
		this.listeningPort=listeningPort;
		this.reducerAddress=reducerAddress;
		
		initialize();
	}
	
	
	/*
	* Creates a new network object that listens on listeningPort,
	* gives it onNewTask as "callback" in a java way.
	*/
	private void initialize(){
		/* ... */
		
		//Temp
		networkListener=new NetworkListener(listeningPort,this);
		networkListener.listen();
	}
	
	/*
	* Is called by network manager when it receives new data.
	* Parses objects received and does task.
	* Task will most likely be:getShitFromDatabase(),map(shit),sendToReducers(shit),notifyMaster();
	* Could be of type boolean so network manager sends reply "job finished";
	* Should most likely break to separate functions.
	*/
	public boolean onNewTask(Object argumentTypesToBeDecided) {
		/* ... */
		
		//Temp
		System.out.println(listeningPort+":Got a new task.");
		System.out.println(listeningPort+":Getting data from database.");
		System.out.println(listeningPort+":Mapping data.");
		System.out.println(listeningPort+":Sending data to reducer.");
		System.out.println(listeningPort+":Im done.");
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
	
	private void sendToReducers(Map<Integer,Object> data) {
		/* ... */
	}
	
	//Will most likely not need this..
	private void notifyMaster() {
		/* ... */
	}
	
}
