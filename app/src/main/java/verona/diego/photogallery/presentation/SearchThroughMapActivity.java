package verona.diego.photogallery.presentation;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.novoda.merlin.NetworkStatus;
import com.novoda.merlin.registerable.bind.Bindable;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

import java.util.HashMap;
import java.util.Iterator;

import verona.diego.photogallery.R;
import verona.diego.photogallery.connectivity.display.NetworkStatusCroutonDisplayer;
import verona.diego.photogallery.connectivity.display.NetworkStatusDisplayer;
import verona.diego.photogallery.models.FlickrPhoto;
import verona.diego.photogallery.presentation.base.CheckInternetActivity;

public class SearchThroughMapActivity extends CheckInternetActivity implements OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener, Connectable, Disconnectable, Bindable {

    private GoogleMap mMap;
    double tmp_lat = 0.0;
    double tmp_lng = 0.0;

    static HashMap<Long, FlickrPhoto> list_photo; //with hash map there will be not double values

    private NetworkStatusDisplayer networkStatusDisplayer;
    private MerlinsBeard merlinsBeard;

    CameraPosition currentPosition;

    ProgressBar mProgress;
    ImageButton btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        list_photo = new HashMap<Long, FlickrPhoto>();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_through_map);

        mProgress = (ProgressBar) findViewById(R.id.progressBarGrid);
        if(mProgress != null)
            mProgress.setProgress(0);
        btn_cancel = (ImageButton) findViewById(R.id.btn_action_cancel);
        if (btn_cancel != null) {
            btn_cancel.setVisibility(View.GONE); //ProgressBar will be adapted
            btn_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Ion.getDefault(SearchThroughMapActivity.this).cancelAll(this); //cancel all pending requests
                    btn_cancel.setVisibility(View.GONE);
                    mProgress.setProgress(0);
                }

            });
        }

        // set toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_map);
        setSupportActionBar(myToolbar);
        getActionBar();
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set google maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // set smart network notices
        networkStatusDisplayer = new NetworkStatusCroutonDisplayer(this);
        merlinsBeard = MerlinsBeard.from(this);
    }

    /**
     * Ask and get photos from Flickr based on latitude and longitude
     */
    private void getPhotos(LatLng mLatLng){
        if(mLatLng == null)
            mLatLng = new LatLng(0.0,0.0);
        //get images
        Ion.getDefault(this).cancelAll(this); //cancel all pending requests
        btn_cancel.setVisibility(View.VISIBLE);
        String query_search = "https://api.flickr.com/services/rest/?method=flickr.photos.search" +
                "&api_key="+ getResources().getString(R.string.flickr_api) +
                "&lat="+mLatLng.latitude +
                "&lon="+mLatLng.longitude +
                "&radius=5" + //[km]
                "&nojsoncallback=1" +
                "&has_geo=true" + //double sanity check
                "&per_page=250" + //num of photos to get
                "&format=json";
        Log.v("query-outer", query_search);
        Future<JsonObject> json = Ion.with(this)
                .load(query_search)
                .setLogging("json1", Log.DEBUG)
                .progressBar(mProgress)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                                 @Override
                                 public void onCompleted(Exception e, JsonObject result) {
                                     //things to do when it is complete
                                     if (result != null && !result.isJsonNull()) {
                                         Log.v("json-outer", result.toString());
                                         if (result.getAsJsonObject("photos").getAsJsonArray("photo") != null && !result.getAsJsonObject("photos").getAsJsonArray("photo").isJsonNull() && result.getAsJsonObject("photos").getAsJsonArray("photo").size() > 0) {
                                             JsonArray lPhoto = result.getAsJsonObject("photos").getAsJsonArray("photo");

                                             Toast.makeText(SearchThroughMapActivity.this, "Getting " + lPhoto.size() + " photos", Toast.LENGTH_SHORT).show();

                                             for (Iterator<JsonElement> it = lPhoto.iterator(); it.hasNext(); ) {
                                                 JsonObject photo = it.next().getAsJsonObject();
                                                 if (photo.get("id") != null && !SearchThroughMapActivity.list_photo.containsKey(photo.get("id").getAsLong())) {
                                                     String query_info = "https://api.flickr.com/services/rest/?method=flickr.photos.getInfo" +//getInfo" +
                                                             "&api_key=" + getResources().getString(R.string.flickr_api) +
                                                             "&photo_id=" + photo.get("id").getAsNumber() +
                                                             "&format=json";
                                                     Log.v("query-inner", query_info);
                                                     Ion.with(SearchThroughMapActivity.this)
                                                             .load(query_info)
                                                             .asString() // Flickr guys are not funny -.-"
                                                             .setCallback(new FutureCallback<String>() {
                                                                 @Override
                                                                 public void onCompleted(Exception e, String result2) {
                                                                     if (result2 != null) {
                                                                         result2 = result2.substring(14, result2.length() - 1); //extra information
                                                                         Log.v("json-inner", result2);

                                                                         JsonParser parser = new JsonParser();
                                                                         JsonObject obj = parser.parse(result2).getAsJsonObject();

                                                                         JsonObject photo = obj.getAsJsonObject("photo");
                                                                         FlickrPhoto tmp = new FlickrPhoto(photo.get("id").getAsLong(),
                                                                                 photo.getAsJsonObject("title").get("_content").getAsString(),
                                                                                 photo.getAsJsonObject("urls").getAsJsonArray("url").get(0).getAsJsonObject().get("_content").getAsString(),
                                                                                 photo.getAsJsonObject("location").get("latitude").getAsDouble(),
                                                                                 photo.getAsJsonObject("location").get("longitude").getAsDouble());
                                                                         Log.v("received-photo", ""+tmp);
                                                                         SearchThroughMapActivity.list_photo.put(photo.get("id").getAsLong(),tmp);
                                                                         mMap.addMarker(new MarkerOptions()
                                                                                 .position(new LatLng(tmp.getLatitude(), tmp.getLongitude()))
                                                                                 .title(tmp.getTitle())
                                                                                 .snippet(String.valueOf(tmp.getId())));
                                                                         //Toast.makeText(SearchThroughMapActivity.this, "Add: " + photo.get("id").getAsLong(), Toast.LENGTH_SHORT).show();
                                                                     }
                                                                 }
                                                             });
                                                 }
                                             }
                                             //Toast.makeText(SearchThroughMapActivity.this, "count photo: " + list_photo.size(), Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                     btn_cancel.setVisibility(View.GONE);
                                     mProgress.setProgress(0);
                                }
                });
    }

    /**
     * Create a simple and smart way to notify users to the connection state
     * @return Merlin object
     */
    @Override
    protected Merlin createMerlin() {
        return new Merlin.Builder()
                .withConnectableCallbacks()
                .withDisconnectableCallbacks()
                .withBindableCallbacks()
                .withLogging(true)
                .build(this);
    }

    // ************   LISTENERS   *************

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // hybrid map
        mMap.setIndoorEnabled(false); // no indoor map
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                CameraPosition cameraP = mMap.getCameraPosition();
                LatLng tmp = new LatLng(cameraP.target.longitude,cameraP.target.latitude);
                getPhotos(tmp);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tmp, 5));
                return true;
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(SearchThroughMapActivity.this,
                    "Review app permissions", Toast.LENGTH_SHORT).show();
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.074779, 11.121749), 10));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                //Toast.makeText(SearchThroughMapActivity.this, "id: "+arg0.getSnippet(), Toast.LENGTH_SHORT).show();
                FlickrPhoto tmp = list_photo.get(Long.valueOf(arg0.getSnippet()));

                Intent i = new Intent(SearchThroughMapActivity.this, PhotoDetails.class);
                i.putExtra("id", ""+arg0.getSnippet());
                i.putExtra("title", ""+arg0.getTitle());
                i.putExtra("url", ""+tmp.getUrl());
                i.putExtra("lat", ""+tmp.getLatitude());
                i.putExtra("lng", ""+tmp.getLongitude());
                startActivity(i);

                return true;
            }
        });
    }

    @Override
    public void onCameraChange(CameraPosition position){
        if (currentPosition != position){
            getPhotos(new LatLng(position.target.latitude, position.target.longitude));
            //Toast.makeText(SearchThroughMapActivity.this,
              //      "Move", Toast.LENGTH_SHORT).show();
        }
        currentPosition = position;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerConnectable(this);
        registerDisconnectable(this);
        registerBindable(this);
    }

    @Override
    public void onBind(NetworkStatus networkStatus) {
        if (!networkStatus.isAvailable()) {
            onDisconnect();
        }
    }

    @Override
    public void onConnect() {
        networkStatusDisplayer.displayConnected();
    }

    @Override
    public void onDisconnect() {
        networkStatusDisplayer.displayDisconnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkStatusDisplayer.reset();
    }

    // ************   MENU   *************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Ion.getDefault(this).cancelAll(this); //cancel all pending requests
                this.finish();
                return true;
            case R.id.action_search_address:
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.dialog_latlng, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText eT_lat = (EditText) promptsView
                        .findViewById(R.id.in_latitude);
                final EditText eT_lng = (EditText) promptsView
                        .findViewById(R.id.in_longitude);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_go_there,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (eT_lat.getText().toString().matches("") && eT_lng.getText().toString().matches(""))
                                            Toast.makeText(SearchThroughMapActivity.this, "Invalid data", Toast.LENGTH_SHORT).show();
                                        else {
                                            LatLng tmp = new LatLng(Double.valueOf(eT_lat.getText().toString()), Double.valueOf(eT_lng.getText().toString()));
                                            getPhotos(tmp);
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tmp, 5));
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.btn_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
