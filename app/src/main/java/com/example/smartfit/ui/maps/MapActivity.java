package com.example.smartfit.ui.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartfit.R;
import com.example.smartfit.maps.LocationHelper;
import com.example.smartfit.maps.SavedLocationRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private TextView tvMapStatus;
    private TextView tvSelectedLocation;

    private MaterialButton btnFindMe;
    private MaterialButton btnSaveWorkoutSpot;
    private MaterialButton btnShowSavedSpot;
    private MaterialButton btnClearRoute;

    private LocationHelper locationHelper;
    private SavedLocationRepository savedLocationRepository;

    private LatLng currentLatLng;
    private LatLng selectedLatLng;

    private Marker currentMarker;
    private Marker selectedMarker;
    private Polyline routeLine;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableMyLocation();
                    findCurrentLocation();
                } else {
                    tvMapStatus.setText("Location permission denied");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tvMapStatus = findViewById(R.id.tv_map_status);
        tvSelectedLocation = findViewById(R.id.tv_selected_location);

        btnFindMe = findViewById(R.id.btn_find_me);
        btnSaveWorkoutSpot = findViewById(R.id.btn_save_workout_spot);
        btnShowSavedSpot = findViewById(R.id.btn_show_saved_spot);
        btnClearRoute = findViewById(R.id.btn_clear_route);

        locationHelper = new LocationHelper(this);
        savedLocationRepository = new SavedLocationRepository(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFindMe.setOnClickListener(v -> checkLocationPermissionAndFindMe());
        btnSaveWorkoutSpot.setOnClickListener(v -> saveWorkoutSpot());
        btnShowSavedSpot.setOnClickListener(v -> showSavedSpot());
        btnClearRoute.setOnClickListener(v -> clearSelectedRoute());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        setupMapUi();
        setMapListeners();

        LatLng defaultLocation = new LatLng(-0.6072, 30.6545); // Mbarara approx
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f));

        tvMapStatus.setText("Tap the map to choose a workout spot");
    }

    private void setupMapUi() {
        if (googleMap == null) return;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        checkLocationPermissionAndEnableOnly();

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Exception ignored) {
        }
    }

    private void setMapListeners() {
        if (googleMap == null) return;

        googleMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;

            if (selectedMarker != null) {
                selectedMarker.remove();
            }

            selectedMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("Selected Workout Spot")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );

            tvSelectedLocation.setText(formatLatLng("Selected", latLng));

            drawRouteIfPossible();
        });

        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return false;
        });
    }

    private void checkLocationPermissionAndFindMe() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            findCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkLocationPermissionAndEnableOnly() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (googleMap == null) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void findCurrentLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.Listener() {
            @Override
            public void onLocationFound(LatLng latLng) {
                currentLatLng = latLng;

                if (currentMarker != null) {
                    currentMarker.remove();
                }

                currentMarker = googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title("My Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                );

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                tvMapStatus.setText("Current location found");
                drawRouteIfPossible();
            }

            @Override
            public void onLocationError(String message) {
                tvMapStatus.setText(message);
            }
        });
    }

    private void drawRouteIfPossible() {
        if (googleMap == null || currentLatLng == null || selectedLatLng == null) {
            return;
        }

        if (routeLine != null) {
            routeLine.remove();
        }

        routeLine = googleMap.addPolyline(
                new PolylineOptions()
                        .add(currentLatLng)
                        .add(selectedLatLng)
                        .width(8f)
                        .color(Color.BLUE)
        );

        tvMapStatus.setText("Route drawn to selected workout spot");
    }

    private void saveWorkoutSpot() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Select a location on the map first", Toast.LENGTH_SHORT).show();
            return;
        }

        savedLocationRepository.saveLocation(
                selectedLatLng.latitude,
                selectedLatLng.longitude,
                "Saved Workout Spot"
        );

        Toast.makeText(this, "Workout spot saved", Toast.LENGTH_SHORT).show();
    }

    private void showSavedSpot() {
        if (!savedLocationRepository.hasSavedLocation()) {
            Toast.makeText(this, "No saved workout spot found", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng savedLatLng = new LatLng(
                savedLocationRepository.getSavedLat(),
                savedLocationRepository.getSavedLng()
        );

        selectedLatLng = savedLatLng;

        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        selectedMarker = googleMap.addMarker(
                new MarkerOptions()
                        .position(savedLatLng)
                        .title(savedLocationRepository.getSavedTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(savedLatLng, 16f));
        tvSelectedLocation.setText(formatLatLng("Saved", savedLatLng));
        drawRouteIfPossible();
    }

    private void clearSelectedRoute() {
        selectedLatLng = null;

        if (selectedMarker != null) {
            selectedMarker.remove();
            selectedMarker = null;
        }

        if (routeLine != null) {
            routeLine.remove();
            routeLine = null;
        }

        tvSelectedLocation.setText("Selected: none");
        tvMapStatus.setText("Marker and route cleared");
    }

    private String formatLatLng(String label, LatLng latLng) {
        return String.format(
                Locale.getDefault(),
                "%s: %.5f, %.5f",
                label,
                latLng.latitude,
                latLng.longitude
        );
    }
}