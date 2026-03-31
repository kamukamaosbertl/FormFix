package com.example.smartfit.maps;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationHelper {

    public interface Listener {
        void onLocationFound(LatLng latLng);
        void onLocationError(String message);
    }

    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(Listener listener) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        listener.onLocationFound(
                                new LatLng(location.getLatitude(), location.getLongitude())
                        );
                    } else {
                        listener.onLocationError("Current location unavailable");
                    }
                })
                .addOnFailureListener(e ->
                        listener.onLocationError("Failed to get location: " + e.getMessage()));
    }
}