package io.ona.kujaku.listeners;

import android.location.Location;

import io.ona.kujaku.services.TrackingService;

/**
 * Listener called by the Tracking Service
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
public interface TrackingServiceListener {

    /**
     * When first location is received.
     * This location is not registered by the Tracker Service but you can force the recording by using the {@link TrackingService#takeLocation()} method
     *
     * @param location
     */
    void onFirstLocationReceived(Location location);

    /**
     * When a location is registered
     *
     * @param location
     */
    void onNewLocationReceived(Location location);

    /**
     * When the location recorder is close to the departure location
     *
     * @param location
     */
    void onCloseToDepartureLocation(Location location);

    /**
     * When the connection is done with the service
     *
     * @param service
     */
    void onServiceConnected(TrackingService service);

    /**
     * When the connection is closed
     */
    void onServiceDisconnected();
}
