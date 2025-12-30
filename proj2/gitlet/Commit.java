package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author cyl
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    Map<String, String> map = new HashMap<>();

    /** The message of this Commit. */
    private String message;
    private String parent;
    private Instant timestamp;
    private String hash;
    /* TODO: fill in the rest of this class. */

    public void changeMap(Map<String, String> map) {
        this.map = new HashMap<>(map);
    }

    public Map<String, String> commitMap() {
        return this.map;
    }

    public Commit(String message, String parent){
        this.message = message;
        this.parent = parent;
        if (this.parent == null) {
            timestamp = Instant.EPOCH;
        } else {
            timestamp = Instant.now();
        }
        this.hash = sha1(serialize(this));
    }

    public static Commit fromFile(String name) {
        File inFile = join(GITLET_DIR, "objects", name);
        return readObject(inFile, Commit.class);
    }

    public String saveCommit() {
        File outFile = join(GITLET_DIR, "objects", this.hash);
        File master = join(GITLET_DIR, "refs", "heads", "master");
        writeContents(master, hash);
        writeObject(outFile, this);
        return this.hash;
    }

    public boolean mapContains(String name) {
        return map.containsKey(name);
    }

    public String mapGetValue(String name) {
        return map.get(name);
    }
}
