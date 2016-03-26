package gr.aueb.cs.ds.network;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 7817450821242935569L;
    public String requestId;

    //Should be ENUM.{0:Master->Mapper, 1:Mapper->Reducer, 2:Master->Reducer} 
    public int requestType;

    //Should be of Object type.
    public String data;


    public Message(String requestId, int requestType, String data) {
        this.requestId = requestId;
        this.requestType = requestType;
        this.data = data;
    }

}
