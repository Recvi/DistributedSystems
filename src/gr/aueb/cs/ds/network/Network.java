package gr.aueb.cs.ds.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import gr.aueb.cs.ds.network.Message.MessageType;

public class Network {

    /*
     * Will be using this static method to send a request
     * and get a reply in a single line.
     */
    public static Object sendRequest(Message message, Address target){
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Object reply = null;
        try {
        	System.out.println("Initializing connection");
            requestSocket = new Socket(InetAddress.getByName(target.getIp()), target.getPort());
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            
            System.out.println("Sending message: " + message);
            out.writeObject(message);
            out.flush();
            System.out.println("Sent message: " +message);
            if (message.getMsgType() != MessageType.ACK &&
            	message.getMsgType() != MessageType.MAPPER_DATA) {
            	System.out.println("Reading message.");
            	reply = in.readObject();
            	System.out.println("Read message: "+reply);
            }
            
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        return reply;
    }
    
    /*
     * Will be using this static method to send a request
     * and get a reply in a single line.
     */
    public static Object sendRequest(Socket con, Message message, Address target){
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Object reply = null;
        try {
            requestSocket = con;
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            
            out.writeObject(message);
            out.flush();
            
            if (message.getMsgType() != MessageType.ACK &&
            	message.getMsgType() != MessageType.MAPPER_DATA) {
            	reply = in.readObject();
            }
            
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        return reply;
    }

}
