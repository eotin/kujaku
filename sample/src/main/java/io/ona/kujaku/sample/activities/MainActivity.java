package io.ona.kujaku.sample.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.helpers.MapBoxWebServiceApi;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.utils.Constants;

public class MainActivity extends AppCompatActivity {

    private EditText topLeftLatEd
            , topLeftLngEd
            , bottomRightLatEd
            , bottomRightLngEd
            , mapNameEd;

    private static final String SAMPLE_JSON_FILE_NAME = "2017-nov-27-kujaku-metadata.json";
    private static final int PERMISSIONS_REQUEST_CODE = 9823;
    private String[] basicPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //callLibrary();

        requestBasicPermissions();

        bottomRightLatEd = (EditText) findViewById(R.id.edt_mainActivity_bottomRightlatitude);
        bottomRightLngEd = (EditText) findViewById(R.id.edt_mainActivity_bottomRightlongitude);
        topLeftLatEd = (EditText) findViewById(R.id.edt_mainActivity_topLeftlatitude);
        topLeftLngEd = (EditText) findViewById(R.id.edt_mainActivity_topLeftlongitude);

        mapNameEd = (EditText) findViewById(R.id.edt_mainActivity_mapName) ;

        Button startOfflineDownload = (Button) findViewById(R.id.btn_mainActivity_startOfflineDownload);
        Button openMapActivity = (Button) findViewById(R.id.btn_mainActivity_openMapActivity);


        startOfflineDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMap();
            }
        });
        openMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLibrary();
            }
        });
        registerLocalBroadcastReceiver();

        Button launchKujakuMap = (Button) findViewById(R.id.btn_mainActivity_launchKujakuMap);
        launchKujakuMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLibrary();
            }
        });

        final EditText mapBoxStyleUrl = (EditText) findViewById(R.id.edt_mainActivity_mapboxStyleURL);
        mapBoxStyleUrl.setText("mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
        Button downloadMapBoxStyle = (Button) findViewById(R.id.btn_mainActivity_downloadMapboxStyle);
        downloadMapBoxStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMapBoxStyle(mapBoxStyleUrl.getText().toString());
            }
        });
    }

    private void callLibrary() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                "file:///sdcard/Dukto/2017-nov-27-kujaku-metadata.json"
        });
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        LatLng bottomRight = new LatLng(
                -17.854564,
                25.854782
        );

        LatLng topLeft = new LatLng(
                        -17.875469,
                        25.876589
                );

        intent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, bottomRight);
        intent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, topLeft);
        intent.putExtra(Constants.PARCELABLE_KEY_CAMERA_TILT, 80.0);
        intent.putExtra(Constants.PARCELABLE_KEY_CAMERA_BEARING, 34.33);
        intent.putExtra(Constants.PARCELABLE_KEY_CAMERA_ZOOM, 13.6);

        startActivity(intent);
    }

    private void downloadMap() {
        double topLeftLat = -1.29020515
                , topLeftLng = 36.78702772
                , bottomRightLat = -1.29351951
                , bottomRightLng =  36.79288566;

        String tllatE = topLeftLatEd.getText().toString();
        String tllngE = topLeftLngEd.getText().toString();
        String brlatE = bottomRightLatEd.getText().toString();
        String brlngE = bottomRightLngEd.getText().toString();

        String mapName = mapNameEd.getText().toString();
        if (mapName.isEmpty()) {
            Toast.makeText(this, "Please enter a Map Name!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (isValidDouble(tllatE) && isValidDouble(tllngE) && isValidDouble(brlatE) && isValidDouble(brlngE) ) {
            topLeftLat = Double.valueOf(tllatE);
            topLeftLng = Double.valueOf(tllngE);
            bottomRightLat = Double.valueOf(brlatE);
            bottomRightLng = Double.valueOf(brlngE);

            Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "pk.eyJ1Ijoib25hIiwiYSI6IlVYbkdyclkifQ.0Bz-QOOXZZK01dq4MuMImQ");
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);//"Hp Invent " + UUID.randomUUID().toString());
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 20.0);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0.0);
            /*mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(37.7897, -119.5073));//new LatLngParcelable(-1.29020515, 36.78702772)); //new LatLngParcelable(-1.2920646, 36.7846043));
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(37.6744, -119.6815));//new LatLngParcelable(-1.29351951, 36.79288566));//new LatLngParcelable(-2.2920646, 38.7846043));*/
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(topLeftLat, topLeftLng)); //new LatLngParcelable(-1.2920646, 36.7846043));
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(bottomRightLat, bottomRightLng));//new LatLngParcelable(-2.2920646, 38.7846043));

            startService(mapDownloadIntent);
        } else {
            Toast.makeText(this, "Invalid Lat or Lng!", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private boolean isValidDouble(String doubleString) {
        String doubleRegex = "[+-]{0,1}[0-9]*.{0,1}[0-9]*";
        if (!doubleString.isEmpty() && doubleString.matches(doubleRegex)) {
            return true;
        }

        return false;
    }

    private void registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.i("KUJAKU SAMPLE APP TAG", intent.getExtras().toString());
                    }
                }, new IntentFilter(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES));
    }
    private void downloadMapBoxStyle(String mapboxStyleUrl) {
        MapBoxWebServiceApi mapBoxWebServiceApi = new MapBoxWebServiceApi(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        mapBoxWebServiceApi.retrieveStyleJSON(mapboxStyleUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error downloading MapBox Style JSON : " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void confirmSampleStyleAvailable() {
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String style = mapBoxStyleStorage.readStyle("file:///sdcard/Dukto/2017-nov-27-kujaku-metadata.json");
        if (TextUtils.isEmpty(style)) {
            //Write the file to storage
            String sampleStyleString = readAssetFile(SAMPLE_JSON_FILE_NAME);
            mapBoxStyleStorage.writeToFile("Dukto", SAMPLE_JSON_FILE_NAME,  sampleStyleString);
        }
    }

    public String readAssetFile(String inFile) {
        String tContents = "";

        try {
            InputStream stream = getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;

    }

    private void requestBasicPermissions() {
        ArrayList<String> notGivenPermissions = new ArrayList<>();

        for (String permission : basicPermissions) {
            if (!Permissions.check(this, permission)) {
                notGivenPermissions.add(permission);
            }
        }

        if (notGivenPermissions.size() > 0) {
            Permissions.request(this, notGivenPermissions.toArray(new String[notGivenPermissions.size()]), PERMISSIONS_REQUEST_CODE);
        } else {
            confirmSampleStyleAvailable();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            requestBasicPermissions();
        }
    }
}
