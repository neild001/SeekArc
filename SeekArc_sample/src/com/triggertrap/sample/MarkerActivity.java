package com.triggertrap.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.triggertrap.seekarc.SeekArc;

/**
 * Created by aggelos on 30/12/2015.
 */
public class MarkerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        SeekArc markerSeekArc = (SeekArc) findViewById(R.id.markerSeekArc);
        markerSeekArc.setMax(100);
        markerSeekArc.addMarker(null, 20, 0, 0, onMarkerClickListener);
        markerSeekArc.addMarker("label", 70, 0, 0, onMarkerClickListener);
        markerSeekArc.addMarker(null, 40, 0, 0, onMarkerClickListener);
    }

    SeekArc.OnMarkerClickListener onMarkerClickListener = new SeekArc.OnMarkerClickListener() {
        @Override
        public void onMarkerClicked(int value, String label) {
            Toast.makeText(MarkerActivity.this, label + " clicked", Toast.LENGTH_SHORT).show();
        }
    };
}
