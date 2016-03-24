package gr.aueb.cs.ds.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import gr.aueb.cs.ds.worker.Worker;

public class NetworkListener {

    //Number of maximum connections in queue.
    final static int BACKLOG=10;
    
    int port;
    Worker worker;
    ServerSocket listenSocket;
    Socket connection = null;
    
    
    public NetworkListener(int port, Worker worker) {
        this.port=port;
        this.worker=worker;
    }

    /*
     * Listens for new connections,when it gets ones passes
     * it to a new thread and continues listening.
     */
    public void listen(){
        System.out.println("Listening to :"+port);
        try {
            listenSocket = new ServerSocket(port, BACKLOG);
            while (true){            
                connection = listenSocket.accept();
                Thread t = new NetworkThread(connection, worker);
                t.start();
            }
        } catch (IOException e) {
                e.printStackTrace();
        }        
    }
}
