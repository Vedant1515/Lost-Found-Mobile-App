package com.example.lostandfound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> permissionLauncher;

    // Maps each Marker → its DB item ID for InfoWindow click navigation
    private final Map<Marker, Integer> markerItemIdMap = new HashMap<>();
    private final List<Marker> allMarkers = new ArrayList<>();

    private Circle radiusCircle;
    private double currentLat = 0;
    private double currentLng = 0;
    private int radiusKm = 10;
    private boolean radiusFilterEnabled = false;

    private TextView tvRadiusValue;
    private SeekBar seekBarRadius;
    private SwitchMaterial switchRadiusFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lost & Found Map");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ── Permission launcher ──────────────────────────────────────────────
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean granted = Boolean.TRUE.equals(
                            permissions.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    if (granted) {
                        enableMyLocationAndFetch();
                    } else {
                        Snackbar.make(
                                findViewById(android.R.id.content),
                                "Location permission is needed to use the radius filter.",
                                Snackbar.LENGTH_LONG).show();
                    }
                });

        // ── Radius controls ──────────────────────────────────────────────────
        tvRadiusValue      = findViewById(R.id.tvRadiusValue);
        seekBarRadius      = findViewById(R.id.seekBarRadius);
        switchRadiusFilter = findViewById(R.id.switchRadiusFilter);

        seekBarRadius.setMax(49);        // 0-49 → 1-50 km
        seekBarRadius.setProgress(9);    // default 10 km

        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusKm = progress + 1;
                tvRadiusValue.setText(radiusKm + " km");
                if (radiusFilterEnabled && currentLat != 0) {
                    updateRadiusCircle();
                    applyRadiusFilter();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        switchRadiusFilter.setOnCheckedChangeListener((btn, isChecked) -> {
            radiusFilterEnabled = isChecked;
            if (isChecked) {
                if (currentLat == 0 && currentLng == 0) {
                    requestLocationPermission();
                } else {
                    updateRadiusCircle();
                    applyRadiusFilter();
                }
            } else {
                // Filter off — show all markers and remove circle
                if (radiusCircle != null) { radiusCircle.remove(); radiusCircle = null; }
                for (Marker m : allMarkers) m.setVisible(true);
            }
        });

        // ── Map fragment ─────────────────────────────────────────────────────
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    // ── OnMapReadyCallback ───────────────────────────────────────────────────

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Tapping an InfoWindow opens ItemDetailActivity for that item
        googleMap.setOnInfoWindowClickListener(marker -> {
            Integer itemId = markerItemIdMap.get(marker);
            if (itemId != null) {
                Intent intent = new Intent(this, ItemDetailActivity.class);
                intent.putExtra("ITEM_ID", itemId);
                startActivity(intent);
            }
        });

        loadMarkersFromDatabase();
        requestLocationPermission();
    }

    // ── Marker loading ───────────────────────────────────────────────────────

    private void loadMarkersFromDatabase() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<LostFoundItem> items = db.getAllItemsWithLocation();

        googleMap.clear();
        allMarkers.clear();
        markerItemIdMap.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasMarkers = false;

        for (LostFoundItem item : items) {
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
            float hue = "Lost".equals(item.getPostType())
                    ? BitmapDescriptorFactory.HUE_RED
                    : BitmapDescriptorFactory.HUE_GREEN;

            MarkerOptions opts = new MarkerOptions()
                    .position(position)
                    .title(item.getName())
                    .snippet(item.getCategory() + "  ·  " + item.getPostType())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue));

            Marker marker = googleMap.addMarker(opts);
            if (marker != null) {
                allMarkers.add(marker);
                markerItemIdMap.put(marker, item.getId());
                boundsBuilder.include(position);
                hasMarkers = true;
            }
        }

        if (hasMarkers) {
            // Wait for the map to finish rendering before fitting bounds
            final LatLngBounds bounds = boundsBuilder.build();
            googleMap.setOnMapLoadedCallback(() ->
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100)));
        }
    }

    // ── Location helpers ─────────────────────────────────────────────────────

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndFetch();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocationAndFetch() {
        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                if (radiusFilterEnabled) {
                    updateRadiusCircle();
                    applyRadiusFilter();
                }
            }
        });
    }

    // ── Radius filter ────────────────────────────────────────────────────────

    private void updateRadiusCircle() {
        if (googleMap == null) return;
        if (radiusCircle != null) radiusCircle.remove();
        radiusCircle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(currentLat, currentLng))
                .radius(radiusKm * 1000.0)           // metres
                .strokeColor(Color.parseColor("#1565C0"))
                .fillColor(Color.parseColor("#1A1565C0"))  // 10% opacity blue
                .strokeWidth(2f));
    }

    private void applyRadiusFilter() {
        if (currentLat == 0 && currentLng == 0) return;
        float[] result = new float[1];
        for (Marker marker : allMarkers) {
            Location.distanceBetween(
                    currentLat, currentLng,
                    marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    result);
            marker.setVisible(result[0] <= radiusKm * 1000f);
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
