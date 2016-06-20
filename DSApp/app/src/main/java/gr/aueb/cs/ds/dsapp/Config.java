package gr.aueb.cs.ds.dsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.ArrayList;

import gr.aueb.cs.ds.network.Address;


public class Config {
    private ArrayList<Address> mappers;
    private Address reducer;
    private String[] servers;

    public  Config(Context context) {

        //  Get the servers addresses, make the first reducer and the others mappers.
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String ser = SP.getString("servers", "10.0.2.2:4322,10.0.2.2:4323,10.0.2.2:4324,10.0.2.2:4325");
        servers = ser.split(",");

        reducer = new Address(servers[0]);



        mappers = new ArrayList<Address>();
        for (int i=1; i<servers.length; i++) {
            mappers.add(new Address(servers[i]));
        }

    }

    public Address getReducer() {
        return reducer;
    }

    public ArrayList<Address> getMappers() {
        return mappers;
    }

    public Address getAvailableServer(Address down) {
        for (int i=0; i<servers.length; i++) {
           // if (!servers[i].equals(down.getAddress())) {
            //    return new Address(servers[i]);
            //}
        }
        return null;
    }

}
