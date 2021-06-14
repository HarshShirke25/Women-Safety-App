package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static android.Manifest.permission.CALL_PHONE;
import static android.location.LocationManager.GPS_PROVIDER;
import android.telephony.SmsManager;


public class MainActivity2 extends AppCompatActivity {

    public FusedLocationProviderClient client;
    public final int REQUEST_CHECK_CODE = 8989;
    DatabaseHandler myDB;
    public LocationSettingsRequest.Builder builder;
    public static final int REQUEST_LOCATION = 1;
    String x = "", y = "";

    LocationManager locationManager;
    Intent mIntent;

    Button b1, b2;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button2);
        myDB = new DatabaseHandler(this);
        client = LocationServices.getFusedLocationProviderClient(this);

        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.emergency_alert);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(GPS_PROVIDER)) {
            onGPS();
        } else {
            startTracK();
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
            }
        });
        b2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mp.start();
                Toast.makeText(getApplicationContext(), "PANIC BUTTON STARTED", Toast.LENGTH_SHORT).show();

                return false;
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    public void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    private void startTracK() {
        if(ActivityCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED
           && ActivityCompat.checkSelfPermission(MainActivity2.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);

        }else{
               client.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                double lat = location.getLatitude();
                                double lon = location.getLongitude();
                                x = String.valueOf(lat);
                                y = String.valueOf(lon);
                            }
                        }
                    });
            Location locationGPS = locationManager.getLastKnownLocation( GPS_PROVIDER);
            if(locationGPS!=null){

            }

        }
    }



    private void loadData() {

        ArrayList<String> thelist = new ArrayList<>();
        Cursor data = myDB.getListContents();
        if (data.getCount() == 0) {
            Toast.makeText(this, "no content to show", Toast.LENGTH_SHORT).show();

        } else {
            String msg = "I NEED HELP\n LATITUDE:" + x + "\nLONGITUDE:" + y+" https://www.google.com/maps/search/?api=1&query="+x+","+y;

            while (data.moveToNext()) {

                String number = "";

                thelist.add(data.getString(1));

                number = number + data.getString(1);

                call();


                if (!thelist.isEmpty()) {

                        sendSms(number, msg, true);
            }




            }
        }
    }

    private void sendSms(String number, String msg, boolean b) {

        SmsManager smsManager = SmsManager.getDefault();

        smsManager.sendTextMessage(number, null, msg, null, null);
    }



    private void call() {
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:1000"));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(i);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{CALL_PHONE}, 1);
            }
        }

    }


}