package gr.aueb.cs.ds.network;

import java.io.Serializable;


public class Message implements Serializable {
	
    public enum MessageType {
    	MAP,           // Start Mapping
    	REDUCE,        // Start Reducing
    	MAPPER_DATA,   // Mapper sends data to Reducer
    	ACK,           // Acknowledgement: Done or everything okay
    	ERROR,          // Something went wrong
		INSERT
	}

	private static final long serialVersionUID = 7817450821242935569L;
    private String clientId;

    /*
     * Specifies the type of the Message.
     *  MAP: Client->Mapper
     *  REDUCE: Client->Reducer
     *  MAPPER_DATA: Mapper->Reducer 
     */
    private MessageType msgType;
    

    /*
     * An Object that gets typecasted to whichever the circumstance.
     */
    public Object data;
    
    public Message(String clientId, MessageType mt, Object data) {
        this.clientId = clientId;
        this.data = data;
        this.msgType = mt;
    }


    public MessageType getMsgType() {
		return msgType;
	}


	public void setMsgType(MessageType mt) {
		this.msgType = mt;
	}


	public String getClientId() {
		return clientId;
	}


	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}

}
