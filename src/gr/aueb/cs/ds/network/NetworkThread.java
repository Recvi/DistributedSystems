package gr.aueb.cs.ds.network;

import gr.aueb.cs.ds.worker.Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkThread extends Thread{

    private Worker worker;
    private ObjectInputStream in;
    private ObjectOutputStream out;


    public NetworkThread(Socket connection, Worker worker) {
        this.worker = worker;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Reads data from input connections,
     * passes them to worker,waits for answer from worker,
     * replies to connection.
     */
    @Override
    public void run() {
        try {
            Object data = in.readObject();
            Object answer = worker.onNewTask(data);
            out.writeObject(answer);
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
