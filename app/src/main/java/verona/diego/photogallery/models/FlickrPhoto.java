package verona.diego.photogallery.models;

import java.util.Date;

/**
 * This is the model for the data downloaded from Flickr
 * Created by Diego on 13/04/2016.
 */
public class FlickrPhoto {
    private long id = 0;
    private String title = "";
    private String url = "";
    private String urlThumbnail = "";
    private String urlMedium = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean shown = false;

    public FlickrPhoto(){}
    public FlickrPhoto(long id, String title){
        this.id = id;
        this.title = title;
    }
    public FlickrPhoto(long id, String title, String url, double latitude, double longitude){
        this.id = id;
        this.title = title;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    public String getUrlThumbnail() {
        return urlThumbnail;
    }

    public void setUrlThumbnail(String urlThumbnail) {
        this.urlThumbnail = urlThumbnail;
    }

    public String getUrlMedium() {
        return urlMedium;
    }

    public void setUrlMedium(String urlMedium) {
        this.urlMedium = urlMedium;
    }

    @Override
    public String toString() {
        return "id: " + this.id +
                "\n: " + this.title +
                "\nurl: " + this.url +
                "\nlatitude: " + this.latitude +
                "\nlongitude: " + this.longitude;
    }
}
