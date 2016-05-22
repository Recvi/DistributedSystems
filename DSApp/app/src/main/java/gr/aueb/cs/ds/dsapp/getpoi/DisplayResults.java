package gr.aueb.cs.ds.dsapp.getpoi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import gr.aueb.cs.ds.dsapp.Config;
import gr.aueb.cs.ds.dsapp.R;
import gr.aueb.cs.ds.network.Address;
import gr.aueb.cs.ds.network.Message;
import gr.aueb.cs.ds.network.NetworkHandler;
import gr.aueb.cs.ds.worker.map.Checkin;

public class DisplayResults extends Activity implements GoogleMap.OnInfoWindowClickListener{

    private GoogleMap googleMap;

    String[] llpoint;
    String[] trpoint;
    String datetimeStart;
    String datetimeEnd;

    private Map<Address, ArrayList<String>> mappersData;
    private ArrayList<Address> mapperAddresses;
    private int pendingRequests;
    private String clientId;

    private Config conf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_results);

        this.conf = new Config();
        this.mappersData = new HashMap<Address, ArrayList<String>>();
        this.mapperAddresses = conf.getMappers();
        this.pendingRequests = mapperAddresses.size();
        this.clientId = UUID.randomUUID().toString();

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        LatLng point1 = (LatLng) bundle.get("Point1");
        LatLng point2 = (LatLng) bundle.get("Point2");

        llpoint = "40.55,-75.0".split(",");
        trpoint =  "40.99,-73.0".split(",");
        datetimeStart = "0000-01-01 00:00:00";
        datetimeEnd = "2022-01-01 00:00:00";

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map2)).getMap();
        MapsInitializer.initialize(this);
        googleMap.setOnInfoWindowClickListener(this);
        //onMapLoaded();
        onMapReady(googleMap);


        new getPois().execute("", "", "");
    }

    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.55, -75.00), 10));
    }

    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
        System.out.println("yolo");
    }


    private class getPois extends AsyncTask<String, String, String> {

        ArrayList<Checkin> checkins = new ArrayList<Checkin>();
        private boolean done = false;

        protected String doInBackground(String... strings) {
            distributeToMappers(llpoint, trpoint, datetimeStart, datetimeEnd);
            while (!done) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "String";
        }

        protected void onPostExecute(String result) {
            for (int i=0; i<checkins.size(); i++) {
                LatLng pos = new LatLng(checkins.get(i).getLatitude(), checkins.get(i).getLongitude());
                googleMap.addMarker(new MarkerOptions().position(pos).title(checkins.get(i).getPOI_name()));
            }
            System.out.println("sdfgdfgdfg3");
        }


        private void distributeToMappers(String[] llpoint, String[] trpoint, String datetimeStart, String datetimeEnd) {

            double lowerLeftLat = Double.parseDouble(llpoint[0]);
            double topRightLat = Double.parseDouble(trpoint[0]);
            double latDiff = topRightLat - lowerLeftLat;
            double latStep = latDiff / mapperAddresses.size();

            topRightLat = lowerLeftLat + latStep;   // reinitialize for first mapper.

            for (Address addr : mapperAddresses) {
                ArrayList<String> data = new ArrayList<String>();
                data.add(Double.toString(lowerLeftLat));
                data.add(llpoint[1]);
                data.add(Double.toString(topRightLat));
                data.add(trpoint[1]);
                data.add(datetimeStart);
                data.add(datetimeEnd);

                lowerLeftLat += latStep;
                topRightLat += latStep;

                mappersData.put(addr, data);
            }

            System.out.println("Distributing to mappers with clientId: " + clientId);

            int mappers_num = mapperAddresses.size();
            for (int i=0; i<mappers_num; i++) {
                new Thread() {
                    public void run() {
                        Address mapperAddress = getNextMapperAddress();
                        Message msg = new Message(clientId, Message.MessageType.MAP, mappersData.get(mapperAddress));
                        NetworkHandler net = new NetworkHandler(mapperAddress);
                        net.sendMessage(msg);
                        Message reply = net.readMessage();
                        net.close();
                        waitForMappers(mapperAddress);
                    }
                }.start();
            }
        }

        private synchronized Address getNextMapperAddress() {
            Address addr = mapperAddresses.remove(0);
            System.out.println("\tClient thread: Sending to " + addr.getIp() + ":" + addr.getPort());
            return addr;
        }

        private synchronized void waitForMappers(Address addr) {
            pendingRequests--;
            System.out.println("\tClient thread: Mapper at " + addr.getIp() + ":" + addr.getPort()+ " is DONE.");
            if (pendingRequests < 1){
                ackToReducer();
            }
        }

        private void ackToReducer(){
            Address reducerAddr = conf.getReducer();
            System.out.println("Notifying Reducer at " + reducerAddr.getIp() + ":" + reducerAddr.getPort());
            Message message = new Message(clientId, Message.MessageType.REDUCE, new String("You can reduce!"));
            NetworkHandler net = new NetworkHandler(conf.getReducer());
            net.sendMessage(message);
            Message reply = net.readMessage();
            net.close();
            collectDataFromReducer((Map<Checkin,Set<String>>)reply.getData());
        }

        private void collectDataFromReducer(Map<Checkin,Set<String>> data) {
            Address reducerAddr = conf.getReducer();
            System.out.println("Got data from Reducer at " + reducerAddr.getIp() + ":" + reducerAddr.getPort());
            for(Map.Entry<Checkin,Set<String>> d : data.entrySet()) {

                System.out.println("POI: "+d.getKey().getPOI()+", counter: " + d.getKey().getPhotos()+", photos: "+d.getValue().size()+
                        ", POI_name: "+d.getKey().getPOI_name()+", POI_category: "+d.getKey().getPOI_category());

                checkins.add(d.getKey());
                LatLng pos = new LatLng(d.getKey().getLatitude(), d.getKey().getLongitude());

            }
            done = true;
        }
    }
}
