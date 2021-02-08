package com.ztech.recomemdedsystem.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ztech.recomemdedsystem.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback, Listener {

    SimpleLocation location;
    private GoogleMap mMap;
    Toolbar locationPrivacyToolBar;
    CircularProgressButton btn;
    EasyWayLocation easyWayLocation;
    LocationRequest request;
    private static AlertDialog waitingDialog;
    String lan, lon, routeDistance;
    Double a, b;
    int rate = 10;
    DatabaseReference rootReference;
    FirebaseAuth firebaseAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_location);

        firebaseAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Rate")) {
                    rate = Integer.parseInt(dataSnapshot.child("Rate").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        lan = "null";
        lon = "null";
        initializeField();
        showLoadingDialog();

        request = new LocationRequest();
        request.setInterval(50000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        easyWayLocation = new EasyWayLocation(AddLocationActivity.this, request, false, this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
        location = new SimpleLocation(AddLocationActivity.this);
        easyWayLocation.startLocation();
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(AddLocationActivity.this);
        } else {
            easyWayLocation.startLocation();
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();

        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!(lan.equals("null"))) {
                    btn.startAnimation();

                } else {
                    Toasty.info(AddLocationActivity.this, "First Chose some Location on Map", Toasty.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void initializeField() {

        btn = findViewById(R.id.location_privacy_done);
        locationPrivacyToolBar = findViewById(R.id.location_privacy_toolbar);
        setSupportActionBar(locationPrivacyToolBar);
        locationPrivacyToolBar.setNavigationIcon(R.drawable.ic_arrow_back);
        locationPrivacyToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng current = new LatLng(31.512765, 74.2581817);
        googleMap.addMarker(new MarkerOptions().position(current).
                icon(BitmapDescriptorFactory.fromBitmap(
                        createCustomMarker(AddLocationActivity.this, R.drawable.person_picture, "Location")))).setTitle("Location");

        waitingDialog.dismiss();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 0));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
            }
        }, 500);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                mMap.clear();
                lan = "" + point.latitude;
                a = point.latitude;
                lon = "" + point.longitude;
                b = point.longitude;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                mMap.addMarker(markerOptions);
            }
        });
    }


    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String
            _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        TextView txt_name = marker.findViewById(R.id.name);
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
    public void locationOn() {

    }

    @Override
    public void currentLocation(Location location) {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(current).
                icon(BitmapDescriptorFactory.fromBitmap(
                        createCustomMarker(AddLocationActivity.this, R.drawable.person_picture, "Current Location")))).setTitle("Current Location");

        waitingDialog.dismiss();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
    }

    @Override
    public void locationCancelled() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        easyWayLocation.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        easyWayLocation.endUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        easyWayLocation.endUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        easyWayLocation.startLocation();
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting up the layout for alert dialog
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_wait, null, false);
        builder.setView(view);
        waitingDialog = builder.create();
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        waitingDialog.show();

    }




//    public void SendUserToActivity(int id) {
//        Intent intent = new Intent(AddLocationActivity.this, OrderSuccessActivity.class);
//        intent.putExtra("id", id + "");
//        startActivity(intent);
//        finish();
//    }
}
