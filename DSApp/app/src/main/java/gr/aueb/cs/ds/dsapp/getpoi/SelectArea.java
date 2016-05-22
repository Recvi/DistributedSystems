package gr.aueb.cs.ds.dsapp.getpoi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

import gr.aueb.cs.ds.dsapp.MainActivity;
import gr.aueb.cs.ds.dsapp.R;

public class SelectArea extends Activity implements GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener{

    private GoogleMap googleMap;
    private Polygon polygon;
    private Marker point1;
    private Marker point2;

    private ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_area);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        MapsInitializer.initialize(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMarkerDragListener(this);
        //onMapLoaded();
        onMapReady(googleMap);

        setUpButtons();
    }

    private void setUpButtons() {
        buttons = new ArrayList<Button>();

        Button next = (Button) findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextActivity();
            }
        });

        Button prev = (Button) findViewById(R.id.cancel_button);
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousActivity();
            }
        });

        buttons.add(next);
        buttons.add(prev);

    }

    private void nextActivity() {
        Intent intent = new Intent(this, SelectTime.class);
        intent.putExtra("Point1", point1.getPosition());
        intent.putExtra("Point2", point2.getPosition());
        startActivity(intent);
    }

    private void previousActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
        System.out.println("yolo");
       /* polygon.remove();

        PolygonOptions options = new PolygonOptions();
        options.add(point1.getPosition(), point2.getPosition());

        options.strokeWidth(10);

        polygon = googleMap.addPolygon(options);*/
    }

    public void onMapReady(GoogleMap googleMap) {
        //mMap = googleMap;

        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        // Add a marker in Sydney and move the camera

        LatLng def1 = new LatLng(37.9825937, 23.741602);
        LatLng def2 = new LatLng(37.9989654, 23.7261526);


        point1 = googleMap.addMarker(
                new MarkerOptions().position(def1).title("Point 1").flat(true).draggable(true));
        point2 = googleMap.addMarker(
                new MarkerOptions().position(def2).title("Point 2").flat(true).draggable(true));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(def1, 15));

        /*PolygonOptions options = new PolygonOptions();
        options.add(def1, def2);
        options.strokeWidth(10);
        options.fillColor(Color.BLACK);

        polygon = mMap.addPolygon(options);
        */
        highlightArea();
       // googleMap.setOnInfoWindowClickListener(this);
       // googleMap.setOnMarkerDragListener(this);
    }

    private void highlightArea() {
        LatLng corner1 = point1.getPosition();
        LatLng corner2 = new LatLng(point1.getPosition().latitude, point2.getPosition().longitude);
        LatLng corner3 = point2.getPosition();
        LatLng corner4 = new LatLng(point2.getPosition().latitude, point1.getPosition().longitude);

        if (polygon!=null) {
            polygon.remove();
        }
        PolygonOptions options = new PolygonOptions();
        options.add(corner1, corner2, corner3, corner4);
        options.strokeWidth(5);
        options.fillColor(Color.argb(155, 255, 0, 0));

        polygon = googleMap.addPolygon(options);

    }

    public boolean onMarkerClick(Marker arg0) {
        return true;
    }

    public void onMarkerDrag(Marker marker)
    {
        highlightArea();
    }

    public void onMarkerDragStart(Marker marker)
    {

    }

    public void onMarkerDragEnd(Marker marker)
    {
        highlightArea();
        /*System.out.println(marker.getTitle() + " : " + marker.getPosition());

        polygon.remove();

        PolygonOptions options = new PolygonOptions();
        options.add(point1.getPosition(), point2.getPosition());

        options.strokeWidth(10);

        polygon = mMap.addPolygon(options);*/
       /* my_polygon.remove();
        polygon.getPoints().remove(marker.getPosition());  //marker still in polygon list
        polygon.add(marker.getPosition());
        my_polygon=map.addPolygon(polygon); //created new polygon which contains previous marker position
        */
    }

}
