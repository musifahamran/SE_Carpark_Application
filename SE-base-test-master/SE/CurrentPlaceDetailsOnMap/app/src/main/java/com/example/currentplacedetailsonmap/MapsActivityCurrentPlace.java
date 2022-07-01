package com.example.currentplacedetailsonmap;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Collections;
import java.util.Comparator;

/**
 * Main menu displaying the map and list
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements View.OnClickListener, OnMarkerClickListener,OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private LocationCallback locationCallback;
    private LocationCallback locationUpdateCallback;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationRequest locationUpdate;

    private boolean requestingLocationUpdates;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(1.5, 103);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //UI
    ImageButton btnShowCarparkList;
    LinearLayout layoutContainer;
    boolean carparkListExpand = false;
    RelativeLayout leftRL;
    RelativeLayout rightRL;
    DrawerLayout drawerLayout;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(0) // This MUST be set, otherwise no updates
                .setSmallestDisplacement(GlobalInstance.getInstance().metresToRefresh);

        //Update location every second
        locationUpdate = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0)
                .setFastestInterval(0);

        //Fetch data from LTA
        new MyAsyncClass().execute("http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2?$skip=");


        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        //UI Stuff
        btnShowCarparkList = (ImageButton) findViewById(R.id.imageButton1);
        btnShowCarparkList.setOnClickListener(this);
        layoutContainer = (LinearLayout) findViewById(R.id.layout);
        GlobalInstance.getInstance().recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert GlobalInstance.getInstance().recyclerView  != null;
        GlobalInstance.getInstance().recyclerView .setLayoutManager(new LinearLayoutManager(this));
        GlobalInstance.getInstance().adapter = new MyRecyclerViewAdapter(this, GlobalInstance.getInstance().nearestCarparkList);
        GlobalInstance.getInstance().recyclerView .setAdapter(GlobalInstance.getInstance().adapter);
        ViewGroup.LayoutParams params = layoutContainer.getLayoutParams();
        params.height =100;
        layoutContainer.setLayoutParams(params);
        leftRL = (RelativeLayout)findViewById(R.id.whatYouWantInLeftDrawer);
        rightRL = (RelativeLayout)findViewById(R.id.whatYouWantInRightDrawer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        requestingLocationUpdates = true;

        // Build the map.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Updates user location every second
        locationUpdateCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {

                        //Updates user location
                        GlobalInstance.getInstance().userLocation = location;
                        final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                        //Resort the nearest carparks based on distance and updates the carpark list
                        Collections.sort(GlobalInstance.getInstance().nearestCarparkList,
                                Comparator.comparingDouble(CarparkItem::getDistanceToUser));
                        GlobalInstance.getInstance().adapter.update(GlobalInstance.getInstance().nearestCarparkList);
                        updateLocationUI();

                        //Camera only follow user if this is allowed, can make it false in either search to not make camera follow until user cancels search
                        if (GlobalInstance.getInstance().cameraFollowUser) {
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 15);
                            GlobalInstance.getInstance().mMap.animateCamera(update);

                        }
                        //Calculates and updates user distance to each carpark
                        for (int i = 0; i < GlobalInstance.getInstance().nearestCarparkList.size(); i++) {
                            GlobalInstance.getInstance().nearestCarparkList.get(i).setDistanceToUser(calculateDistance(GlobalInstance.getInstance().userLocation, GlobalInstance.getInstance().nearestCarparkList.get(i).getCarparkLocation()));
                        }

                        //Fetches and gets the x nearest carparks for the first time
                        if (!GlobalInstance.getInstance().onFirstMarkerFetch) {
                            UpdateDistances();
                            GlobalInstance.getInstance().onFirstMarkerFetch=true;
                        }
                    }
                }

            }

            ;

        };

        //Updates for new nearest markers every time user moved by x metres
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        UpdateDistances();
                    }
                }
            }

            ;

        };



    }
    /**Opens navigation drawer*/
    public  void onLeft(View view) {
        drawerLayout.openDrawer(leftRL);
    }





    /**Updates user location every second */
    public  void showUserLocation(View view) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(GlobalInstance.getInstance().userLocation.getLatitude(),GlobalInstance.getInstance().userLocation.getLongitude()), 15);
        GlobalInstance.getInstance().mMap.animateCamera(update);

    }

    /**Handle button presses*/
    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.imageButton1:
                if(!carparkListExpand)
                {
                    btnShowCarparkList.animate().rotation(v.getRotation()-180).start();
                    expand(layoutContainer, 800, 500);
                    carparkListExpand=true;
                }
                else
                {
                    btnShowCarparkList.animate().rotation(v.getRotation()-180).start();
                    collapse(layoutContainer, 800, 100);
                    carparkListExpand=false;
                }
                break;
