package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class StagingArea implements Serializable {

    private Map<String, String> additon;
    private Set<String> removal;

    public StagingArea() {
        additon = new HashMap<>();
        removal = new HashSet<>();
    }

    public Map<String, String> addition() {
        return this.additon;
    }

    public Set<String> removal() {
        return this.removal;
    }

    public boolean additionContains(String name) {
        return additon.containsKey(name);
    }

    public boolean removalContains(String name) {
        return removal.contains(name);
    }

    public void stagingAdd(String name, String hash) {
        if (removal.contains(name)) {
            removal.remove(name);
        } else {
            additon.put(name, hash);
        }
    }

    public void stagingRemove(String name) {
        if (additon.containsKey(name)) {
            additon.remove(name);
        } else {
            removal.add(name);
            restrictedDelete(name);
        }
    }

    public void saveStagingArea() {
        File outFile = join(GITLET_DIR, "index");
        writeObject(outFile, this);
    }

    public static StagingArea fromFile() {
        File inFile = join(GITLET_DIR, "index");
        return readObject(inFile, StagingArea.class);
    }

    public void clear() {
        additon.clear();
        removal.clear();
    }
}
