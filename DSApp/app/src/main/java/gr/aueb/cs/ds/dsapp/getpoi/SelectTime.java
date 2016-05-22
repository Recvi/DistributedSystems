package gr.aueb.cs.ds.dsapp.getpoi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import gr.aueb.cs.ds.dsapp.R;

public class SelectTime extends Activity {

    static final int DATE_DIALOG_L = 0;
    static final int DATE_DIALOG_H = 1;

    private static int lYear;
    private static int lMonth;
    private static int lDay;
    private static int hYear;
    private static int hMonth;
    private static int hDay;
    private Button lPickDate;
    private Button hPickDate;

    LatLng point1;
    LatLng point2;
    private ArrayList<Button> buttons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time);

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        point1 = (LatLng) bundle.get("Point1");
        point2 = (LatLng) bundle.get("Point2");

        setUpButtons();

        lPickDate = (Button) findViewById(R.id.pick_low_date);
        lPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_L);
            }
        });
        hPickDate = (Button) findViewById(R.id.pick_high_date);
        hPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_H);
            }
        });


        // Set Defaults
        lYear = 2001;
        lMonth = 2;
        lDay = 2;
        hYear = 2101;
        hMonth = 2;
        hDay = 2;

        updateDisplay();
    }

    private void setUpButtons() {
        buttons = new ArrayList<Button>();

        Button next = (Button) findViewById(R.id.next_button2);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextActivity();
            }
        });

        Button prev = (Button) findViewById(R.id.cancel_button2);
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousActivity();
            }
        });

        buttons.add(next);
        buttons.add(prev);
    }

    private void nextActivity() {
        Intent intent = new Intent(this, DisplayResults.class);
        intent.putExtra("Point1", point1);
        intent.putExtra("Point2", point2);
        startActivity(intent);
        startActivity(intent);
    }

    private void previousActivity() {
        finish();
    }

    private DatePickerDialog.OnDateSetListener lDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    lYear = year;
                    lMonth = monthOfYear;
                    lDay = dayOfMonth;
                    updateDisplay();
                }
            };

    private DatePickerDialog.OnDateSetListener hDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    hYear = year;
                    hMonth = monthOfYear;
                    hDay = dayOfMonth;
                    updateDisplay();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_L:
                return new DatePickerDialog(this,
                        lDateSetListener,
                        lYear, lMonth, lDay);
            case DATE_DIALOG_H:
                return new DatePickerDialog(this,
                        hDateSetListener,
                        hYear, hMonth, hDay);
        }
        return null;
    }

    private void updateDisplay() {
        this.lPickDate.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(lDay).append("-")
                        .append(lMonth + 1).append("-")
                        .append(lYear).append(" "));
        this.hPickDate.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(hDay).append("-")
                        .append(hMonth + 1).append("-")
                        .append(hYear).append(" "));
    }
}
