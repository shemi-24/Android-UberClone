package com.js.androidmartialarts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private Button btngetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap mMap;
    ListView listView;
    ArrayList<String> nearByDriverRequests;
    ArrayAdapter adapter;
    private ArrayList<Double> passengersLatitude;
    private ArrayList<Double> passengersLongitude;
    private ArrayList<String> requestCarUsernames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        listView = findViewById(R.id.requestListView);
        nearByDriverRequests = new ArrayList<>();
        passengersLatitude = new ArrayList<>();
        passengersLongitude = new ArrayList<>();
        requestCarUsernames=new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriverRequests);

        listView.setAdapter(adapter);

        nearByDriverRequests.clear();


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

//            try{
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
//            } catch (Exception e){
//                e.printStackTrace();
//            }
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    if (ActivityCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            };

        }


        btngetRequests = findViewById(R.id.btnGetRequest);
        btngetRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (Build.VERSION.SDK_INT < 23) {

                    if (ActivityCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateRequestListView(currentDriverLocation);
                } else if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                    } else {
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        updateRequestListView(currentDriverLocation);
                    }
                }
            }


        });

        listView.setOnItemClickListener(this);
    }

    private void updateRequestListView(Location location) {

        if (location != null) {

            ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.whereDoesNotExist("driverOfMe");
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {
                        if (objects.size() > 0) {
                            if (nearByDriverRequests.size() > 0) {
                                nearByDriverRequests.clear();
                            }
                            if (passengersLatitude.size() > 0) {
                                passengersLatitude.clear();
                            }
                            if (passengersLongitude.size() > 0) {
                                passengersLongitude.clear();
                            }
                            if(requestCarUsernames.size()>0){
                                requestCarUsernames.clear();
                            }
                            for (ParseObject nearRequest : objects) {

                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");

                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);
                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10);
                                nearByDriverRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));

                                passengersLatitude.add(pLocation.getLatitude());
                                passengersLongitude.add(pLocation.getLongitude());
                                requestCarUsernames.add(nearRequest.get("username").toString());
                            }


                        } else {
                            Toast.makeText(DriverRequestListActivity.this, "Sorry,No request is found..", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();

                    }


                }
            });


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.driverLogoutItem) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//        Toast.makeText(this, "Lists tapped", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(cdLocation!=null){
            Intent intent=new Intent(this,ViewLocatioMapActivity.class);
            intent.putExtra("dLatitude",cdLocation.getLatitude());
            intent.putExtra("dLongitude",cdLocation.getLongitude());
            intent.putExtra("pLatitude",passengersLatitude.get(position));
            intent.putExtra("pLongitude",passengersLongitude.get(position));
            intent.putExtra("pUsername",requestCarUsernames.get(position));
            startActivity(intent);
        }


    }
}