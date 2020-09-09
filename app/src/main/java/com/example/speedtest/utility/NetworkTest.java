package com.example.speedtest.utility;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class NetworkTest extends AsyncTask {
    Context context;
    SpeedListener speedListener;
    private final OkHttpClient client = new OkHttpClient();
    private long startTime;
    private long endTime;
    private long fileSize;
    // Bandwidth range in kbps copied from FBConnect Class
    private int POOR_BANDWIDTH = 150;
    private int AVERAGE_BANDWIDTH = 550;
    private int GOOD_BANDWIDTH = 2000;
    private String TAG = "NetworkTest";

    public NetworkTest(Context context, SpeedListener speedListener) {
        this.context = context;
        this.speedListener = speedListener;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        final Network network = new Network();
        if (!Util.isOnline(context)) {
            network.status = 0;
            network.message = "Offline";
            speedListener.onListener(network);
        } else {
            Request request = new Request.Builder()
                    .url("https://i.picsum.photos/id/237/200/300.jpg?hmac=TmmQSbShHz9CdQm0NkEjx1Dyh_Y984R9LpNrpvH2D_U") // replace image url
                    .build();
            startTime = System.currentTimeMillis();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    speedListener.onListener(network);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    InputStream input = response.body().byteStream();

                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];

                        while (input.read(buffer) != -1) {
                            bos.write(buffer);
                        }
                        byte[] docBuffer = bos.toByteArray();
                        fileSize = bos.size();

                    } finally {
                        input.close();
                    }

                    endTime = System.currentTimeMillis();

                    // calculate how long it took by subtracting endtime from starttime

                    final double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
                    final double timeTakenInSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
                    final int kilobytePerSec = (int) Math.round(1024 / timeTakenInSecs);
                    final double speed = Math.round(fileSize / timeTakenMills);

                    Log.d(TAG, "Time taken in secs: " + timeTakenInSecs);
                    Log.d(TAG, "Kb per sec: " + kilobytePerSec);
                    Log.d(TAG, "Download Speed: " + speed);
                    Log.d(TAG, "File size in kb: " + fileSize);
                    network.kbps = kilobytePerSec;
                    if (kilobytePerSec <= POOR_BANDWIDTH) {
                        // slow connection
                        network.status = 1;
                        network.message = "Poor";

                    } else if (kilobytePerSec <= AVERAGE_BANDWIDTH) {
                        // Average connection
                        network.status = 2;
                        network.message = "Average";

                    } else if (kilobytePerSec <= GOOD_BANDWIDTH) {
                        // Fast connection
                        network.status = 3;
                        network.message = "Good";

                    } else {
                        network.status = 4;
                        network.message = "Excellent";
                    }
                    speedListener.onListener(network);
                }
            });
            return null;
        }
        return null;
    }
}
