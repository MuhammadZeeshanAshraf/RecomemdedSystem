package com.ztech.recomemdedsystem.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ztech.recomemdedsystem.Activities.AddLocationActivity;
import com.ztech.recomemdedsystem.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final int REQUEST_CODE = 101;
    private String currentUserID;
    private View view;
    private GoogleMap Map;
    private MapView mapView;
    private GoogleApiClient googleApiClient;
    private double latitude, longitude;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private MarkerOptions markerOptions;
    private Location currentlocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FloatingActionsMenu map_add;
    private FloatingActionButton my_location, add_location, friend_location, find_frind_location;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private LinearLayout searchlayout;
    private EditText searchBox;
    private ImageView search;
    com.google.android.material.floatingactionbutton.FloatingActionButton add;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.colorPrimary, R.color.blue, R.color.colorAccent, R.color.primary_dark_material_light};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_maps, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        googleApiClient = getAPIClientInstance();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        add = view.findViewById(R.id.addLocation);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext() , AddLocationActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        requestGPSSettings();
    }


    private GoogleApiClient getAPIClientInstance() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API).build();
        return mGoogleApiClient;
    }

    private void requestGPSSettings() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(500);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
//                        Toast.makeText(getContext(), "GPS is already enable", Toast.LENGTH_SHORT).show();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("", "Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings ");
                        try {
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
//                            Toast.makeText(getContext(), "Checking", Toast.LENGTH_SHORT).show();


                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getContext(), "Location settings are inadequate, and cannot be fixed here", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map_view);

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        Map = googleMap;
        Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.night_mode));

            if (!success) {
                Log.e("Maps", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Maps", "Can't find style. Error: ", e);
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Map.setMyLocationEnabled(true);

        }
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 20, 20);
        }

        if (currentlocation != null) {
            LatLng latLng = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("User Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


            currentUserLocationMarker = Map.addMarker(markerOptions);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            Map.moveCamera(update);
        } else {
            LatLng lahore = new LatLng(31.520370, 74.358749);
            LatLng shah = new LatLng(31.617720, 74.292834);
            LatLng uet = new LatLng(31.586652, 74.382433);

            LatLng latLng = new LatLng(31.6946215, 74.2445379);

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("UET KSK Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


            googleMap.addMarker(new MarkerOptions().position(lahore).title("Shop 1"));
            googleMap.addMarker(new MarkerOptions().position(shah).title("Shop 2"));
            googleMap.addMarker(new MarkerOptions().position(uet).title("Shop 3"));
            googleMap.addMarker(markerOptions);
//            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(lahore).
                    icon(BitmapDescriptorFactory.fromBitmap(
                            createCustomMarker(getContext(), R.drawable.profile, "Shop 1")))).setTitle("Shop 1");


            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lahore, 0));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lahore, 10));
                }
            }, 500);




//            currentUserLocationMarker = Map.addMarker(markerOptions);
//            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 10);
//            Map.moveCamera(update);
        }


        Map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                if (marker.equals(markerOptions)) {

//                   LatLng start = new LatLng(18.015365, -77.499382);
//                    LatLng waypoint= new LatLng(18.01455, -77.499333);
//                    LatLng end = new LatLng(18.012590, -77.500659);
//
//                    Routing routing = new Routing.Builder()
//                            .travelMode(Routing.TravelMode.WALKING)
//                            .waypoints(start, waypoint, end)
//                            .build();
//                    routing.execute();
//
//                    Toast.makeText(getContext(), "" + routing, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


//        LatLng origin = new LatLng(-7.788969, 110.338382);
//        LatLng destination = new LatLng(-7.781200, 110.349709);
//        DrawRouteMaps.getInstance(getContext())
//                .draw(origin, destination, Map);
//        DrawMarker.getInstance(getContext()).draw(Map, origin, R.drawable.ic_check, "Origin Location");
//        DrawMarker.getInstance(getContext()).draw(Map, destination, R.drawable.ic_arrow_close, "Destination Location");
//
//        LatLngBounds bounds = new LatLngBounds.Builder()
//                .include(origin)
//                .include(destination).build();
//        Point displaySize = new Point();
////        getContext().getWindowManager().getDefaultDisplay().getSize(displaySize);
//        Map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));

//        Map.addMarker(new MarkerOptions()
//                .position(new LatLng(0, 0))
//                .title("Marker")
//                .draggable(true)
//                .snippet("Hello")
//                .draggable(true)
//                .icon(BitmapDescriptorFactory.fromBitmap(
//                        createCustomMarker(getContext(),R.drawable.marker_background,"Dragable Marker")))).setTitle("Pick Any Location");


    }


    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        TextView txt_name = (TextView) marker.findViewById(R.id.name);
        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        lastLocation = location;
        if (currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("User Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


        currentUserLocationMarker = Map.addMarker(markerOptions);

        Map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        Map.animateCamera(CameraUpdateFactory.zoomBy(12));

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }


    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("types=" + nearbyPlace);
        sb.append("&location=" + latitude + "," + longitude);
        sb.append("&radius=10000");
        sb.append("&sensor=true");
        Log.d("URL", sb.toString());
        sb.append("&key=AIzaSyCNSMV9xKktTbmB9HvCl9qn7FLGfB91a6M");
        return sb.toString();

    }


}
