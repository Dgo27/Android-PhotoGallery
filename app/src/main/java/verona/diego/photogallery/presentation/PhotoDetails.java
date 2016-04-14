package verona.diego.photogallery.presentation;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.novoda.merlin.NetworkStatus;
import com.novoda.merlin.registerable.bind.Bindable;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

import verona.diego.photogallery.R;
import verona.diego.photogallery.connectivity.display.NetworkStatusCroutonDisplayer;
import verona.diego.photogallery.connectivity.display.NetworkStatusDisplayer;
import verona.diego.photogallery.models.FlickrPhoto;
import verona.diego.photogallery.presentation.base.CheckInternetActivity;

/**
 * This activity is used both from search through map and from searchByAddr and it shows
 * complete photo information
 */
public class PhotoDetails extends CheckInternetActivity implements Connectable, Disconnectable, Bindable {

    private Toolbar myToolbar;
    private NetworkStatusDisplayer networkStatusDisplayer;
    private MerlinsBeard merlinsBeard;

    private FlickrPhoto fp;
    private ImageView image;
    private long id;
    private String descr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);

        image = (ImageView) findViewById(R.id.photoView);

        TextView text = (TextView)  findViewById(R.id.text_image_description);
        descr = "\nTitle: " + getIntent().getExtras().getString("title") +
                "\nFlickr url: " + getIntent().getExtras().getString("url") +
                "\nLatitude: " + getIntent().getExtras().getString("lat") +
                "\nLongitude: " + getIntent().getExtras().getString("lng") +
                "\n\n";
        if(text != null)
            text.setText(descr);

        myToolbar = (Toolbar) findViewById(R.id.toolbar_photo_detail);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        networkStatusDisplayer = new NetworkStatusCroutonDisplayer(this);
        merlinsBeard = MerlinsBeard.from(this);

        id = Long.valueOf(getIntent().getExtras().getString("id"));

        if(getIntent().getExtras().getString("urlPhoto") != null){ //called from searchAddr
            Ion.with(image)
                    //.placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_action_cancel)
                    .load(getIntent().getExtras().getString("urlPhoto"))
                    .setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                            if(findViewById(R.id.loadingPanel) != null)
                                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }
                    });
        }else { //called from map
            printPhoto(id);
        }

    }

    public void printPhoto(long id){

        String query_image = "https://api.flickr.com/services/rest/?method=flickr.photos.getSizes" +
                "&api_key=" + getResources().getString(R.string.flickr_api) +
                "&photo_id=" + id +
                "&format=json";
        Log.v("query-sizes", query_image);
        Ion.with(this)
                .load(query_image)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (result != null) {
                            result = result.substring(14, result.length() - 1); //extra information
                            Log.v("result-sizes", ""+result);

                            JsonParser parser = new JsonParser();
                            JsonObject obj = parser.parse(result).getAsJsonObject();

                            JsonArray size = obj.getAsJsonObject("sizes").getAsJsonArray("size");
                            //Toast.makeText(PhotoDetails.this, ""+size, Toast.LENGTH_LONG).show();
                            for( JsonElement s : size ) {
                                JsonObject ph = (JsonObject)s;
                                if(ph.get("label").getAsString().equals("Large")){
                                    Log.v("url-img", ph.get("source").getAsString());
                                    Ion.with(image)
                                            //.placeholder(R.drawable.placeholder_image)
                                            .error(R.drawable.ic_action_cancel)
                                            .load(ph.get("source").getAsString())
                                            .setCallback(new FutureCallback<ImageView>() {
                                                @Override
                                                public void onCompleted(Exception e, ImageView result) {
                                                    if(findViewById(R.id.loadingPanel) != null)
                                                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                                }
                                            });
                                    break;
                                }
                            }
                        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
