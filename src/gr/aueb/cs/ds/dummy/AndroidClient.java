package gr.aueb.cs.ds.dummy;

import java.util.ArrayList;

import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.Network;
import java.util.UUID;
import static java.lang.Math.random;

public class AndroidClient {

    private ArrayList<Address> mapAddresses;
    private Address reducerAddress;
    private String requestId;

    //Number of mappers that have not respond yet.
    private int pendingRequest;

    public AndroidClient(ArrayList<Address> mapAddresses, Address reducerAddress) {
        this.mapAddresses = mapAddresses;
        this.reducerAddress = reducerAddress;
        pendingRequest = mapAddresses.size();
        distributeToMappers();
    }

    private void distributeToMappers(){
        requestId = UUID.randomUUID().toString();
        System.out.println("Master:I am distributing to mappers with requestId:"+requestId);
        for(int i=0;i<mapAddresses.size();i++){
            new Thread()
            {
                public void run() {
                    sendToMapper();
                }
            }.start();
        }
    }

    private void sendToMapper(){
        Message message=new Message(requestId,0,"Area:"+random());
        Network.sendRequest(message, getNextMapper());
        waitForMappers();
    }

    private synchronized Address getNextMapper(){
        return mapAddresses.remove(0);
    }

    private synchronized void waitForMappers(){
        pendingRequest--;
        if (pendingRequest<1){
            ackToReducer();
        }
    }

    private void ackToReducer(){
        Message message=new Message(requestId,2,"Reduce Now!");
        System.out.println("Master:I received from reducer:"+(String)Network.sendRequest(message, reducerAddress));
    }
    
}
