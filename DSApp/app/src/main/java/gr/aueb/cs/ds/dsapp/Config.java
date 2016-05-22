package gr.aueb.cs.ds.dsapp;

import java.util.ArrayList;

import gr.aueb.cs.ds.network.Address;

/**
 * Created by root on 5/22/16.
 */
public class Config {
    private ArrayList<Address> mappers;
    private Address reducer;

    public  Config() {
        mappers = new ArrayList<Address>();
        mappers.add(new Address("10.0.2.2", 4322));
        mappers.add(new Address("10.0.2.2", 4323));
        mappers.add(new Address("10.0.2.2", 4324));
        reducer = new Address("10.0.2.2", 4325);
    }

    public Address getReducer() {
        return reducer;
    }

    public ArrayList<Address> getMappers() {
        return mappers;
    }

}
