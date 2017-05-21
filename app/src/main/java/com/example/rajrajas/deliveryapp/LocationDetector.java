package com.example.rajrajas.deliveryapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Created by rajrajas on 5/18/2017.
 */

public class LocationDetector implements LocationListener {

    private Context context;

    private Boolean location_found = false;
    private ArrayList<String> location_list;

    public LocationDetector(Context context) {
        this.context = context;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //TODO fetch the current Location address
    public ArrayList<String> getting_current_location() throws IOException {


        if (Looper.myLooper() == null)
            Looper.prepare();

        Criteria criteria = new Criteria();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location_list = new ArrayList<>();
            location_list.add("On your GPS");
            return location_list;
        }

        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            location_list = new ArrayList<>();
            location_list.add("Request Permission");
            return location_list;
        }
        locationManager.requestLocationUpdates(provider, 15000, 0, this);

// Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if (location == null) {
            if (provider.equals("gps")) {
                locationManager.requestLocationUpdates(NETWORK_PROVIDER, 15000, 0, (LocationListener) this);
                //Getting Current Location
                location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, (LocationListener) this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            }
        }
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            location_list = new ArrayList<>();
            location_list.add("Location Found");
            location_list.add(latitude + "");
            location_list.add(longitude + "");

            try {
                location_list.add(addresses.get(0).getAddressLine(0) + "," + addresses.get(0).getLocality() + "," + addresses.get(0).getAdminArea() + "," + addresses.get(0).getCountryName() + "," + addresses.get(0).getPostalCode());
            } catch (Exception e) {
                location_list.add("lat : " + latitude + " , Lon :" + longitude);
            }

            location_found = true;

        } else {
            location_found = false;
        }


        if (location_found) {
            return location_list;
        } else {


            location_list = new ArrayList<>();
            location_list.add("Location Not Found");
            return location_list;

        }

    }


    //TODO calculate the distance between two location in kms
    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


}
