package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.join;

public class Blob implements Serializable {
    private String id;
    private byte[] contents;
    private String name;


    public Blob(File file) {
        name = file.getName();
        contents = Utils.readContents(file);
        id = Utils.sha1(file.getName(), contents);
        File blobSave = join(Main.BLOBS, id);
        Utils.writeObject(blobSave, this);
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }

    public byte[] getContents() {
        return contents;
    }
}
