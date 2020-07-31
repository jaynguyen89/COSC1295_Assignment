package cosc1295.providers.bases;

import cosc1295.designs.Flasher;

import java.util.List;

public class BinaryFileServiceBase<T> {

    private final Flasher flasher = Flasher.getInstance();

    private final String ASSET_PATH = System.getProperty("user.dir") + "\\src\\assets\\texts";

    public List<T> readDataFromFile(T type) {
        //TODO
        return null;
    }
}
