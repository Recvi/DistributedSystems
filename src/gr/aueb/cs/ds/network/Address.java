package gr.aueb.cs.ds.network;

public class Address {
    
    /*
     * Can use Address objects instead of passing
     * (String ip,int port) as arguments all the time.
     * Especially useful for return types.
    */
    
    //Might add set/getters or use as C "struct"
    public String ip;
    public int port;
    
    public Address(String ip, int port) {
        this.ip=ip;
        this.port=port;
    }

}
