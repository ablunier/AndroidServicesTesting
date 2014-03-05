package net.ablunier.servicestesting.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by apblunier on 5/03/14.
 */
public class LocationTrackerService extends Service implements LocationListener {

    private LocationManager mLocationManager;
    private String mProvider;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 3000 * 60 * 1; // 3 minute

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.wtf("LocationTrackerService", "onStartCommand");
        writeToFile("onStartCommand");

        startLocationTracking();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.wtf("LocationTrackerService", "onBind");
        writeToFile("onBind");

        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        writeToFile("onUnBind");

        return super.onUnbind(intent);
    }

    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        writeToFile("onTaskRemoved");

        wakeUpService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        writeToFile("onDestroy");

        wakeUpService();
    }

    private void wakeUpService() {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
    }

    private void startLocationTracking() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        mProvider = mLocationManager.getBestProvider(criteria, true);

        if (mProvider != null && !mProvider.isEmpty()) {
            Location location = mLocationManager.getLastKnownLocation(mProvider);

            if (location != null) {
                writeToFile("Provider " + mProvider + " has been selected");
                onLocationChanged(location);
            } else {
                writeToFile("Location not available");
            }

            mLocationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        } else {
            writeToFile("Can't get the provider");
        }
    }

    private void writeToFile(String string) {
        try {
            String filename = "testing_geolocation_tracking.txt";
            File myFile = new File(Environment.getExternalStorageDirectory(), filename);

            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

            String logString = currentDateandTime + " -> " + string + "\n";

            FileOutputStream fos;
            byte[] data = logString.getBytes();

            try {
                fos = new FileOutputStream(myFile, true);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e("IOException", "File write failed: " + e.toString());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Double mLatitude = location.getLatitude();
        Double mLongitude = location.getLongitude();

        writeToFile("lat: " + Double.toString(mLatitude) + ", long: " + Double.toString(mLongitude));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        writeToFile("Enabled new provider: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        writeToFile("Disabled provider: " + provider);
    }
}
