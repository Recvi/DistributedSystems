package gr.aueb.cs.ds;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.io.*;
import java.net.*;

import gr.aueb.cs.ds.dummy.AndroidClient;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.worker.map.MapWorker;
import gr.aueb.cs.ds.worker.reduce.ReduceWorker;

public class TestFile {

    static Address mapperAddress1=new Address("localhost",3330);
    static Address mapperAddress2=new Address("localhost",3331);

    static Address reducerAddress=new Address("localhost",3332);


    public static void main(String[] args) throws InterruptedException {
        makeReducer();
        Thread.sleep(1000);
        makeMapper1();
        Thread.sleep(1000);
        makeMapper2();
        Thread.sleep(1000);
        makeDummy();
    }

    private static void makeDummy(){
        new Thread()
        {
            public void run() {
                ArrayList<Address> mappers = new ArrayList<Address>();
                mappers.add(mapperAddress1);
                mappers.add(mapperAddress2);
                AndroidClient client = new AndroidClient(mappers, reducerAddress);
            }
        }.start();
    }

    private static void makeMapper1(){
        new Thread()
        {
            public void run() {
                MapWorker mapwork = new MapWorker(mapperAddress1.port, reducerAddress);
            }
        }.start();
    }

    private static void makeMapper2(){
        new Thread()
        {
            public void run() {
                MapWorker mapwork = new MapWorker(mapperAddress2.port, reducerAddress);
            }
        }.start();
    }

    private static void makeReducer(){
        new Thread()
        {
            public void run() {
                ReduceWorker mapwork = new ReduceWorker(reducerAddress.port);
            }
        }.start();
    }


    private void testProp(){
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("src/config.properties");
            prop.load(input);

            System.out.println(prop.getProperty("database_ip"));
            System.out.println(prop.getProperty("database_port"));
            System.out.println(prop.getProperty("database_user"));
            System.out.println(prop.getProperty("database_password"));

        } catch (IOException e) {
            System.out.println("Config file not found.");
        }
    }

}
