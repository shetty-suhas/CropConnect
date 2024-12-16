package com.crop.cropconnect;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.datatransport.BuildConfig;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class dashboard extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private CardView resultsCard;
    private RecyclerView searchResultsRecyclerView;
    private List<Address> searchResults = new ArrayList<>();
    private Handler searchHandler = new Handler();
    private static final long SEARCH_DELAY = 1000; // 1 second delay
    private Runnable searchRunnable;
    private Marker currentLocationMarker;
    private BottomSheetBehavior<NestedScrollView> bottomSheetBehavior;
    private ConstraintLayout mapContainer;
    private RecyclerView marketsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Places
        Places.initialize(getApplicationContext(), getApiKey());

        // Initialize views
        initializeViews();

        // Setup components
        setupBottomSheet();
        setupSearchView();
        setupRecyclerView();

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeViews() {
        searchView = findViewById(R.id.searchView);
        resultsCard = findViewById(R.id.resultsCard);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        mapContainer = findViewById(R.id.mapContainer);
        NestedScrollView bottomSheet = findViewById(R.id.bottomSheet);
        marketsRecyclerView = findViewById(R.id.marketsRecyclerView);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    }
    private String getApiKey() {
        try {
            ApplicationInfo ai = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Maps", "Failed to load API key", e);
            return "";
        }
    }
    private void setupBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Handle state changes if needed
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Adjust map size based on bottom sheet position
                float newPercent = 0.7f - (slideOffset * 0.3f); // 70% to 40%
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                        findViewById(R.id.map).getLayoutParams();
                params.matchConstraintPercentHeight = newPercent;
                findViewById(R.id.map).setLayoutParams(params);
            }
        });

        // Setup markets recycler view
        marketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupMarketsAdapter();
    }

    private void setupMarketsAdapter() {
        List<Market> markets = new ArrayList<>();
        markets.add(new Market("Central Farmers Market", "2.5 km", "Open", "4.5"));
        markets.add(new Market("City Agricultural Market", "3.2 km", "Open", "4.2"));
        markets.add(new Market("Local Produce Market", "4.1 km", "Closed", "4.0"));
        markets.add(new Market("Green Valley Market", "5.0 km", "Open", "4.7"));
        markets.add(new Market("Fresh Harvest Market", "5.5 km", "Open", "4.3"));

        MarketsAdapter adapter = new MarketsAdapter(markets);
        marketsRecyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setQueryHint("Search location");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                searchView.clearFocus();
                resultsCard.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 2) {
                    searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> getAddressSuggestions(newText);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
                } else {
                    resultsCard.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getAddressSuggestions(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(-90, -180),
                new LatLng(90, 180)
        );

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setOrigin(new LatLng(20.5937, 78.9629))  // India's coordinates
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        PlacesClient placesClient = Places.createClient(this);

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            resultsCard.setVisibility(View.VISIBLE);
            List<PlacePrediction> predictions = new ArrayList<>();

            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                predictions.add(new PlacePrediction(
                        prediction.getPlaceId(),
                        prediction.getPrimaryText(null).toString(),
                        prediction.getSecondaryText(null).toString()
                ));
            }

            updateSearchResults(predictions);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
            }
        });
    }
    private void updateSearchResults(List<PlacePrediction> predictions) {
        PlacePredictionAdapter adapter = new PlacePredictionAdapter(predictions, prediction -> {
            fetchPlaceDetails(prediction.placeId);
            resultsCard.setVisibility(View.GONE);
            searchView.clearFocus();
        });
        searchResultsRecyclerView.setAdapter(adapter);
    }

    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.LAT_LNG,
                Place.Field.NAME,
                Place.Field.ADDRESS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        PlacesClient placesClient = Places.createClient(this);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            LatLng latLng = place.getLatLng();
            if (latLng != null) {
                moveToLocation(latLng, place.getAddress());
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
                Toast.makeText(this, "Error fetching place details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveToLocation(LatLng latLng, String title) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

        // Add back the current location marker
        updateCurrentLocationMarker();
    }

    private void updateCurrentLocationMarker() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }
                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // Set map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
            }
        });
    }
    private void searchLocation(String query) {
        // Hide keyboard after search
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(query, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Add a marker at the searched location
                mMap.clear(); // Clear previous markers
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(address.getAddressLine(0)));

                // Animate camera to the location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                // Update nearby markets based on new location
                updateNearbyMarkets(latLng);
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNearbyMarkets(LatLng location) {
        // This is where you would implement the logic to fetch nearby markets
        // based on the new location. For now, we'll just show a toast
        Toast.makeText(this, "Updating nearby markets...", Toast.LENGTH_SHORT).show();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user
            Toast.makeText(this,
                    "Location permission is needed for showing your current location",
                    Toast.LENGTH_LONG).show();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMap != null) {
            mMap.setMyLocationEnabled(false);
        }
    }
}