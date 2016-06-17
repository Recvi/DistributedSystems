package gr.aueb.cs.ds.dsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.ArrayList;

import gr.aueb.cs.ds.network.Address;


public class Config {
    private ArrayList<Address> mappers;
    private Address reducer;

    public  Config(Context context) {

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String mapper1Address = SP.getString("mapper1", "10.0.2.2:4322");
        String mapper2Address = SP.getString("mapper2", "10.0.2.2:4323");
        String mapper3Address = SP.getString("mapper3", "10.0.2.2:4324");
        String reducerAddress = SP.getString("reducer", "10.0.2.2:4325");

        System.out.println(mapper1Address);
        System.out.println(mapper2Address);
        System.out.println(mapper3Address);
        System.out.println(reducerAddress);

        mappers = new ArrayList<Address>();
        mappers.add(new Address(mapper1Address));
        mappers.add(new Address(mapper2Address));
        mappers.add(new Address(mapper3Address));
        reducer = new Address(reducerAddress);
    }

    public Address getReducer() {
        return reducer;
    }

    public ArrayList<Address> getMappers() {
        return mappers;
    }

}
