package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
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
    private String anotherParent;
    private Date timestamp;
    /* TODO: fill in the rest of this class. */

    public Commit(String message, String parent){
        this.message = message;
        this.parent = parent;
        if (this.parent == null) {
            timestamp = new Date(0);
        } else {
            timestamp = new Date();
        }
    }

    public String getAnotherParent() {
        return this.anotherParent;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getDate() {
        return this.timestamp;
    }

    public String getParent() {
        return this.parent;
    }

    public void changeMap(Map<String, String> map) {
        this.map = new HashMap<>(map);
    }

    public Map<String, String> commitMap() {
        return this.map;
    }

    public static File findFile(String hash) {
        return join(GITLET_DIR, "objects", "commits", hash);
    }

    public static Commit fromFile(String name) {
        return readObject(findFile(name), Commit.class);
    }

    public String saveCommit() {
        String hash = sha1(serialize(this));
        File outFile = join(GITLET_DIR, "objects", "commits", hash);
        File master = join(GITLET_DIR, "refs", "heads", "master");
        writeContents(master, hash);
        writeObject(outFile, this);
        return hash;
    }

    public boolean mapContains(String name) {
        return map.containsKey(name);
    }

    public String mapGetValue(String name) {
        return map.get(name);
    }
}
