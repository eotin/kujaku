package io.ona.kujaku.services;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

/**
 * GPS Location Tracking Service used in Foreground
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
public class GPSLocationTrackingService extends TrackingService {
    // Location Manager
    private volatile LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        try {
            // Remove listeners
            if (locationManager != null && locationListener != null) {
                Log.d(TAG, "Remove location manager updates.");
                locationManager.removeUpdates(locationListener);
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to stop service properly.", e);
        }

        super.onDestroy();
    }

    @Override
    protected boolean isProviderEnabled() {
       return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ;
    }


    /***
     * Register LocationManager request locations updates
     */
    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationListener() {
        Log.d(TAG, "Register location update listener.");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                trackingServiceOptions.getMinTime(),
                trackingServiceOptions.getGpsMinDistance(),
                locationListener, Looper.myLooper());
    }

    /**
     * Volatile because different methods are called from the main thread and serviceThread
     */
    private volatile LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d(TAG, "GPS available.");
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d(TAG, "GPS temporary unavailable.");
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d(TAG, "GPS out of service.");
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // See GPS Broadcast receiver
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "GPS Provider has been disabled.");
            Log.i(TAG, "Stopping tracking service.");
            // Stop the service
            GPSLocationTrackingService.this.stopSelf();
        }

        @Override
        public void onLocationChanged(Location location) {
            GPSLocationTrackingService.this.onLocationChanged(location);
        }
    };
}
