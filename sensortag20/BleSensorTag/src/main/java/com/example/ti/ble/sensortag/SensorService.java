package com.example.ti.ble.sensortag;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tahiyasalam on 8/5/15.
 */
public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "SensorTag_Test/SensorService";

    private final static int SENS_LIGHT = Sensor.TYPE_LIGHT;
    SensorManager mSensorManager;
    private PowerManager.WakeLock wl;

    private BufferedWriter outputLux;

    private TimeString timestring = new TimeString();

    @Override
    public void onCreate() {
        super.onCreate();

        startMeasurement();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMeasurement();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement() {
        Log.d(TAG, "start measurement in wear: SensorService");
        String prefix = Environment.getExternalStorageDirectory() + "/sensor_tag/lux_" + timestring.currentTimeForFile();

        try {
            outputLux = new BufferedWriter(new FileWriter(prefix + "lux.csv"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        Sensor lightSensor = mSensorManager.getDefaultSensor(SENS_LIGHT);

        if (mSensorManager != null) {
            if (lightSensor != null) {
                mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.w(TAG, "No Light Sensor found");
            }
        }
    }

    protected void stopMeasurement() {
        mSensorManager.unregisterListener(this);
        mSensorManager = null;

        try {
            outputLux.flush();
            outputLux.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        wl.release();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        long timestamp = event.timestamp;

        try {
            if (type == SENS_LIGHT) {
                outputLux.write(timestamp + "," + event.values[0] + "\n");
                outputLux.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
