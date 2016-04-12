package verona.diego.photogallery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ImageButton btn_search_by_address;
    ImageButton btn_search_by_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
        getSupportActionBar();

        addListenerOnButton();
    }

    public void addListenerOnButton() {

        btn_search_by_address = (ImageButton) findViewById(R.id.action_search_by_address);
        btn_search_by_map = (ImageButton) findViewById(R.id.action_search_by_map);

        btn_search_by_address.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Toast.makeText(MainActivity.this,
                        "Click! ahah", Toast.LENGTH_SHORT).show();
            }

        });
        btn_search_by_map.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(MainActivity.this,SearchByMapActivity.class));
            }

        });
    }
}
