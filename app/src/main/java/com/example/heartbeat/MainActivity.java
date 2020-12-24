package com.example.heartbeat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity {

    /**
     * Measure heart beat with clicks.
     *
     * @author Piotr Dymala p.dymala@gmail.com
     * @version 1.0
     * @since 2020-12-21
     */


    public static final String TAG = "Main Activity";
    private ArrayList<SingleBeat> heartBeats = new ArrayList();
    private Handler handler = new Handler();
    private final Object monitor = new Object();
    private long averageHeatBeat = 0;
    private boolean threadActive = true;
    private int requiredBeats = 10;
    private Thread thread;
    private long start = 0;


    TextView tvClicks;
    Button stopButton;
    Button activateButton;
    TextView textViewBeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvClicks = findViewById(R.id.tvClicks);
        stopButton = findViewById(R.id.stopButton);

        activateButton = findViewById(R.id.activateButton);
        textViewBeat = findViewById(R.id.textViewBeat);
    }

    //reset variables
    private void resetVariables() {
        threadActive = true;
        start = 0;
        heartBeats.clear();
        activateButton.setClickable(true);
        stopButton.setClickable(false);
        tvClicks.setEnabled(false);


        handler.post(() -> tvClicks.setText("Activate and tap to start"));
    }

    public void startCounter() {
        start = System.currentTimeMillis();
    }

    public void activate(View view) {
        tvClicks.setEnabled(true);
        handler.post(() -> {
            tvClicks.setText("Tap to start");
            textViewBeat.setVisibility(View.INVISIBLE);
        });
    }


    public void clickBeat(View v) {
        tvClicks.setEnabled(true);
        synchronized (monitor) {
            if (heartBeats.isEmpty()) {
                startCounter();
                startListening();
                heartBeats.add(new SingleBeat(System.currentTimeMillis() - start));
                int temp = requiredBeats - heartBeats.size();
                tvClicks.setText(Integer.toString(temp) + " clicks needed");
                stopButton.setClickable(true);
                activateButton.setClickable(false);

            } else {
                heartBeats.add(new SingleBeat(System.currentTimeMillis() - start));
                handler.post(() -> {
                    int temp = requiredBeats - heartBeats.size();
                    tvClicks.setText(Integer.toString(temp) + " clicks needed");

                });
                monitor.notifyAll();
            }
        }
    }


    public void startListening() {
        // background thread start
        thread = new Thread(() -> {
            try {
                clickListener();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }

    public void stopListening(View view) {
        // stop background thread -> click listener
        threadActive = false;
        thread.interrupt();
        resetVariables();

    }


    public void clickListener() throws InterruptedException {
//this is background thread
        synchronized (monitor) {

            while (threadActive) {
                while (heartBeats.size() < requiredBeats) {
                    monitor.wait();
                }
                //count average
                averageHeatBeat = 0; // na zas
                for (SingleBeat sb : heartBeats) {
                    averageHeatBeat += sb.getSingleBeatTime();
                }
                averageHeatBeat = averageHeatBeat / heartBeats.size();

                //count dist. to avera

                for (SingleBeat sb : heartBeats) {
                    sb.setAverageBeatTime(averageHeatBeat);
                }
                Log.i(TAG, "clickListener: " + heartBeats.toString());

                //sort
               // heartBeats.sort(new DataComparator());  // API 24

                //BELOW is API 16
                Collections.sort(heartBeats, new Comparator<SingleBeat>() {
                    @Override
                    public int compare(SingleBeat e1, SingleBeat e2) {
                        if(e1.getDistanceToAverage() > e2.getDistanceToAverage()){
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });



                Log.i(TAG, "clickListener: " + heartBeats.toString());

                // del smallest and biggest

                for (int i = 0; i < 4; i++) {
                    heartBeats.remove(heartBeats.size() - 1);

                }

                Log.i(TAG, "clickListener: " + heartBeats.toString());

                // count new averaage
                averageHeatBeat = 0; // reset average
                for (SingleBeat sb : heartBeats) {
                    averageHeatBeat += sb.getSingleBeatTime();
                }
                averageHeatBeat = averageHeatBeat / heartBeats.size();


                handler.post(() -> {

                    textViewBeat.setVisibility(View.VISIBLE);
                    textViewBeat.setText(360000 / averageHeatBeat + " beats/min");

                });
                resetVariables();
                thread.interrupt();
                //reset?
            }
        }

        Log.i(TAG, "clickListener: end");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //thread stop
        thread.interrupt();
        resetVariables();

    }

    public void closeApp(View view) {

        thread.interrupt();
        this.finishAffinity();

    }
}