package verona.diego.photogallery.connectivity.display;

public interface NetworkStatusDisplayer {
    void displayConnected();

    void displayDisconnected();

    void reset();
}

