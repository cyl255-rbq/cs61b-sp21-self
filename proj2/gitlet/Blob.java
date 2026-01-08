package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static gitlet.Repository.BLOBS;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    private String hash;
    private byte[] contend;

    public Blob(File name) {
        this.contend = readContents(name);
        this.hash = sha1(this.contend);
    }

    public static Blob fromFile(String name) {
        File inFile = join(BLOBS, name);
        return readObject(inFile, Blob.class);
    }

    byte[] getBlobContend() {
        return this.contend;
    }

    String getBlobAsText() {
        return new String(this.contend, StandardCharsets.UTF_8);
    }

    String saveBlob() {
        File outFile = join(BLOBS, this.hash);
        if (!outFile.exists()) {
            writeObject(outFile, this);
        }
        return this.hash;
    }

    String getHash() {
        return this.hash;
    }
}
