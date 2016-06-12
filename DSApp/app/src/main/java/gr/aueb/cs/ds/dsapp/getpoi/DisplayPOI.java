package gr.aueb.cs.ds.dsapp.getpoi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.Arrays;

import gr.aueb.cs.ds.dsapp.R;
import gr.aueb.cs.ds.worker.map.Checkin;

public class DisplayPOI extends Activity {

    Button goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_poi);

        goBack = (Button) findViewById(R.id.ok_button);
        goBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        Checkin point1 = (Checkin) bundle.get("Checkin");
        Object[] photos = (Object[]) bundle.get("Photos");

        TextView title = (TextView) findViewById(R.id.poi_name_placeholder);
        title.setText(point1.getPOI_name());
        new DownloadImageTask().execute(Arrays.copyOf(photos, photos.length, String[].class));
    }

    private void addImage(Bitmap imageUrl){
        TableLayout table = (TableLayout) findViewById(R.id.table);
        TableRow row = new TableRow(this);
        ImageView imageBox = new ImageView(this);
        imageBox.setImageBitmap(imageUrl);
        row.addView(imageBox);
        table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap[]> {

        public DownloadImageTask() {

        }

        protected Bitmap[] doInBackground(String... urls) {
            Bitmap[] images = new Bitmap[urls.length];
            for ( int i = 0; i < urls.length; i++){
                Bitmap mIcon11 = null;
                try {
                    System.out.println(urls[i]);
                    InputStream in = new java.net.URL(urls[i]).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    images[i] = mIcon11;
                } catch (Exception e) {

                }
            }
            return images;
        }

        protected void onPostExecute(Bitmap[] result) {
            for ( int i = 0; i < result.length; i++){
                addImage(result[i]);
            }
        }
    }
}
