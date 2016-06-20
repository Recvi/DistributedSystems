package gr.aueb.cs.ds.dsapp.getpoi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
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

        this.conf = new Config(getBaseContext());
        this.mappersData = new HashMap<Address, ArrayList<String>>();
        this.mapperAddresses = conf.getMappers();
        this.pendingRequests = mapperAddresses.size();
        this.clientId = UUID.randomUUID().toString();

        Button next = (Button) findViewById(R.id.go_back2);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        LatLng point1 = (LatLng) bundle.get("Point1");
        LatLng point2 = (LatLng) bundle.get("Point2");
        datetimeStart = (String) bundle.get("DateStart") +  "00:00:00";
        datetimeEnd = (String) bundle.get("DateEnd") + "00:00:00";

        llpoint = new String[2];
        trpoint = new String[2];

        if (point1.latitude < point2.latitude) {
            llpoint[0] = Double.toString(point1.latitude);
        } else {
            llpoint[0] = Double.toString(point2.latitude);
        }
        if (point1.longitude < point2.longitude) {
            llpoint[1] = Double.toString(point1.longitude);
        } else {
            llpoint[1] = Double.toString(point2.longitude);
        }

        if (point1.latitude > point2.latitude) {
            trpoint[0] = Double.toString(point1.latitude);
        } else {
            trpoint[0] = Double.toString(point2.latitude);
        }
        if (point1.longitude > point2.longitude) {
            trpoint[1] = Double.toString(point1.longitude);
        } else {
            trpoint[1] = Double.toString(point2.longitude);
        }

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map2)).getMap();
        MapsInitializer.initialize(this);
        googleMap.setOnInfoWindowClickListener(this);
        onMapReady(googleMap);

        new getPois().execute("", "", "");
    }

    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        LatLng newYork = new LatLng(40.7127784,-74.0409606);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10));
    }

    public void onInfoWindowClick(Marker marker) {

        for (int i = 0; i < checkins.size(); i++) {

            if (checkins.get(i).getPOI_name().equals(marker.getTitle())) {
                System.out.println(checkins.get(i).getPOI_category());
                System.out.println(checkins.get(i).getPhotos());
                System.out.println(imageSet.get(marker.getTitle()));
                Intent intent = new Intent(this, DisplayPOI.class);
                intent.putExtra("Checkin", checkins.get(i));

                intent.putExtra("Photos", imageSet.get(marker.getTitle()).toArray());
                startActivity(intent);
                break;
            }
        }

    }

    ArrayList<Checkin> checkins = new ArrayList<Checkin>();
    Map<String,Set<String>> imageSet = new HashMap<String,Set<String>>();

    private class getPois extends AsyncTask<String, String, String> {

       // ArrayList<Checkin> checkins = new ArrayList<Checkin>();
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
            findViewById(R.id.progress_bar1).setVisibility(View.GONE);
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
                data.add(conf.getReducer());

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
                checkins.add(d.getKey());
                imageSet.put(d.getKey().getPOI_name(), d.getValue());
            }
            done = true;

        }
    }

}
