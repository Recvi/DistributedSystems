package gr.aueb.cs.ds.worker.map;

import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.NetworkListener;
import gr.aueb.cs.ds.worker.Worker;

import java.util.Map;

public class MapWorker implements Worker{

    private final int listeningPort;
    private NetworkListener networkListener;
    
    //Might be replaced with array for multiple reducers
    private Address reducerAddress;    
    
    
    public MapWorker(int listeningPort, Address reducerAddress) {
        this.listeningPort=listeningPort;
        this.reducerAddress=reducerAddress;
        
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
    public Object onNewTask(Object argumentTypesToBeDecided) {
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
    
    
    /*
    * Sends the mapped data to $reducerAddress.
    * Most likely through a new network object or static method.
    */
    private void sendToReducers(Map<Integer,Object> data) {
        /* ... */
    }
    
    /*
    * At current branch this method will not be used.
    * Master gets notified through a reply to his initial request.
    */
    @SuppressWarnings("unused")
    private void notifyMaster() {
        
    }
    
}
