package com.example.surfacescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    Sensor accelerometer;

    static float NS2S;
    static float[] prevAcceleration;
    static float[] velocity;
    static float[] position;
    static float[] linearAcceleration;
    static float[] gravity;
    static long prevTimestamp;
    static float[] last_values;
    static {
        NS2S = 1.0f / 1000000000.0f;
        prevAcceleration = new float[]{0f, 0f, 0f};
        velocity = new float[]{0f, 0f, 0f};
        position = new float[]{0f, 0f, 0f};
        linearAcceleration = new float[]{0f, 0f, 0f};
        gravity = new float[]{0f, 0f, 0f};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.start);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Dokme ro zad!");

                Log.d(TAG, "onCreate: Initializing Sensor Services");
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                sensorManager.registerListener(
                        MainActivity.this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL
                );

                Log.d(TAG, "onCreate: Registered accelerometer listener");
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "onSensorChanged:");
        Log.d(TAG, "--- a(X, Y, Z) = (" + sensorEvent.values[0] + " , " +
                sensorEvent.values[1] + ", " +
                sensorEvent.values[2] + ")"
        );

        float dt = (sensorEvent.timestamp - prevTimestamp) * NS2S;
        dt = Math.min(dt, 0.16f);

        Log.d(TAG, "--- dt = " + dt);


        for(int i = 0; i < 3; ++i) {
            if (sensorEvent.values[i] < 0.20)
                continue;
            velocity[i] += ((sensorEvent.values[i] + prevAcceleration[i]) / 2.0f) * dt;
            position[i] += velocity[i] * dt;
        }

        System.arraycopy(sensorEvent.values, 0, prevAcceleration, 0, 3);
        prevTimestamp = sensorEvent.timestamp;
        Log.d(TAG, "--- r(X, Y, Z) = (" + position[0] + ", " +
                position[1] + ", " +
                position[2] + ")"
        );
    }
}