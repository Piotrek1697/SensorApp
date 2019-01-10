package com.example.piotrjanus.sensorapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;

import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    private final static String TAG = "MainActivity";

    private Button button;
    private TextView sensorXtextView;
    private TextView sensorYtextView;
    private TextView sensorZtextView;
    private TextView stepsTextView;
    private SensorManager sensorManager;
    private boolean isRunning = false;
    private Sensor accelerometer;
    private Sensor stepCounter;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private LineGraph lineGraph;
    private GraphicalView chartView;

    private ArrayList<PointF> graphListX = new ArrayList<>();
    private ArrayList<PointF> graphListY = new ArrayList<>();
    private ArrayList<PointF> graphListZ = new ArrayList<>();

    private int iterator = 0;
    private double max = 0;
    private int steps = 0;
    private String buttonText;
    private boolean isChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

        button = findViewById(R.id.button2);
        sensorXtextView = findViewById(R.id.sensorXtextView);
        sensorYtextView = findViewById(R.id.sensorYtextView);
        sensorZtextView = findViewById(R.id.sensorZtextView);
        stepsTextView = findViewById(R.id.steps);
        stepsTextView.setText(String.valueOf(0));




        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:wakeLock");

        LinearLayout chart = findViewById(R.id.lv);
        lineGraph = new LineGraph("X series", "Y series", "Z series");
        chartView = lineGraph.getChartView(this);
        chart.addView(chartView);

        if (savedInstanceState != null){
            steps = savedInstanceState.getInt("steps");
            stepsTextView.setText(String.valueOf(steps));
            isRunning = savedInstanceState.getBoolean("isRunning");
            buttonText = savedInstanceState.getString("buttonText");
            button.setText(buttonText);

            isChart = savedInstanceState.getBoolean("isChart");
            if (isChart) {
                graphListX = savedInstanceState.getParcelableArrayList("graphListX");
                graphListY = savedInstanceState.getParcelableArrayList("graphListY");
                graphListZ = savedInstanceState.getParcelableArrayList("graphListZ");

                lineGraph.addAllDataX(graphListX);
                lineGraph.addAllDataY(graphListY);
                lineGraph.addAllDataZ(graphListZ);
                chartView.repaint();
            }

        }

    }

    public void onClick(View view) {
        Log.i(TAG, "Test");
        isRunning = !isRunning;

        if (isRunning) {
            wakeLock.acquire();
            graphListX.clear();
            graphListY.clear();
            graphListZ.clear();
            buttonText = getString(R.string.stop);
            button.setText(buttonText);

            lineGraph.clearData();
            isChart = false;
            chartView.repaint();
        } else {
            if (wakeLock.isHeld()) {
                wakeLock.release();
                super.onDestroy();
            }

            chartView.clearFocus();

            buttonText = getString(R.string.start);
            button.setText(buttonText);

            isChart = true;

            lineGraph.addAllDataX(graphListX);
            lineGraph.addAllDataY(graphListY);
            lineGraph.addAllDataZ(graphListZ);

            WriteToFile.saveToTxt("graphX.txt", graphListX);
            WriteToFile.saveToTxt("graphY.txt", graphListY);
            WriteToFile.saveToTxt("graphZ.txt", graphListZ);

            chartView.repaint();

        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (isRunning) {
            int sensorType = event.sensor.getType();

            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                iterator++;

                float ax = event.values[0]; //skladowa x wektora przyspieszenia
                float ay = event.values[1];
                float az = event.values[2];
                long timeStamp = event.timestamp; //Czas w nanosekundach
                long timeInMillis = timeStamp / 1000000;

                sensorXtextView.setText(String.valueOf(ax));
                sensorYtextView.setText(String.valueOf(ay));
                sensorZtextView.setText(String.valueOf(az));


                graphListX.add(new PointF(iterator, ax));
                graphListY.add(new PointF(iterator, Math.abs(ay)));
                graphListZ.add(new PointF(iterator, az));

               countSteps();

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void countSteps(){
        //Tylko czujnik y

        double[] table = new double[7];

        if (graphListY.size()%7 == 0 && graphListY.size() > 0) {
            for (int i = graphListY.size() - 7; i < graphListY.size()-1 ; i++){
                table[i - (graphListY.size() - 7)] = graphListY.get(i).y;
            }
        }

        if (table[3] > 5 && table[3] < 50) {
            if ((table[3] >= 9.81 && table[3] <= 9.6)) {
                Log.i(TAG, "No movement");
                max = 10;
            }else {
                max = table[3];
                if (max > 10) {

                    int tableSize = table.length;
                    boolean[] isLowerLeft = new boolean[3];
                    boolean[] isLowerRight = new boolean[3];
                    boolean correct = false;

                    for (int i = 0; i <= 2; i++){
                        isLowerLeft[i] = table[i] < max;
                    }

                    for (int i = 4; i < tableSize; i++){
                        isLowerRight[i - 4] = table[i] < max;
                    }

                    int i;
                    for (i = 0; i < isLowerRight.length; i++){
                        if (isLowerLeft[i] && isLowerRight[i]){
                            correct = true;
                        }else {
                            correct = false;
                            break;
                        }
                    }

                    if (correct){
                        steps++;
                        stepsTextView.setText(String.valueOf(steps));
                    }

                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("steps",steps);
        outState.putBoolean("isRunning",isRunning);
        outState.putString("buttonText",buttonText);
        outState.putBoolean("isChart",isChart);

        outState.putParcelableArrayList("graphListX",graphListX);
        outState.putParcelableArrayList("graphListY",graphListY);
        outState.putParcelableArrayList("graphListZ",graphListZ);

    }
}
