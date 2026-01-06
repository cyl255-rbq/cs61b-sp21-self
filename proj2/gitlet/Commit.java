package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author cyl
 */
public class Commit implements Serializable {
    /**
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

    public Commit(String message, String parent, String anotherParent) {
        this.message = message;
        this.parent = parent;
        this.anotherParent = anotherParent;
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

    public void changeMap(Map<String, String> otherMap) {
        this.map = new HashMap<>(otherMap);
    }

    public Map<String, String> commitMap() {
        return this.map;
    }

    public static File findFile(String hash) {
        return join(GITLET_DIR, "objects", "commits", hash);
    }

    public static Commit fromFile(String hash) {
        return readObject(findFile(hash), Commit.class);
    }

    public String saveCommit() {
        String hash = sha1((Object) serialize(this));
        File outFile = join(GITLET_DIR, "objects", "commits", hash);
        String headName = readContentsAsString(HEAD).substring(16).trim();
        File head = join(HEADS, headName);
        writeContents(head, hash);
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
