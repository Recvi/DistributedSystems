package gr.aueb.cs.ds.worker.reduce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.NetworkListener;
import gr.aueb.cs.ds.worker.Worker;

public class ReduceWorker implements Worker {

    private final int listeningPort;
    private NetworkListener networkListener;

    /*
     * Will have an array/stack/hash-map here,
     * to hold data received from mappers until,
     * the corresponding master asks for them to be reduced.
     * Will use Map<String,ArrayList<String>> for now.
     */
    Map<String,ArrayList<String>> data = new HashMap<String,ArrayList<String>>(); 


    public ReduceWorker(int listeningPort) {
        this.listeningPort = listeningPort;
        
        //Initialize & waitForTasksThread should be called @Main
        initialize();
        waitForTasksThread();
    }
    

    /*
     * Creates a new network object that listens on listeningPort,
     * gives it onNewTask() as "callback" in a java way.
     */
    @Override
    public void initialize() {
        networkListener = new NetworkListener(listeningPort,this);
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
    * Parses objects received and decides whether it is a
    * task from a MapWorker or a Master.
    * Returns onMappedData(data) or onMasterAck(Object data)
    * respectively. 
    */
    @Override
    public Object onNewTask(Object message) {
        Message casted = (Message) message;
        System.out.println("Reducer " + listeningPort + ":I got:" +((Message) message).data);
        
        switch (casted.requestType) {
            case 1:
                return onMappedData(message);
            case 2:
                return onMasterAck(message);
        }
        return null;
    }


    /*
    * Is called by onNewTask when a mapper sends it data.
    * Stores the data.
    * Returns success message.??
    */
    private Object onMappedData(Object data) {
        if (!this.data.containsKey(((Message)data).clientId)) {
            this.data.put(((Message)data).clientId,new ArrayList<String>()); 
        }
        ArrayList<String> list=this.data.get(((Message)data).clientId);
        list.add(((Message)data).data);
        
        return true;
    }


    /*
     * Is called by onNewTask when a Masters asks,
     * for an answer.Does the reduce and returns the answer.
     */
    private Object onMasterAck(Object data) {
        /* ... */
        
        //Temp
        return fakeReduce(((Message)data).clientId);
    }

    private synchronized String fakeReduce(String requestId){
        ArrayList<String> data=this.data.get(requestId);
        String reduced = "[";
        for(int i=0;i<data.size();i++){
            reduced+= data.get(i);
        }
        reduced+= ":Reduced by " + listeningPort +"]";
        this.data.remove(data);
        return reduced;
    }

    private void reduce(int dunno, Object what) {
        /* ... */
    }

    private void getResults(Map<Integer,Object> data) {
        /* ... */
    }

}
