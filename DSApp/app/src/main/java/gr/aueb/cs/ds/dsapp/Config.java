package gr.aueb.cs.ds.dsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import gr.aueb.cs.ds.network.Address;


public class Config {

    private Address[] servers;
    private ArrayList<Address> onlineServers;
    private ArrayList<Address> freeServers;

    private ArrayList<Address> mappers;
    private final int maxThreadsPerServer;
    private int threadsPerServerRemaining = 2;
    private int mapParts = 3;

    public  Config(Context context) {

        //  Get the servers addresses, make the first reducer and the others mappers.
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);

        maxThreadsPerServer = Integer.parseInt(SP.getString("maxThreadsPerServer", "1"));
        mapParts = Integer.parseInt(SP.getString("numberOfParts", "3"));

        String ser = SP.getString("servers", "10.0.2.2:4322,10.0.2.2:4323,10.0.2.2:4324,10.0.2.2:4325");
        String[] serversS = ser.split(",");
        servers = new Address[serversS.length];

        for (int i=0; i<serversS.length; i++) {
            servers[i] = new Address(serversS[i]);
        }
        resetOnlineServers();
        resetfreeServers();
    }

    public synchronized Address getServer() {
        if (freeServers.size() > 0) {
            Address address = freeServers.remove(0);
            Log.d("ERROR69","I am config and i give: " + address.toString());
            return address;
        }

        if ((onlineServers.size() > 0) && (threadsPerServerRemaining >= 1)) {
            Log.d("ERROR69","Will have to use servers again.");
            resetfreeServers();
            Address address = freeServers.remove(0);
            Log.d("ERROR69","I am config and i give: " + address.toString());
            return address;
        }
        return null;
    }

    private void resetOnlineServers() {
        onlineServers = new ArrayList<Address>();
        for (int i=0; i<servers.length; i++) {
            onlineServers.add(servers[i]);
        }
    }

    private void resetfreeServers() {
       ;
        threadsPerServerRemaining--;
        freeServers = new ArrayList<Address>();
        for (int i=0; i<onlineServers.size(); i++) {
            freeServers.add(onlineServers.get(i));
        }
    }

    public void resetUsedServers() {
        threadsPerServerRemaining = maxThreadsPerServer;
        resetfreeServers();
    }

    public int getParts(){
        return mapParts;
    }

    public synchronized boolean removeServerFromOnline(Address address) {
        Log.d("ERROR69:", "I am config and i and i will remove this: " + address.toString());
        Log.d("ERROR69:", "I am config and i have onlieServer.size() =  " + onlineServers.size());
        int before= onlineServers.size();
        onlineServers.remove(address);
        int after = onlineServers.size();
        Log.d("ERROR69:", "I am config and i have onlieServer.size() (after removal) = " + onlineServers.size());
        return before != after;
    }

}
