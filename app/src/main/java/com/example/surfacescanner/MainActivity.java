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
                        // createScatterPlot();
                        GraphView graph = (GraphView) findViewById(R.id.graph);
                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                                new DataPoint(0, 1),
                                new DataPoint(1, 5),
                                new DataPoint(2, 3),
                                new DataPoint(3, 2),
                                new DataPoint(4, 6)
                        });
                        graph.addSeries(series);
                    }
                }
        );

    }

    public void createScatterPlot()
    {
        Log.d(TAG, "Creating Scatter Plot");

        // xyValueArray = sortArray(xyValueArray);
        for (int i = 0; i < xyValueArray.size(); i++) {
            double x = xyValueArray.get(i).getX();
            double y = xyValueArray.get(i).getY();
            xySeries.appendData(new DataPoint(x, y), true, 1000);
        }

        xySeries.setShape(PointsGraphSeries.Shape.RECTANGLE);
        xySeries.setColor(Color.BLUE);
        xySeries.setSize(20f);

        //set Scrollable and Scaleable
        g.getViewport().setScalable(true);
        g.getViewport().setScalableY(true);
        g.getViewport().setScrollable(true);
        g.getViewport().setScrollableY(true);

        //set manual x bounds
        g.getViewport().setYAxisBoundsManual(true);
        g.getViewport().setMaxY(150);
        g.getViewport().setMinY(-150);

        //set manual y bounds
        g.getViewport().setXAxisBoundsManual(true);
        g.getViewport().setMaxX(150);
        g.getViewport().setMinX(-150);

        g.addSeries(xySeries);
    }

    private ArrayList<XYValue> sortArray(ArrayList<XYValue> array){
        /*
        //Sorts the xyValues in Ascending order to prepare them for the PointsGraphSeries<DataSet>
         */
        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;
        Log.d(TAG, "sortArray: Sorting the XYArray.");


        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            Log.d(TAG, "sortArray: m = " + m);
            try {
                //print out the y entrys so we know what the order looks like
                //Log.d(TAG, "sortArray: Order:");
                //for(int n = 0;n < array.size();n++){
                //Log.d(TAG, "sortArray: " + array.get(n).getY());
                //}
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "sortArray: ArrayIndexOutOfBoundsException. Need more than 1 data point to create Plot." +
                        e.getMessage());
                break;
            }
        }
        return array;
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