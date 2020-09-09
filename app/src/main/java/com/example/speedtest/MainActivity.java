package com.example.speedtest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedtest.utility.Network;
import com.example.speedtest.utility.NetworkTest;
import com.example.speedtest.utility.SpeedListener;

public class MainActivity extends AppCompatActivity {

    Button btCheck;
    TextView tvkb, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvkb = findViewById(R.id.tvkb);
        btCheck = findViewById(R.id.btCheck);

        tvStatus = findViewById(R.id.tvStatus);
        btCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSpeed();
            }
        });
    }

    public void checkSpeed() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        new NetworkTest(this, new SpeedListener() {
            @Override
            public void onListener(final Network network) {
                Log.d("onResponse", " " + network.status + ", " + network.message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvkb.setText(network.kbps + " kb");
                        tvStatus.setText(network.message + "");
                        findViewById(R.id.progress).setVisibility(View.GONE);
                    }
                });
            }
        }).execute();
    }
}