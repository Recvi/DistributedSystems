package gr.aueb.cs.ds.worker.map;

import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
import gr.aueb.cs.ds.network.NetworkListener;
import gr.aueb.cs.ds.worker.Worker;

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

        //Get data from database
        /* ... */

        //Map data
        /* ... */

        //Send mapped data to reducer
        String requestId = ((Message) message).requestId;
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

}
