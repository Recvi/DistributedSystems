package gr.aueb.cs.ds.network;

public class Address {

    /*
     * Can use Address objects instead of passing
     * (String ip,int port) as arguments all the time.
     * Especially useful for return types.
    */
    private String ip;
	private int port;

	public Address(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Address(String addr) {
        String[] add = addr.split(':');
        if (add.length == 2) {
            this.ip = add[0];
            this.port = add[1];
        } else {
            this.ip = null;
            this.port = null;
        }
    }

    public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
