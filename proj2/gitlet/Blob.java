package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    private File name;
    private String hash;

    public Blob(File name) {
        this.name = name;
        this.hash = sha1(serialize(this));
    }

    String saveBlob() {
        File outFile = join(GITLET_DIR, "objects", this.hash);
        writeObject(outFile, this);
        return this.hash;
    }

    String getHash() {
        return this.hash;
    }
}
