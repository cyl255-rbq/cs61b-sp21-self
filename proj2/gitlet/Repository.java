package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author cyl
 */
public class Repository implements Serializable {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File master = join(GITLET_DIR, "refs", "heads", "master");

    /* TODO: fill in the rest of this class. */

    private static String getHead() {
        return readContentsAsString(master);
    }

    private static String getBranchHead(String name) {
        return readContentsAsString(join(GITLET_DIR, "refs", "heads", name));
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        File heads = join(GITLET_DIR, "refs", "heads");
        heads.mkdirs();
        File commits = join(GITLET_DIR, "objects", "commits");
        commits.mkdirs();
        File blobs = join(GITLET_DIR, "objects", "commits");
        blobs.mkdir();
        File HEAD = join(GITLET_DIR, "HEAD");
        writeContents(HEAD, "ref: refs/heads/master\n");
        commit("initial commit", null); //commit ↓
    }

    public static void commit(String message) {
        commit(message, getHead());
    }

    private static void commit(String message, String parent) {
        Commit commit = new Commit(message, parent);
        if (parent == null) {
            commit.saveCommit();
            return;
        }
        testIndex("No changes added to the commit.");
        commit.changeMap(Commit.fromFile(parent).commitMap());
        StagingArea stagingArea = StagingArea.fromFile();
        Map<String, String> addition = stagingArea.addition();
        Map<String, String> removal = stagingArea.removal();
        if (addition.isEmpty() && removal.isEmpty()) {
            message("No changes added to the commit.");
            return;
        }
        for (String name : addition.keySet()) {
            commit.commitMap().put(name, addition.get(name));
        }
        for (String name : removal.keySet()) {
            commit.commitMap().remove(name);
        }
        writeContents(master, commit.saveCommit());
        stagingArea.clear();
        stagingArea.saveStagingArea();
    }

    public static void add(String name) {
        File temp = join(CWD, name);
        if (!temp.exists()) {
            message("File does not exist.");
            return;
        }
        File index = join(GITLET_DIR, "index");
        Blob tempBlob = new Blob(temp);
        String tempBlobHash = tempBlob.getHash();
        if (!index.exists()) {
            new StagingArea().saveStagingArea();
        }
        StagingArea stagingArea = StagingArea.fromFile();
        Commit now = Commit.fromFile(getHead());
        if (now.mapContains(name) && now.mapGetValue(name).equals(tempBlobHash)) {
            if (stagingArea.additionContains(name)) {
                stagingArea.stagingRemove(name, tempBlobHash);
            }
        } else {
            stagingArea.stagingAdd(name, tempBlobHash);
            tempBlob.saveBlob();
        }
        stagingArea.saveStagingArea();
    }

    public static void rm(String name) {
        testIndex("No reason to remove the file.");
        StagingArea stagingArea = StagingArea.fromFile();
        Commit now = Commit.fromFile(getHead());
        if (!stagingArea.additionContains(name) && !now.mapContains(name)) {
            message("No reason to remove the file.");
        } else {
            Blob tempBlob = new Blob(join(CWD, name));
            stagingArea.stagingRemove(name, tempBlob.getHash());
            stagingArea.saveStagingArea();
        }
    }

    private static void testIndex(String error) {
        if (!join(GITLET_DIR, "index").exists()) {
            message(error);
            System.exit(0);
        }
    }

    private static void helpLog(File nowFile, Commit now, String nowParent) {
        message("===");
        message("commit %s", nowFile.getName());
        if (now.getAnotherParent() != null) {
            message("Merge: %.7s %.7s", nowParent, now.getAnotherParent());
        }
        message(String.format(Locale.US, "Date: %1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz", now.getDate()));
        message(now.getMessage());
    }

    public static void log() {
        File nowFile = Commit.findFile(getHead());
        Commit now = Commit.fromFile(getHead());
        String nowParent = now.getParent();
        while (nowParent != null) {
            helpLog(nowFile, now, nowParent);
            nowFile = Commit.findFile(nowParent);
            now = Commit.fromFile(nowParent);
            nowParent = now.getParent();
        }
    }

    public static void globalLog() {
        File commits = join(GITLET_DIR, "objects", "commits");
        for (String eachName : plainFilenamesIn(commits)) {
            Commit now = Commit.fromFile(eachName);
            File nowFile = Commit.findFile(eachName);
            helpLog(nowFile, now, now.getParent());
        }
    }

    public static void find(String message) {
        File commits = join(GITLET_DIR, "objects", "commits");
        for (String eachName : plainFilenamesIn(commits)) {
            if (Commit.fromFile(eachName).getMessage().equals(message)) {
                message(eachName);
            }
        }
    }

    public static void status() {

    }

    public static void checkout(String[] args, int n) {
        if (n == 3) {
            checkoutFile(getHead(), args[2]);
        } else if (n == 4) {
            checkoutFile(args[1], args[3]);
        } else {
            checkoutBranch(args[1]);
        }
    }

    public static void checkoutFile(String hash, String name) {
        File checkoutFile = Commit.findFile(hash);
        if (!hash.equals(getHead()) && !checkoutFile.exists()) {
            message("No commit with that id exists.");
            return;
        }
        String checkoutBlobHash = Commit.fromFile(hash).commitMap().get(name);
        if (checkoutBlobHash == null) {
            message("File does not exist in that commit.");
            return;
        }
        byte[] checkoutBlobContend = Blob.fromFile(checkoutBlobHash).getBlobContend();
        File find = join(CWD, name);
        writeContents(find, (Object) checkoutBlobContend);
    }
    public static void checkoutBranch(String branchName) {
        File branch = Commit.findFile(branchName);
        if (!branch.exists()) {
            message("No such branch exists.");
            return;
        }
        if (readContentsAsString(branch).equals(getHead())) {
            message("No need to checkout the current branch.");
            return;
        }
        Commit branchCommit = Commit.fromFile(getBranchHead(branchName));
        Map<String, String> blobs = branchCommit.commitMap();
        StagingArea stagingArea = StagingArea.fromFile();
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!stagingArea.addition().containsKey(fileName) &&
                !stagingArea.removal().containsKey(fileName) && blobs.containsKey(fileName)) {
                message("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!branchCommit.mapContains(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }
        for (String file : blobs.keySet()) {
            writeContents(join(CWD, file), (Object) Blob.fromFile(blobs.get(file)).getBlobContend());
        }
        File HEAD = join(GITLET_DIR, "HEAD");
        writeContents(HEAD, "ref: refs/heads/%s\n", branchName);
        stagingArea.clear();
        stagingArea.saveStagingArea();
    }

}
