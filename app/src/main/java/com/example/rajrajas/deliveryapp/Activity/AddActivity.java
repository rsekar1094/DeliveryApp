package com.example.rajrajas.deliveryapp.Activity;

import android.content.Context;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rajrajas.deliveryapp.DbController;
import com.example.rajrajas.deliveryapp.R;
import com.example.rajrajas.deliveryapp.dummy;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.rajrajas.deliveryapp.R.id.map;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

/**
 * Created by rajrajas on 5/17/2017.
 */

public class AddActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private String Address;
    private DbController dbController;
    private LatLng tlatLng;
    private TextView latlngtext;
    private EditText ConsigneeName, commenttext;
    private LinearLayout comment_lay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addactivity);

        getSupportActionBar().setTitle("Add Consignment");
        dbController = new DbController(dummy.db_context);

        ConsigneeName = (EditText) findViewById(R.id.consignee_name_text);
        commenttext = (EditText) findViewById(R.id.comment_text);
        latlngtext = (TextView) findViewById(R.id.latlng_text);

        comment_lay = (LinearLayout) findViewById(R.id.comment_lay);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    //TODO trigger this method when map loaded successfully

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mMap != null) {
            return;
        }
        mMap = googleMap;

        setUpMap();
    }

    private void setUpMap() {

        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(map)).getMapAsync(this);
        }

        mMap.moveCamera(newLatLngZoom( new LatLng(13.0891, 80.2096), 15));
        mMap.setOnMapClickListener(this);
    }

    //TODO used for marking the cliked location and consider that as Consignee Location
    @Override
    public void onMapClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        try {
            Address = getAddressList(latLng);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        View view = getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        tlatLng = latLng;
        latlngtext.setText("Selected Latitude and Longitude is " + latLng.latitude + " and " + latLng.longitude);
        latlngtext.setVisibility(View.VISIBLE);
        latlngtext.setAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.showdescription));
        Snackbar.make(findViewById(R.id.main_container), "Selected Address " + Address, Snackbar.LENGTH_SHORT).show();
    }

    //todo returns the full address bssed on the LatLng value
    private String getAddressList(LatLng latLng) throws Exception {
        Geocoder geocoder;
        List<android.location.Address> addresses = null;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert addresses != null;
        return addresses.get(0).getAddressLine(0) + "," + addresses.get(0).getLocality() + "," + addresses.get(0).getAdminArea() + "," + addresses.get(0).getCountryName() + "," + addresses.get(0).getPostalCode();
    }

    //Todo Add consignee button onclick listener. check whether consignee name and location entered if so add those information and clear the textviews
    public void addConsignee(View view) {
        if (tlatLng == null) {
            Snackbar.make(findViewById(R.id.main_container), "Please select the delivery point in map", Snackbar.LENGTH_SHORT).show();
        } else {
            String[] insert_records = {ConsigneeName.getText().toString(), commenttext.getText().toString(), tlatLng.latitude + "", tlatLng.longitude + "", Address};
            if (insert_records[0].equals(""))
                Snackbar.make(findViewById(R.id.main_container), "Please enter the Consignee Name", Snackbar.LENGTH_SHORT).show();
            else {
                dbController.insert_ConsigneeDetails(insert_records);
                ConsigneeName.setText("");
                commenttext.setText("");
                comment_lay.setVisibility(View.GONE);
                mMap.clear();
                latlngtext.setText("");
                Snackbar.make(findViewById(R.id.main_container), "Consignee Information has been Successfully added", Snackbar.LENGTH_SHORT).show();
            }

        }

    }

    //todo Add description button on click listener. Make the comment lay as Visible so that user can enter their commnets
    public void AddDescription(View view) {
        if (comment_lay.getVisibility() == View.GONE) {
            view.setVisibility(View.GONE);
            comment_lay.setVisibility(View.VISIBLE);
            comment_lay.setAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.showdescription));
        }
    }
}
