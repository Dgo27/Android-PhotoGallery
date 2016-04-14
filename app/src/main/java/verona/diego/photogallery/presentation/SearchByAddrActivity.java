package verona.diego.photogallery.presentation;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import verona.diego.photogallery.R;
import verona.diego.photogallery.connectivity.display.NetworkStatusCroutonDisplayer;
import verona.diego.photogallery.connectivity.display.NetworkStatusDisplayer;
import verona.diego.photogallery.adapter.GridViewAdapter;
import verona.diego.photogallery.adapter.ScrollListener;
import verona.diego.photogallery.models.FlickrPhoto;
import verona.diego.photogallery.presentation.base.CheckInternetActivity;

public class SearchByAddrActivity extends CheckInternetActivity implements Connectable, Disconnectable, Bindable {
    LatLng cLatLng = new LatLng(46.074779, 11.121749); //Trento
    TextView text_coords;

    GridView gv;
    GridViewAdapter adapter = null;

    ProgressBar mProgress;
    ImageButton btn_cancel;

    HashMap<Long,FlickrPhoto> hTmp = new HashMap<>();
    List<FlickrPhoto> photos = new ArrayList<>();

    private NetworkStatusDisplayer networkStatusDisplayer;
    private MerlinsBeard merlinsBeard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_addr);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_map);
        setSupportActionBar(myToolbar);
        getActionBar();
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgress = (ProgressBar) findViewById(R.id.progressBarGrid);
        if(mProgress != null)
            mProgress.setProgress(0);
        btn_cancel = (ImageButton) findViewById(R.id.btn_action_cancel);
        if (btn_cancel != null) {
            btn_cancel.setVisibility(View.GONE); //ProgressBar will be adapted
            btn_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Ion.getDefault(SearchByAddrActivity.this).cancelAll(this); //cancel all pending requests
                    btn_cancel.setVisibility(View.GONE);
                    mProgress.setProgress(0);
                }

            });
        }

        text_coords = (TextView)  findViewById(R.id.text_coords);
        if(text_coords != null)
            text_coords.setText("\n"+cLatLng+"\n");

        networkStatusDisplayer = new NetworkStatusCroutonDisplayer(this);
        merlinsBeard = MerlinsBeard.from(this);

        gv = (GridView) findViewById(R.id.grid_view);
        adapter = new GridViewAdapter(SearchByAddrActivity.this, photos);
        gv.setAdapter(adapter);
        gv.setOnScrollListener(new ScrollListener(this));

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                FlickrPhoto photo = (FlickrPhoto) parent.getItemAtPosition(position);

                Intent i = new Intent(SearchByAddrActivity.this, PhotoDetails.class);
                i.putExtra("id", ""+photo.getId());
                i.putExtra("title", ""+photo.getTitle());
                i.putExtra("url", ""+photo.getUrl());
                i.putExtra("lat", ""+photo.getLatitude());
                i.putExtra("lng", ""+photo.getLongitude());
                i.putExtra("urlPhoto", ""+photo.getUrlMedium());
                startActivity(i);
            }
        });

        updateGallery(cLatLng);
    }

    /**
     * This function asks to Ficklr news photos according to LatLng value
     * @param mLatLng
     */
    private void updateGallery(LatLng mLatLng) {
        hTmp = null;
        hTmp = new HashMap<>();
        Ion.getDefault(SearchByAddrActivity.this).cancelAll(this);
        photos.clear(); // remove older
        btn_cancel.setVisibility(View.VISIBLE);
        if(mLatLng == null)
            mLatLng = new LatLng(0.0,0.0);
        //get images
        Ion.getDefault(this).cancelAll(this); //cancel all pending requests
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

                                Toast.makeText(SearchByAddrActivity.this, "Getting " + lPhoto.size() + " photos", Toast.LENGTH_SHORT).show();

                                for (Iterator<JsonElement> it = lPhoto.iterator(); it.hasNext(); ) {
                                    JsonObject photo = it.next().getAsJsonObject();
                                    if (photo.get("id") != null) {
                                        hTmp.put(photo.get("id").getAsLong(), new FlickrPhoto(photo.get("id").getAsLong(), ""));
                                        //get general info
                                        String query_info = "https://api.flickr.com/services/rest/?method=flickr.photos.getInfo" +//getInfo" +
                                                "&api_key=" + getResources().getString(R.string.flickr_api) +
                                                "&photo_id=" + photo.get("id").getAsNumber() +
                                                "&format=json";
                                        Log.v("query-inner", query_info);
                                        Ion.with(SearchByAddrActivity.this)
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
                                                            hTmp.get(photo.get("id").getAsLong()).setTitle(photo.getAsJsonObject("title").get("_content").getAsString());
                                                            hTmp.get(photo.get("id").getAsLong()).setUrl(photo.getAsJsonObject("urls").getAsJsonArray("url").get(0).getAsJsonObject().get("_content").getAsString());
                                                            hTmp.get(photo.get("id").getAsLong()).setLatitude(photo.getAsJsonObject("location").get("latitude").getAsDouble());
                                                            hTmp.get(photo.get("id").getAsLong()).setLongitude(photo.getAsJsonObject("location").get("longitude").getAsDouble());
                                                        }
                                                    }
                                                });
                                        //get photo sizes
                                        String query_image = "https://api.flickr.com/services/rest/?method=flickr.photos.getSizes" +
                                                "&api_key=" + getResources().getString(R.string.flickr_api) +
                                                "&photo_id=" + photo.get("id").getAsNumber() +
                                                "&format=json";
                                        Log.v("query-sizes", query_image);
                                        Ion.with(SearchByAddrActivity.this)
                                                .load(query_image)
                                                .asString()
                                                .setCallback(new FutureCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String result) {

                                                        if (result != null) {
                                                            result = result.substring(14, result.length() - 1); //extra information
                                                            Log.v("result-sizes", "" + result);

                                                            JsonParser parser = new JsonParser();
                                                            JsonObject obj = parser.parse(result).getAsJsonObject();

                                                            JsonArray size = obj.getAsJsonObject("sizes").getAsJsonArray("size");
                                                            //Toast.makeText(PhotoDetails.this, ""+size, Toast.LENGTH_LONG).show();
                                                            Long idT = null;
                                                            for (JsonElement s : size) {
                                                                JsonObject ph = (JsonObject) s;
                                                                //get ID user from url
                                                                idT = Long.valueOf(ph.get("source").getAsString().substring(36,47));
                                                                if (ph.get("label").getAsString().equals("Medium")) {
                                                                    Log.v("url-img", ph.get("source").getAsString());
                                                                    hTmp.get(idT).setUrlThumbnail(ph.get("source").getAsString());
                                                                }
                                                                if (ph.get("label").getAsString().equals("Large")) {
                                                                    Log.v("url-img", ph.get("source").getAsString());
                                                                    hTmp.get(idT).setUrlMedium(ph.get("source").getAsString());
                                                                }
                                                            }
                                                            photos.add(hTmp.get(idT));
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                        //Toast.makeText(SearchByAddrActivity.this, "num photo: " + photos.size(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        mProgress.setProgress(0);
                        btn_cancel.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected Merlin createMerlin() {
        return new Merlin.Builder()
                .withConnectableCallbacks()
                .withDisconnectableCallbacks()
                .withBindableCallbacks()
                .withLogging(true)
                .build(this);
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
                                            Toast.makeText(SearchByAddrActivity.this, "Invalid data", Toast.LENGTH_SHORT).show();
                                        else {
                                            LatLng tmp = new LatLng(Double.valueOf(eT_lat.getText().toString()), Double.valueOf(eT_lng.getText().toString()));
                                            SearchByAddrActivity.this.text_coords.setText(""+tmp);
                                            updateGallery(tmp);
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
