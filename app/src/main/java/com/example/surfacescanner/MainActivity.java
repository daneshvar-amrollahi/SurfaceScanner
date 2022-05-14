package com.example.surfacescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    Sensor accelerometer;
    Sensor gyroscope;

    static float NS2S;
    static float[] prevAcceleration;
    static float[] velocity;
    static float[] position;
    static float[] linearAcceleration;
    static float[] gravity;
    static long prevTimestampAcc;
    static long prevTimeStampGyro;
    static final float A_THRESHOLD;
    static float[] prevOmega;
    static float[] theta;
    static final float AV_THRESHOLD;

    PointsGraphSeries<DataPoint> xySeries;
    GraphView g;
    private static ArrayList<XYValue> xyValueArray;


    static {
        NS2S = 1.0f / 1000000000.0f;
        prevAcceleration = new float[]{0f, 0f, 0f};
        velocity = new float[]{0f, 0f, 0f};
        position = new float[]{0f, 0f, 0f};
        linearAcceleration = new float[]{0f, 0f, 0f};
        gravity = new float[]{0f, 0f, 0f};
        A_THRESHOLD = 0.125f;
        prevOmega = new float[]{0f, 0f, 0f};
        theta = new float[]{0f, 0f, 0f};
        AV_THRESHOLD = 0.2f; //check later
        xyValueArray = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.start);

        Button finish = findViewById(R.id.finish);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "START PRESSED!");

                Log.d(TAG, "onCreate: Initializing Sensor Services");

                xySeries = new PointsGraphSeries<>();

                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

                sensorManager.registerListener(
                        MainActivity.this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL
                );
                sensorManager.registerListener(
                        MainActivity.this,
                        gyroscope,
                        SensorManager.SENSOR_DELAY_NORMAL
                );

                Log.d(TAG, "onCreate: Registered accelerometer listener");
            }
        });


        finish.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "FINISH PRESSED!");
                        sensorManager.unregisterListener(MainActivity.this, accelerometer);
                        sensorManager.unregisterListener(MainActivity.this, gyroscope);
                        createScatterPlot();
                    }
                }
        );

    }

    public void createScatterPlot()
    {
        Log.d(TAG, "Creating Scatter Plot");

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        g = (GraphView) findViewById(R.id.graph);

        for (int i = 0; i < xyValueArray.size(); i++) {
            double x = xyValueArray.get(i).getX();
            double y = xyValueArray.get(i).getY();
            series.appendData(new DataPoint(x, y), true, 1000);
        }
        g.addSeries(series);
    }


    public void handleGyroscope(SensorEvent sensorEvent) {
        Log.d(TAG, "onGyroscopeChanged:");
        float omegaX = sensorEvent.values[0];
        float omegaY = sensorEvent.values[1];
        float omegaZ = sensorEvent.values[2];
        Log.d(TAG, "--- omega(X, Y, Z) = (" + omegaX + " , " +
                omegaY + ", " +
                omegaZ + ")"
        );
        float dt = (sensorEvent.timestamp - prevTimeStampGyro) * NS2S;
        dt = Math.min(dt, 0.16f);
        float omega = (float)Math.sqrt(omegaX*omegaX + omegaY*omegaY + omegaZ*omegaZ);
        for (int i = 0; i < 3; i++) {
            if (Math.abs(omega) < AV_THRESHOLD)
                continue;
            theta[i] += ((sensorEvent.values[i] + prevOmega[i]) / 2f) * dt;
        }
        System.arraycopy(sensorEvent.values, 0, prevOmega, 0, 3);
        prevTimeStampGyro = sensorEvent.timestamp;
        Log.d(TAG, "--- theta(X, Y, Z) = (" + theta[0] + ", " +
                theta[1] + ", " +
                theta[2] + ")"
        );
    }

    public void handleAccelerometer(SensorEvent sensorEvent) {
        Log.d(TAG, "onAccelerometerChanged:");
        float dt = (sensorEvent.timestamp - prevTimestampAcc) * NS2S;
        dt = Math.min(dt, 0.16f);
        for (int i = 0; i < 3; ++i) {
            if (sensorEvent.values[i] < A_THRESHOLD)
                continue;
            velocity[i] += ((sensorEvent.values[i] + prevAcceleration[i]) / 2.0f) * dt;
            // position[i] += velocity[i] * dt;
        }
        if (sensorEvent.values[0] >= A_THRESHOLD) {
            position[0] += velocity[0] * dt * Math.cos(theta[1]);
            position[2] += velocity[2] * dt * Math.sin(theta[1]);
        }
        System.arraycopy(sensorEvent.values, 0, prevAcceleration, 0, 3);
        prevTimestampAcc = sensorEvent.timestamp;
        Log.d(TAG, "--- r(X, Y, Z) = (" + position[0] + ", " +
                position[1] + ", " +
                position[2] + ")"
        );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            handleAccelerometer(sensorEvent);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            handleGyroscope(sensorEvent);
        }
        xyValueArray.add(new XYValue(position[0], position[2]));
    }
}