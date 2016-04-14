package verona.diego.photogallery.models;

import java.util.Date;

/**
 * Created by Diego on 13/04/2016.
 */
public class FlickrPhoto {
    private int id = 0;
    private String title = "";
    private String url = "";
    private double latitude = 0.0;
    private double longitude = 0.0;

    public FlickrPhoto(int id, String title){
        this.id = id;
        this.title = title;
    }

    public FlickrPhoto(int id, String title, String url, double latitude, double longitude){
        this.id = id;
        this.title = title;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
