package io.ona.kujaku.services;

import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


/**
 * Fused Location Tracking Service used in Foreground
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
public class FusedLocationTrackingService extends TrackingService {

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    protected boolean isProviderEnabled() {
       return true;
    }

    @Override
    public void onDestroy() {
        try {
            // Remove listeners
            if (fusedLocationClient != null && locationCallback != null) {
                Log.d(TAG, "Remove location manager updates.");
                this.fusedLocationClient.removeLocationUpdates(locationCallback);
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to stop service properly.", e);
        }

        super.onDestroy();

        Log.i(TAG, "Tracking service stopped.");
    }


    /***
     * Register LocationManager request locations updates
     */
    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationListener() {
        Log.d(TAG, "Register location update listener.");
        // https://stackoverflow.com/questions/33022662/android-locationmanager-vs-google-play-services

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(trackingServiceOptions.getMinTime());
        locationRequest.setInterval(trackingServiceOptions.getMinTime());

        this.fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }


    private volatile LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult.getLastLocation() != null) {
                FusedLocationTrackingService.this.onLocationChanged(locationResult.getLastLocation());
            }
        }
    };
}
