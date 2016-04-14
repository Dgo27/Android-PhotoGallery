package verona.diego.photogallery.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

import verona.diego.photogallery.R;
import verona.diego.photogallery.models.SquaredImageView;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

/**
 * Created by Diego on 14/04/2016.
 */
public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> urls = new ArrayList<String>();

    public GridViewAdapter(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        String url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .placeholder(R.drawable.placeholder) //
                .error(R.drawable.error) //
                .fit() //
                .tag(context) //
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}
