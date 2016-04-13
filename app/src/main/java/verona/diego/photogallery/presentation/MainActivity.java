package verona.diego.photogallery.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.novoda.merlin.NetworkStatus;
import verona.diego.photogallery.R;
import verona.diego.photogallery.connectivity.display.NetworkStatusCroutonDisplayer;
import verona.diego.photogallery.connectivity.display.NetworkStatusDisplayer;
import verona.diego.photogallery.presentation.base.CheckInternetActivity;
import com.novoda.merlin.registerable.bind.Bindable;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

public class MainActivity extends CheckInternetActivity implements Connectable, Disconnectable, Bindable {

    private Toolbar myToolbar;
    private ImageButton btn_search_by_address;
    private ImageButton btn_search_through_map;
    private NetworkStatusDisplayer networkStatusDisplayer;
    private MerlinsBeard merlinsBeard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
        getSupportActionBar();

        addListenerOnButton();

        networkStatusDisplayer = new NetworkStatusCroutonDisplayer(this);
        merlinsBeard = MerlinsBeard.from(this);
    }

    public void addListenerOnButton() {

        btn_search_by_address = (ImageButton) findViewById(R.id.action_search_by_address);
        btn_search_through_map = (ImageButton) findViewById(R.id.action_search_through_map);

        btn_search_by_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (merlinsBeard.isConnected()) {
                    networkStatusDisplayer.displayConnected();
                } else {
                    networkStatusDisplayer.displayDisconnected();
                }
            }
        });
        btn_search_through_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (merlinsBeard.isConnected()) {
                    startActivity(new Intent(MainActivity.this,SearchThroughMapActivity.class));
                } else {
                    networkStatusDisplayer.displayDisconnected();
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
}
