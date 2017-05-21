package com.example.rajrajas.deliveryapp.Activity;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajrajas.deliveryapp.DbController;
import com.example.rajrajas.deliveryapp.LocationDetector;
import com.example.rajrajas.deliveryapp.R;
import com.example.rajrajas.deliveryapp.dummy;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

/**
 * Created by rajrajas on 5/17/2017.
 */

public class ViewActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private GoogleMap mMap;
    private double lat, lon;
    private String Address, status_message;
    private int ConsigneeId;
    private LocationDetector locationDetector;
    private Timer timer;
    private DbController dbController;

    private boolean show_fab_bool = false;
    private FloatingActionButton Navigation, DeliveryguyLocation, ConsigneeLocation;
    private Double currentLocationLatitude = 0.0, currentLocationLongitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewactivity);

        getSupportActionBar().setTitle("View Consignment");

        dbController = new DbController(dummy.db_context);

        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("Description");
        lat = Double.parseDouble(getIntent().getStringExtra("lat"));
        lon = Double.parseDouble(getIntent().getStringExtra("lon"));
        Address = getIntent().getStringExtra("Address");
        status_message = getIntent().getStringExtra("Status");
        ConsigneeId = Integer.parseInt(getIntent().getStringExtra("Id"));

        ((TextView) findViewById(R.id.consignee_name_text)).setText(name);

        if (description.equals(""))
        {
            ((TextView) findViewById(R.id.short_description_text)).setText("No Comments");
        }
        else
            ((TextView) findViewById(R.id.short_description_text)).setText(description);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Navigation = (FloatingActionButton) findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_1);
        DeliveryguyLocation = (FloatingActionButton) findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_2);
        ConsigneeLocation = (FloatingActionButton) findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_3);

        Navigation.setOnClickListener(this);
        DeliveryguyLocation.setOnClickListener(this);
        ConsigneeLocation.setOnClickListener(this);

    }

    //TODO trigger this method when map loaded successfully
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (mMap != null) {
            return;
        }
        mMap = googleMap;

        setUpMapIfNeeded();

    }

    //TODO adding marker for the consignee Location and calls the timer which will update distance between user and consignee Location every 10 seconds
    private void setUpMapIfNeeded() {

        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }

        mMap.moveCamera(newLatLngZoom(
                new LatLng(lat, lon), 15));


        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.userpointer))
                .anchor(0.0f, 1.0f)
                .position(new LatLng(lat, lon))
                .title(Address));

        timer = new Timer();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timer.schedule(new location_detector_timer(), 0, 10000);
    }

    //TODO Respective actions will take based on the result returns from locationDetector
    void calculate_distance(ArrayList<String> result) {
        switch (result.get(0)) {
            case "On your GPS":
                timer.cancel();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                buildAlertMessageNoGps();
                break;
            case "Request Permission":
                request_permission();
                break;
            case "Location Found":
                currentLocationLatitude = Double.parseDouble(result.get(1));
                currentLocationLongitude = Double.parseDouble(result.get(2));
                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.currentpointer))
                        .anchor(0.0f, 1.0f)
                        .position(new LatLng(currentLocationLatitude, currentLocationLongitude))
                        .title(result.get(3)));
                Double dist = locationDetector.distance(lat, lon, Double.parseDouble(result.get(1)), Double.parseDouble(result.get(2)));
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.FLOOR);
                ((TextView) findViewById(R.id.distance_text)).setText(df.format(dist) + " kms");
                break;
            case "Location Not Found":
                Snackbar.make(findViewById(R.id.main_container), "Unable to find the Current Location", Snackbar.LENGTH_SHORT).show();
                break;

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu, menu);
        return true;
    }

    //TODO delete and Complete menu.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.delete:
                dbController.delete_consigneedetails(ConsigneeId);
                Snackbar.make(findViewById(R.id.main_container), "Deleted the Entry", Snackbar.LENGTH_SHORT).show();
                onBackPressed();
                break;
            case R.id.complete:
                dbController.update_Consignee_status(ConsigneeId);
                Snackbar.make(findViewById(R.id.main_container), "Completed the Delivery", Snackbar.LENGTH_SHORT).show();
                onBackPressed();

        }

        return super.onOptionsItemSelected(item);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void request_permission() {
        requestPermissions(dummy.PermissionsLocation, dummy.RequestLocationId);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

    }

    //TODO view Description button Click Listener. Show / Hide the User comment
    public void displayDescription(View view) {
        LinearLayout description_lay = (LinearLayout) findViewById(R.id.description_lay);
        if (description_lay.getVisibility() == View.GONE) {
            ((Button) view).setText("Hide Description");
            showDescription(description_lay);
        } else {
            ((Button) view).setText("View Description");
            showDescription(description_lay);

        }
    }

    //TODO Floating action Button on click listener . Displays the sub menus
    public void fab_click(View view) {
        if (!show_fab_bool) {
            ((FloatingActionButton) view).startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fabanim1));
            show_fab_bool = true;

            show_fab_menu(Navigation, R.anim.fab1_show, 1.7, 0.25);
            show_fab_menu(findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_1_text), R.anim.fab1_show, 0.3, 0.5);

            show_fab_menu(DeliveryguyLocation, R.anim.fab1_show, 1.5, 1.5);
            show_fab_menu(findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_2_text), R.anim.fab1_show, 0.3, 2.0);

            show_fab_menu(ConsigneeLocation, R.anim.fab1_show, 0.25, 1.7);
            show_fab_menu(findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_3_text), R.anim.fab1_show, 0.5, 4.8);


        } else {
            ((FloatingActionButton) view).clearAnimation();

            //((FloatingActionButton) view).setImageResource(R.mipmap.more);
            show_fab_bool = false;

            hide_fab_menu(Navigation, R.anim.fab1_hide, 1.7, 0.25);
            hide_fab_menu(findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_1_text), R.anim.fab1_hide, 0.3, 0.5);

            hide_fab_menu(DeliveryguyLocation, R.anim.fab2_hide, 1.5, 1.5);
            hide_fab_menu(findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_2_text), R.anim.fab2_hide, 0.3, 2.0);

            hide_fab_menu(ConsigneeLocation, R.anim.fab3_hide, 0.25, 1.7);
            hide_fab_menu(findViewById(R.id.fab_sub_menu).findViewById(R.id.fab_3_text), R.anim.fab3_hide,0.5, 4.8);
        }

    }

    //TODO hide the fab sub menus
    private void hide_fab_menu(View view, int AnimId, double d1, double d2) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.rightMargin -= (int) (view.getWidth() * d1);
        layoutParams.bottomMargin -= (int) (view.getHeight() * d2);
        view.setLayoutParams(layoutParams);
        view.startAnimation(AnimationUtils.loadAnimation(getApplication(), AnimId));
        view.setClickable(false);

    }

    //TODO show the fab sub menus
    private void show_fab_menu(View view, int AnimId, double d1, double d2) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.rightMargin += (int) (view.getWidth() * d1);
        layoutParams.bottomMargin += (int) (view.getHeight() * d2);
        view.setLayoutParams(layoutParams);
        view.startAnimation(AnimationUtils.loadAnimation(getApplication(), AnimId));
        view.setClickable(true);
    }


    //TODO triggers when GPS off
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        ViewActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timer = new Timer();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        timer.schedule(new location_detector_timer(), 0, 10000);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    //TODO fab submenus click listener
    //TODO  shows the current location and consignee location
    @Override
    public void onClick(View v) {
        show_fab_bool = true;
        fab_click(findViewById(R.id.fab));
        switch (v.getId()) {
            case R.id.fab_1:
                call_navigationApp();
                break;
            case R.id.fab_2:
                mMap.moveCamera(newLatLngZoom(
                        new LatLng(currentLocationLatitude, currentLocationLongitude), 15));
                break;
            case R.id.fab_3:
                mMap.moveCamera(newLatLngZoom( new LatLng(lat, lon), 15));
                break;
        }

    }

    //TODO Recyclerview Item view touch listener class
    private class location_detector_timer extends TimerTask {
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    locationDetector = new LocationDetector(getApplicationContext());
                    ArrayList<String> locationDetectorResult = null;
                    try {
                        locationDetectorResult = locationDetector.getting_current_location();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    calculate_distance(locationDetectorResult);

                   // Toast.makeText(getApplicationContext(), "entered view activity loop", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    //TODO stopping the timer which used to measure the distance for every 10 seconds
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(ViewActivity.this, MainActivity.class);
        ViewActivity.this.startActivity(intent);
    }

    //TODO stopping the timer which used to measure the distance for every 10 seconds
    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //TODO stopping the timer which used to measure the distance for every 10 seconds
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //TODO comments edittext animation
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showDescription(final LinearLayout lay) {
        if (lay.getVisibility() == View.GONE) {
            int centerX = lay.getWidth() / 2;
            int centerY = lay.getHeight() / 2;
            int startRadius = 0;
            int endRadius = (int) Math.hypot(lay.getWidth(), lay.getHeight()) / 2;

            lay.setVisibility(View.VISIBLE);
            ViewAnimationUtils
                    .createCircularReveal(
                            lay,
                            centerX,
                            centerY,
                            startRadius,
                            endRadius
                    )
                    .setDuration(1000)
                    .start();
        } else {

            int centerX = lay.getWidth() / 2;
            int centerY = lay.getHeight() / 2;
            int startRadius = (int) Math.hypot(lay.getWidth(), lay.getHeight()) / 2;
            int endRadius = 0;

            Animator animator = ViewAnimationUtils
                    .createCircularReveal(
                            lay,
                            centerX,
                            centerY,
                            startRadius,
                            endRadius
                    );
            animator.setDuration(1000);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    lay.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        }
    }

    //TODO Navigation-calls the navigation app with destination point as Consignee location
    void call_navigationApp() {
        String label = Address;
        String uriBegin = "geo:" + lat + "," + lon;
        String query = lat + "," + lon + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}
