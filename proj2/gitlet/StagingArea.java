package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class StagingArea implements Serializable {

    private Map<String, String> additon;
    private Map<String, String> removal;

    public StagingArea() {
        additon = new HashMap<>();
        removal = new HashMap<>();
    }

    public Map<String, String> addition() {
        return this.additon;
    }

    public Map<String, String> removal() {
        return this.removal;
    }

    public boolean additionContains(String name) {
        return additon.containsKey(name);
    }

    public void stagingAdd(String name, String hash) {
        if (removal.containsKey(name)) {
            removal.remove(name);
        } else {
            additon.put(name,hash);
        }
    }

    public void stagingRemove(String name, String hash) {
        if (additon.containsKey(name)) {
            additon.remove(name);
        } else {
            removal.put(name, hash);
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

    public void clear(){
        additon.clear();
        removal.clear();
    }
}
