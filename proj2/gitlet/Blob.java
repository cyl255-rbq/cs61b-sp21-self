package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    private String name;
    private String hash;
    private byte[] contend;

    public Blob(File name) {
        this.contend = readContents(name);
        this.name = name.getName();
        this.hash = sha1(this.contend);
    }

    public static Blob fromFile(String name) {
        File inFile = join(GITLET_DIR, "objects", name);
        return readObject(inFile, Blob.class);
    }

    byte[] getBlobFile() {
        return this.contend;
    }

    String getBlobName() {
        return this.name;
    }

    String saveBlob() {
        File outFile = join(GITLET_DIR, "objects", this.hash);
        if (!outFile.exists()) {
            writeObject(outFile, this);
        }
        return this.hash;
    }

    String getHash() {
        return this.hash;
    }
}
