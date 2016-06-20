package gr.aueb.cs.ds.dsapp.getpoi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    private ArrayList<ArrayList<String>> mappersData;
   // private ArrayList<Address> mapperAddresses;
    private int pendingRequests;
    private int parts;
    private String clientId;
    Thread[] threads;
    private Config conf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_results);

        this.conf = new Config(getBaseContext());
        this.parts = conf.getParts();

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
        Address reducer;

        protected String doInBackground(String... strings) {
            boolean repeat = true;
            while(repeat){
                repeat = !distributeToMappers(llpoint, trpoint, datetimeStart, datetimeEnd, parts);
                for (int i = 0; i<parts; i++) {
                    if (threads[i] != null){
                        threads[i].interrupt();
                    }
                }
                if (outOfAddresses) {
                    Log.d("ERROR69", "bgika");
                    return "oufOfAddresses";
                }
            }
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
            if (result.equals("oufOfAddresses")) {
                new AlertDialog.Builder(DisplayResults.this)
                        .setTitle("Out Of Addresses")
                        .setMessage("Not enough available servers, to serve your request. Check servers, settings and try again.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                finish();
                            }
                        })
                        .show();
                //System.exit(0);
            }
            for (int i=0; i<checkins.size(); i++) {
                LatLng pos = new LatLng(checkins.get(i).getLatitude(), checkins.get(i).getLongitude());
                googleMap.addMarker(new MarkerOptions().position(pos).title(checkins.get(i).getPOI_name()));
            }
            findViewById(R.id.progress_bar1).setVisibility(View.GONE);
        }

        boolean restartdistributeToMappers = false;
        boolean outOfAddresses = false;
        private boolean distributeToMappers(String[] llpoint, String[] trpoint, String datetimeStart, String datetimeEnd, int parts) {
            Log.d("ERROR69", "mpika");
            double lowerLeftLat = Double.parseDouble(llpoint[0]);
            double topRightLat = Double.parseDouble(trpoint[0]);
            double latDiff = topRightLat - lowerLeftLat;
            double latStep = latDiff / parts;

            topRightLat = lowerLeftLat + latStep;   // reinitialize for first mapper.

            clientId = UUID.randomUUID().toString();
            mappersData = new ArrayList<ArrayList<String>>();
            pendingRequests = parts;
            conf.resetUsedServers();
            reducer = conf.getServer();
            Log.d("ERROR69","reducer" + reducer.toString());

            if (reducer == null) {
                outofAddresses();
            }
            for (int i = 0; i < parts; i++) {
                ArrayList<String> data = new ArrayList<String>();
                data.add(Double.toString(lowerLeftLat));
                data.add(llpoint[1]);
                data.add(Double.toString(topRightLat));
                data.add(trpoint[1]);
                data.add(datetimeStart);
                data.add(datetimeEnd);
                data.add(reducer.toString());

                lowerLeftLat += latStep;
                topRightLat += latStep;

                mappersData.add(data);
            }

            System.out.println("Distributing to mappers with clientId: " + clientId);

            int mappers_num = parts;
            threads = new Thread[parts];
            for (int i=0; i<mappers_num; i++) {
                threads[i] = new Thread() {
                    public void run() {
                        boolean repeat = true;
                        ArrayList<String> data = getNextData();
                        while (repeat) {
                            repeat = false;
                            Log.d("ERROR69","v2");
                            Address mapperAddress = conf.getServer();
//                            Log.d("ERROR69", mapperAddress.toString());
                            if (mapperAddress == null){
                                Log.d("ERROR69","asss2sd");
                                outofAddresses();
                                break;
                            }
                            try {
                                Message msg = new Message(clientId, Message.MessageType.MAP, data);
                                NetworkHandler net = new NetworkHandler(mapperAddress);
                                net.sendMessage(msg);
                                Message reply = net.readMessage();
                                net.close();
                                if (reply.getMsgType() == Message.MessageType.ERROR) {
                                    conf.removeServerFromOnline(new Address(data.get(6)));
                                    restartdistributeToMappers = true;
                                    return;
                                }
                                waitForMappers(mapperAddress);
                            } catch (Exception dealWithIt) {
                                Log.d("ERROR69","ff");
                                dealWithIt.printStackTrace();
                                repeat = true;
                                conf.removeServerFromOnline(mapperAddress);
                                messageMainThread("Lost mapper");
                            }
                        }

                    }
                };
                threads[i].start();
            }
            for (int i = 0 ; i < parts; i++){
                try {
                    threads[i].join();
                    if (restartdistributeToMappers) {
                        return false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        private synchronized Address getNextMapperAddress() {
           // Address addr = mapperAddresses.remove(0);
           // System.out.println("\tClient thread: Sending to " + addr.getIp() + ":" + addr.getPort());
            return null;
        }

        private synchronized ArrayList<String> getNextData() {
            return mappersData.remove(0);
        }
        private synchronized void waitForMappers(Address addr) {
            pendingRequests--;
            System.out.println("\tClient thread: Mapper at " + addr.getIp() + ":" + addr.getPort()+ " is DONE.");
            if (pendingRequests < 1){
                ackToReducer();
            }
        }

        private void ackToReducer(){
            Address reducerAddr = reducer;
            System.out.println("Notifying Reducer at " + reducerAddr.getIp() + ":" + reducerAddr.getPort());
            Message message = new Message(clientId, Message.MessageType.REDUCE, new String("You can reduce!"));
            NetworkHandler net = new NetworkHandler(reducer);
            net.sendMessage(message);
            Message reply = net.readMessage();
            net.close();
            collectDataFromReducer((Map<Checkin,Set<String>>)reply.getData());
        }

        private void collectDataFromReducer(Map<Checkin,Set<String>> data) {
            Address reducerAddr = reducer;
            System.out.println("Got data from Reducer at " + reducerAddr.getIp() + ":" + reducerAddr.getPort());

            for(Map.Entry<Checkin,Set<String>> d : data.entrySet()) {
                checkins.add(d.getKey());
                imageSet.put(d.getKey().getPOI_name(), d.getValue());
            }
            done = true;

        }

        private void messageMainThread(String message) {
            final String msg = message;
            DisplayResults.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DisplayResults.this, msg, Toast.LENGTH_LONG);
                }
            });
        }
        private void outofAddresses() {
            outOfAddresses = true;

        }
    }

}
