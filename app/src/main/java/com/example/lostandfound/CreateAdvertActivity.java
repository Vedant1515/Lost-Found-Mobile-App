package com.example.lostandfound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etDescription, etDate, etLocation;
    private RadioGroup rgPostType;
    private Spinner spinnerCategory;
    private ImageView ivImagePreview;

    private String selectedImageUri = "";
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    private ActivityResultLauncher<Intent>   imagePickerLauncher;
    private ActivityResultLauncher<Intent>   placesLauncher;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private FusedLocationProviderClient      fusedLocationClient;

    private static final String[] CATEGORIES =
            {"Electronics", "Pets", "Wallets", "Keys", "Clothing", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Advert");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ── View bindings ────────────────────────────────────────────────────
        etName        = findViewById(R.id.etName);
        etPhone       = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etDate        = findViewById(R.id.etDate);
        etLocation    = findViewById(R.id.etLocation);
        rgPostType    = findViewById(R.id.rgPostType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        ivImagePreview  = findViewById(R.id.ivImagePreview);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // ── Places SDK ───────────────────────────────────────────────────────
        // ★ PASTE YOUR GOOGLE MAPS API KEY INTO local.properties as:
        //     MAPS_API_KEY=AIzaSy...yourkey...
        //   BuildConfig.MAPS_API_KEY is auto-generated from that value.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }

        // ── Location provider ─────────────────────────────────────────────
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ── Result launchers ─────────────────────────────────────────────────

        // Image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (SecurityException ignored) {}
                            selectedImageUri = uri.toString();
                            ivImagePreview.setImageURI(uri);
                            ivImagePreview.setVisibility(View.VISIBLE);
                        }
                    }
                });

        // Places Autocomplete
        placesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AutocompleteActivity.RESULT_OK
                            && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        if (place.getAddress() != null) {
                            etLocation.setText(place.getAddress());
                        }
                        if (place.getLatLng() != null) {
                            selectedLat = place.getLatLng().latitude;
                            selectedLng = place.getLatLng().longitude;
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        Toast.makeText(this, "Location search failed", Toast.LENGTH_SHORT).show();
                    }
                });

        // Location permission
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    if (Boolean.TRUE.equals(
                            permissions.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
                        fetchCurrentLocation();
                    } else {
                        Toast.makeText(this,
                                "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // ── Click listeners ──────────────────────────────────────────────────

        etDate.setOnClickListener(v -> showDatePicker());

        // Tapping the location field opens Places Autocomplete
        etLocation.setOnClickListener(v -> launchPlacesAutocomplete());

        // GPS button reverse-geocodes current coordinates
        findViewById(R.id.btnGetCurrentLocation).setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        // Image upload
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveAdvert());
    }

    // ── Places Autocomplete ──────────────────────────────────────────────────

    private void launchPlacesAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        placesLauncher.launch(intent);
    }

    // ── GPS current location ─────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        Toast.makeText(this, "Fetching location…", Toast.LENGTH_SHORT).show();
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                reverseGeocode(selectedLat, selectedLng);
            } else {
                Toast.makeText(this,
                        "Could not get location. Ensure GPS is enabled.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(addr.getAddressLine(i));
                }
                etLocation.setText(sb.toString());
            } else {
                etLocation.setText(lat + ", " + lng);
            }
        } catch (Exception e) {
            // Network unavailable or geocoder failed — fall back to raw co-ords
            etLocation.setText(lat + ", " + lng);
        }
    }

    // ── Date picker ──────────────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    private void saveAdvert() {
        int selectedId = rgPostType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select Lost or Found", Toast.LENGTH_SHORT).show();
            return;
        }
        String postType    = (selectedId == R.id.rbLost) ? "Lost" : "Found";
        String name        = etName.getText()        != null ? etName.getText().toString().trim()        : "";
        String phone       = etPhone.getText()       != null ? etPhone.getText().toString().trim()       : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String date        = etDate.getText()        != null ? etDate.getText().toString().trim()        : "";
        String location    = etLocation.getText()    != null ? etLocation.getText().toString().trim()    : "";
        String category    = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LostFoundItem item = new LostFoundItem();
        item.setPostType(postType);
        item.setName(name);
        item.setPhone(phone);
        item.setDescription(description);
        item.setDate(date);
        item.setLocation(location);
        item.setCategory(category);
        item.setImageUri(selectedImageUri);
        item.setLatitude(selectedLat);
        item.setLongitude(selectedLng);

        DatabaseHelper db = new DatabaseHelper(this);
        long id = db.insertItem(item);

        if (id != -1) {
            Toast.makeText(this, "Advert saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save advert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