// handle button A click;
                //break;
            case R.id.navBtn2:
                Intent intent = new Intent(MapsActivityCurrentPlace.this,PlaceHolderFare.class);
                startActivity(intent);
                break;
// handle button B click;
            //  break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
    }

    /**Expand list of carparks, can be used for other panels*/
    public static void expand(final LinearLayout v, int duration, int targetHeight) {

        int prevHeight  = v.getHeight();

        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    /**Collaspe list of carparks, can be used for other panels*/
    public static void collapse(final View v, int duration, int targetHeight) {
        int prevHeight  = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }
    @Override
    public void onConnected(Bundle bundle) {

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location) {

    }

    /**Calculates distance between user and carpark*/
    public double calculateDistance(Location userLocation, Location carparkLoction) {
        if(userLocation == null || carparkLoction == null)
            return 9999999;
        else {
            double distance = userLocation.distanceTo(carparkLoction);
            return distance;
        }
    }
    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        GlobalInstance.getInstance().mMap=map;
        GlobalInstance.getInstance().mMap.setOnMarkerClickListener(this);

        if(GlobalInstance.getInstance().userLocation!=null) {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(GlobalInstance.getInstance().userLocation.getLatitude(), GlobalInstance.getInstance().userLocation.getLatitude()), 11);
            GlobalInstance.getInstance().mMap.animateCamera(update);
        }
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        GlobalInstance.getInstance().mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        GlobalInstance.getInstance().mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
                //Deselect any markers if previously selected to remove and to allow camera to follow user again
                if(GlobalInstance.getInstance().selectedCarparkItem != null && !GlobalInstance.getInstance().nearestCarparkList.contains(GlobalInstance.getInstance().selectedCarparkItem)) {
                    GlobalInstance.getInstance().selectedCarparkItem.getCarparkMarker().remove();
                    GlobalInstance.getInstance().cameraFollowUser=true;
                    GlobalInstance.getInstance().selectedCarparkItem = null;
                }
            }
        });
        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
     }


    /** Called when the user clicks a marker. Update this to show the carpark item*/
    @Override
    public boolean onMarkerClick(final Marker marker) {
        //Stops camera from follow user and to keep it on marker instead
        GlobalInstance.getInstance().cameraFollowUser=false;
        GlobalInstance.getInstance().selectedCarparkItem=(CarparkItem)marker.getTag();

        return false;
    }

    /**Remove all markers on map since user has already reached x distance to refresh nearest carpark list
     * Call fetch to get a whole new set of nearest carparks
     * */

    public void UpdateDistances() {
        GlobalInstance.getInstance().mMap.clear();

        //If user click on marker before movement, cause the map will clear all markers so we must add back the one user clicked.
        if(GlobalInstance.getInstance().selectedCarparkItem!=null)
        GlobalInstance.getInstance().mMap.addMarker(new MarkerOptions()
                .position(new LatLng(GlobalInstance.getInstance().selectedCarparkItem.getCarparkLocation().getLatitude(), GlobalInstance.getInstance().selectedCarparkItem.getCarparkLocation().getLongitude()))
                .title(GlobalInstance.getInstance().selectedCarparkItem.getCarparkDevelopment() + " " + GlobalInstance.getInstance().selectedCarparkItem.getCarLots() + " " + GlobalInstance.getInstance().selectedCarparkItem.getMotorLots() + " " + GlobalInstance.getInstance().selectedCarparkItem.getHvLots()));
        new UIAsync().execute(" " );

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();

        }
    }

    /**Callbacks to update user location every second and nearest carparks list every y metres*/
    private void startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */);
        mFusedLocationProviderClient.requestLocationUpdates(locationUpdate,
                locationUpdateCallback,
                null /* Looper */);

    }

    /**
     * Gets the current location of the device, and positions the map's camera (If gps is on)
     * Shows user to the settings gps page (If gps isn't on)
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if ( locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            try {
                if (mLocationPermissionGranted) {
                    Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                    locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the device.

                                mLastKnownLocation = task.getResult();


                                GlobalInstance.getInstance().mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                GlobalInstance.getInstance().mMap.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                GlobalInstance.getInstance().mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
        else
        {
            Toast.makeText(this,
                    "Please enable your gps!",
                    Toast.LENGTH_LONG).show();
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        //updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (GlobalInstance.getInstance().mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                GlobalInstance.getInstance().mMap.setMyLocationEnabled(true);
                //GlobalInstance.getInstance().mMap.animateCamera();
                GlobalInstance.getInstance().mMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                GlobalInstance.getInstance().mMap.setMyLocationEnabled(false);
                //GlobalInstance.getInstance().mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
