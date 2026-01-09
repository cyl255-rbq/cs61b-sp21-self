package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.Locale;
import java.util.Map;

import static gitlet.Utils.*;
import static gitlet.Utils.plainFilenamesIn;

/** Represents a gitlet repository.
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
    public static final File HEADS = join(GITLET_DIR, "refs", "heads");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File COMMITS = join(GITLET_DIR, "objects", "commits");
    public static final File BLOBS = join(GITLET_DIR, "objects", "blobs");
    public static final File REMOTES = join(GITLET_DIR, "remotes");


    private static String getHeadName() {
        return readContentsAsString(HEAD).substring(16).trim();
    }

    private static String getHeadHash() {
        File head = join(HEADS, getHeadName());
        return readContentsAsString(head);
    }

    private static String getBranchHead(String name) {
        return readContentsAsString(join(HEADS, name));
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        HEADS.mkdirs();
        COMMITS.mkdirs();
        BLOBS.mkdir();
        HashMap<String, String> remotes = new HashMap<>();
        writeObject(REMOTES, remotes);
        writeContents(HEAD, "ref: refs/heads/master\n");
        commit("initial commit", null, null);
        new StagingArea().saveStagingArea();
    }

    public static void commit(String message) {
        commit(message, getHeadHash(), null);
    }

    private static void commit(String message, String parent, String anotherParent) {
        Commit commit = new Commit(message, parent, anotherParent);
        if (parent == null) {
            commit.saveCommit();
            return;
        }
        testIndex("No changes added to the commit.");
        commit.changeMap(Commit.fromFile(parent).commitMap());
        StagingArea stagingArea = StagingArea.fromFile();
        Map<String, String> addition = stagingArea.addition();
        Set<String> removal = stagingArea.removal();
        if (addition.isEmpty() && removal.isEmpty()) {
            message("No changes added to the commit.");
            return;
        }
        for (String name : addition.keySet()) {
            commit.commitMap().put(name, addition.get(name));
        }
        for (String name : removal) {
            commit.commitMap().remove(name);
        }
        writeContents(join(HEADS, getHeadName()), commit.saveCommit());
        stagingArea.clear();
        stagingArea.saveStagingArea();
    }

    public static void add(String name) {
        File temp = join(CWD, name);
        if (!temp.exists()) {
            message("File does not exist.");
            return;
        }
        Blob tempBlob = new Blob(temp);
        String tempBlobHash = tempBlob.getHash();
        StagingArea stagingArea = StagingArea.fromFile();
        Commit now = Commit.fromFile(getHeadHash());
        if (now.mapContains(name) && now.mapGetValue(name).equals(tempBlobHash)) {
            if (stagingArea.removalContains(name)) {
                stagingArea.stagingAdd(name, tempBlobHash);
            } else if (stagingArea.additionContains(name)) {
                stagingArea.stagingRemove(name);
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
        Commit now = Commit.fromFile(getHeadHash());
        if (!stagingArea.additionContains(name) && !now.mapContains(name)) {
            message("No reason to remove the file.");
        } else {
            stagingArea.stagingRemove(name);
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
        String format = "Date: %1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz";
        message(String.format(Locale.US, format, now.getDate()));
        message(now.getMessage());
        System.out.println();
    }

    public static void log() {
        File nowFile = Commit.findFile(getHeadHash());
        Commit now = Commit.fromFile(getHeadHash());
        String nowParent = now.getParent();
        while (true) {
            helpLog(nowFile, now, nowParent);
            if (nowParent == null) {
                break;
            }
            nowFile = Commit.findFile(nowParent);
            now = Commit.fromFile(nowParent);
            nowParent = now.getParent();
        }
    }

    public static void globalLog() {
        for (String eachName : plainFilenamesIn(COMMITS)) {
            Commit now = Commit.fromFile(eachName);
            File nowFile = Commit.findFile(eachName);
            helpLog(nowFile, now, now.getParent());
        }
    }

    public static void find(String message) {
        boolean find = false;
        for (String eachName : plainFilenamesIn(COMMITS)) {
            if (Commit.fromFile(eachName).getMessage().equals(message)) {
                message(eachName);
                find = true;
            }
        }
        if (!find) {
            message("Found no commit with that message.");
        }
    }

    public static void status() {
        message("=== Branches ===");
        String head = readContentsAsString(HEAD).substring(16).trim();
        for (String eachName : plainFilenamesIn(HEADS)) {
            if (eachName.equals(head)) {
                System.out.print("*");
            }
            message(eachName);
        }
        System.out.println();
        message("=== Staged Files ===");
        StagingArea stagingArea = StagingArea.fromFile();
        Map<String, String> addition = stagingArea.addition();
        Set<String> removal = stagingArea.removal();
        List<String> additonList = new ArrayList<>(addition.keySet());
        List<String> removalList = new ArrayList<>(removal);
        Collections.sort(additonList);
        Collections.sort(removalList);
        for (String addName : additonList) {
            message(addName);
        }
        System.out.println();
        message("=== Removed Files ===");
        for (String removeName : removalList) {
            message(removeName);
        }
        System.out.println();
        message("=== Modifications Not Staged For Commit ===");
        List<String> modifiedNotStaged = new ArrayList<>();
        Map<String, String> blobs = Commit.fromFile(getHeadHash()).commitMap();
        for (String fileName : blobs.keySet()) {
            File file = join(CWD, fileName);
            if (addition.containsKey(fileName)) {
                continue;
            }
            boolean removalContain = removal.contains(fileName);
            String blobHash = blobs.get(fileName);
            if (!file.exists() && !removal.contains(fileName)) {
                modifiedNotStaged.add(fileName + " (deleted)");
            } else if (!removalContain && !blobHash.equals(new Blob(file).getHash())) {
                modifiedNotStaged.add(fileName + " (modified)");
            }
        }
        for (String fileName : addition.keySet()) {
            File file = join(CWD, fileName);
            if (!file.exists()) {
                modifiedNotStaged.add(fileName + " (deleted)");
            } else if (!addition.get(fileName).equals(new Blob(file).getHash())) {
                modifiedNotStaged.add(fileName + " (modified)");
            }
        }
        Collections.sort(modifiedNotStaged);
        for (String s : modifiedNotStaged) {
            message(s);
        }
        System.out.println();
        message("=== Untracked Files ===");
        for (String fileName : plainFilenamesIn(CWD)) {
            if (untracked(fileName)) {
                message(fileName);
            }
        }
    }

    public static void checkout(String[] args, int n) {
        if (n == 3) {
            checkoutFile(getHeadHash(), args[2]);
        } else if (n == 4) {
            checkoutFile(args[1], args[3]);
        } else {
            checkoutBranch(args[1]);
        }
    }

    private static File shortFind(String hash) {
        int length = hash.length();
        if (length == 40) {
            return Commit.findFile(hash);
        }
        List<String> fit = new ArrayList<>();
        for (String file : plainFilenamesIn(COMMITS)) {
            if (file.startsWith(hash)) {
                fit.add(file);
            }
        }
        if (fit.size() != 1) {
            return null;
        }
        return join(COMMITS, fit.get(0));
    }

    private static void helpCheckoutExist(String hash) {
        File checkoutFile = shortFind(hash);
        if (checkoutFile == null || !checkoutFile.exists()) {
            message("No commit with that id exists.");
            System.exit(0);
        }
    }

    public static void checkoutFile(String hash, String name) {
        helpCheckoutExist(hash);
        hash = shortFind(hash).getName();
        String checkoutBlobHash = Commit.fromFile(hash).commitMap().get(name);
        if (checkoutBlobHash == null) {
            message("File does not exist in that commit.");
            return;
        }
        byte[] checkoutBlobContend = Blob.fromFile(checkoutBlobHash).getBlobContend();
        File find = join(CWD, name);
        writeContents(find, (Object) checkoutBlobContend);
    }

    private static boolean untracked(String fileName) {
        Map<String, String> blobsCurrentCommit = Commit.fromFile(getHeadHash()).commitMap();
        StagingArea stagingArea = StagingArea.fromFile();
        boolean inCurrent = blobsCurrentCommit.containsKey(fileName);
        boolean inAdd = stagingArea.addition().containsKey(fileName);
        boolean inRm = stagingArea.removal().contains(fileName);
        return ((!inCurrent && !inAdd) || inRm);
    }

    private static void helpCheckoutBranch(String branchHash) {
        Commit branchCommit = Commit.fromFile(branchHash);
        Map<String, String> blobs = branchCommit.commitMap();
        StagingArea stagingArea = StagingArea.fromFile();
        for (String fileName : plainFilenamesIn(CWD)) {
            boolean inTarget = blobs.containsKey(fileName);
            if (inTarget && untracked(fileName)) {
                String error1 = "There is an untracked file in the way;";
                String error2 = " delete it, or add and commit it first.";
                message(error1 + error2);
                System.exit(0);
            }
        }
        Map<String, String> currentMap = Commit.fromFile(getHeadHash()).commitMap();
        for (String fileName : currentMap.keySet()) {
            if (!branchCommit.mapContains(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }
        for (String file : blobs.keySet()) {
            Object blobContend = Blob.fromFile(blobs.get(file)).getBlobContend();
            writeContents(join(CWD, file), blobContend);
        }
        stagingArea.clear();
        stagingArea.saveStagingArea();
    }

    public static void checkoutBranch(String branchName) {

        File branch = join(HEADS, branchName);
        if (!branch.exists()) {
            message("No such branch exists.");
            return;
        }
        if (branchName.equals(getHeadName())) {
            message("No need to checkout the current branch.");
            return;
        }
        helpCheckoutBranch(getBranchHead(branchName));
        writeContents(HEAD, "ref: refs/heads/", branchName, "\n");
    }

    public static void branch(String branchName) {
        File branch = join(HEADS, branchName);
        if (branch.exists()) {
            message("A branch with that name already exists.");
            return;
        }
        writeContents(branch, getHeadHash());
    }

    public static void rmBranch(String branchName) {
        File branch = join(HEADS, branchName);
        if (branchName.equals(getHeadName())) {
            message("Cannot remove the current branch.");
            return;
        }
        if (!branch.exists()) {
            message("A branch with that name does not exist.");
            return;
        }
        branch.delete();
    }

    public static void reset(String commitHash) {
        helpCheckoutExist(commitHash);
        helpCheckoutBranch(commitHash);
        writeContents(join(HEADS, getHeadName()), commitHash);
    }

    private static Set<String> helpBuildParents() {
        Set<String> parents = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(getHeadHash());
        while (!queue.isEmpty()) {
            String currentHash = queue.remove();
            if (parents.contains(currentHash)) {
                continue;
            }
            parents.add(currentHash);
            Commit current = Commit.fromFile(currentHash);
            String parent = current.getParent();
            if (parent != null) {
                queue.add(parent);
            }
            String anotherParent = current.getAnotherParent();
            if (anotherParent != null) {
                queue.add(anotherParent);
            }
        }
        return parents;
    }

    /**
     private static Set<String> helpBuildParents() {
        Set<String> parents = new HashSet<>();
        File nowFile = Commit.findFile(getHeadHash());
        Commit now = Commit.fromFile(getHeadHash());
        String nowParent = now.getParent();
        while (true) {
            parents.add(nowFile.getName());
            String anotherParent = now.getAnotherParent();
            if (anotherParent != null) {
                parents.add(anotherParent);
            }
            if (nowParent == null) {
                break;
            }
            nowFile = Commit.findFile(nowParent);
            now = Commit.fromFile(nowParent);
            nowParent = now.getParent();
        }
        return parents;
     }
     */

    private static String helpFindSplit(String branchName, Set<String> parents) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(getBranchHead(branchName));
        while (!queue.isEmpty()) {
            String branchHash = queue.remove();
            if (visited.contains(branchHash)) {
                continue;
            }
            if (parents.contains(branchHash)) {
                return branchHash;
            }
            Commit branch = Commit.fromFile(branchHash);
            String parent = branch.getParent();
            if (parent != null) {
                queue.add(parent);
            }
            String anotherParent = branch.getAnotherParent();
            if (anotherParent != null) {
                queue.add(anotherParent);
            }
            visited.add(branchHash);
        }
        return null;
    }

    private static void checkMergeFirst(String branchName) {
        StagingArea stagingArea = StagingArea.fromFile();
        if (!stagingArea.addition().isEmpty() || !stagingArea.removal().isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        }
        File target = join(HEADS, branchName);
        if (!target.exists()) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(getHeadName())) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    private static String getBlobContent(File blob) {
        return readObject(blob, Blob.class).getBlobAsText();
    }


    private static void helpFastCheckout(String branchName) {
        String branchHash = getBranchHead(branchName);
        String headName = getHeadName();
        File head = join(HEADS, headName);
        checkoutBranch(branchName);
        writeContents(head, branchHash);
        writeContents(HEAD, "ref: refs/heads/", headName, "\n");
    }

    public static void merge(String branchName) {
        checkMergeFirst(branchName);
        String branchHash = getBranchHead(branchName);
        Set<String> parents = helpBuildParents();
        String splitHash = helpFindSplit(branchName, parents);
        Commit current = Commit.fromFile(getHeadHash());
        Commit branch = Commit.fromFile(branchHash);
        Commit split = Commit.fromFile(splitHash);
        Map<String, String> currentMap = current.commitMap();
        Map<String, String> branchMap = branch.commitMap();
        Map<String, String> splitMap = split.commitMap();
        helpCheckMerge(branchMap, splitMap);
        if (branchHash.equals(splitHash)) {
            message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (getHeadHash().equals(splitHash)) {
            helpFastCheckout(branchName);
            message("Current branch fast-forwarded.");
            return;
        }
        boolean conflict = false;
        Set<String> currentSet = currentMap.keySet();
        Set<String> branchSet = branchMap.keySet();
        Set<String> splitSet = splitMap.keySet();
        for (String file : branchSet) {
            if (!splitSet.contains(file)) {
                if (!currentSet.contains(file)) {
                    checkoutFile(branchHash, file);
                    add(file);
                } else if (!currentMap.get(file).equals(branchMap.get(file))) {
                    String currentContent = getBlobContent(join(BLOBS, currentMap.get(file)));
                    String branchContent = getBlobContent(join(BLOBS, branchMap.get(file)));
                    helpConflictContent(file, currentContent, branchContent);
                    conflict = true;
                    add(file);
                }
            }
        }
        for (String file : splitSet) {
            String splitFileHash = splitMap.get(file);
            String currentFileHash = currentMap.get(file);
            String branchFileHash = branchMap.get(file);
            if (splitFileHash.equals(currentFileHash)) {
                if (!branchSet.contains(file)) {
                    rm(file);
                } else if (!splitFileHash.equals(branchFileHash)) {
                    checkoutFile(branchHash, file);
                    add(file);
                }
            } else if (!splitFileHash.equals(branchFileHash)) {
                if (branchFileHash != null) {
                    if (currentFileHash == null) {
                        String branchContent = getBlobContent(join(BLOBS, branchFileHash));
                        helpConflictContent(file, "", branchContent);
                        conflict = true;
                        add(file);
                    } else if (!branchFileHash.equals(currentFileHash)) {
                        String currentContent =  getBlobContent(join(BLOBS, currentFileHash));
                        String branchContent = getBlobContent(join(BLOBS, branchFileHash));
                        helpConflictContent(file, currentContent, branchContent);
                        conflict = true;
                        add(file);
                    }
                } else if (currentFileHash != null) {
                    String currentContent = getBlobContent(join(BLOBS, currentFileHash));
                    helpConflictContent(file, currentContent, "");
                    conflict = true;
                    add(file);
                }

            }
        }
        commit("Merged " + branchName + " into " + getHeadName() + ".", getHeadHash(), branchHash);
        if (conflict) {
            message("Encountered a merge conflict.");
        }
    }

    private static void helpCheckMerge(Map<String, String> branch, Map<String, String> split) {
        Commit current = Commit.fromFile(getHeadHash());
        Map<String, String> currentMap = current.commitMap();
        for (String fileName : plainFilenamesIn(CWD)) {
            String splitHash = split.get(fileName);
            String currentHash = currentMap.get(fileName);
            String branchHash = branch.get(fileName);
            boolean givenChanged = !isSame(splitHash, branchHash);
            boolean distinctFromCurrent = !isSame(branchHash, currentHash);
            boolean total = givenChanged && distinctFromCurrent;
            if (total && untracked(fileName)) {
                String error1 = "There is an untracked file in the way;";
                String error2 = " delete it, or add and commit it first.";
                message(error1 + error2);
                System.exit(0);
            }
        }
    }

    private static void helpConflictContent(String file, String curContent, String branchContent) {
        writeContents(join(CWD, file), "<<<<<<< HEAD\n" + curContent
                + "=======\n" + branchContent + ">>>>>>>\n");
    }

    private static boolean isSame(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    public static void addRemote(String[] args) {
        HashMap<String, String> remotes = readObject(REMOTES, HashMap.class);
        if (remotes.containsKey(args[1])) {
            message("A remote with that name already exists.");
            return;
        }
        String path = args[2].replace("/", java.io.File.separator);
        remotes.put(args[1], path);
        writeObject(REMOTES, remotes);
    }

    public static void rmRemote(String remoteName) {
        HashMap<String, String> remotes = readObject(REMOTES, HashMap.class);
        if (!remotes.containsKey(remoteName)) {
            message("A remote with that name does not exist.");
            return;
        }
        remotes.remove(remoteName);
        writeObject(REMOTES, remotes);
    }

    private static Set<String> buildPushCommits(String historyCommit) {
        Set<String> pushCommits = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(getHeadHash());
        while (!queue.isEmpty()) {
            String currentHash = queue.remove();
            if (pushCommits.contains(currentHash)) {
                continue;
            }
            pushCommits.add(currentHash);
            Commit current = Commit.fromFile(currentHash);
            String parent = current.getParent();
            String anotherParent = current.getAnotherParent();
            if (parent != null) {
                if (parent.equals(historyCommit)) {
                    pushCommits.add(historyCommit);
                } else {
                    queue.add(parent);
                }
            }
            if (anotherParent != null) {
                if (anotherParent.equals(historyCommit)) {
                    pushCommits.add(historyCommit);
                } else {
                    queue.add(anotherParent);
                }
            }
        }
        if (!pushCommits.contains(historyCommit)) {
            return null;
        }
        pushCommits.remove(historyCommit);
        return pushCommits;
    }

    private static void writeCommitsBlobs(Set<String> commitsSet, File remoteDir, boolean isFetch) {
        File sourceCommitsDir;
        File sourceBlobsDir;
        File destCommitsDir;
        File destBlobsDir;
        if (isFetch) {
            sourceCommitsDir = join(remoteDir, "objects", "commits");
            sourceBlobsDir = join(remoteDir, "objects", "blobs");
            destCommitsDir = COMMITS;
            destBlobsDir = BLOBS;
        } else {
            sourceCommitsDir = COMMITS;
            sourceBlobsDir = BLOBS;
            destCommitsDir = join(remoteDir, "objects", "commits");
            destBlobsDir = join(remoteDir, "objects", "blobs");
        }
        Set<String> visitedBlobs = new HashSet<>();
        for (String commitHash : commitsSet) {
            File destFile = join(destCommitsDir, commitHash);
            File sourceFile = join(sourceCommitsDir, commitHash);
            if (!sourceFile.exists()) {
                continue;
            }
            Commit commit = readObject(sourceFile, Commit.class);
            if (!destFile.exists()) {
                writeObject(destFile, commit);
            }
            Set<String> commitBlobs = new HashSet<>(commit.commitMap().values());
            for (String blobHash : commitBlobs) {
                if (visitedBlobs.contains(blobHash)) {
                    continue;
                }
                visitedBlobs.add(blobHash);
                File destBlobFile = join(destBlobsDir, blobHash);
                File sourceBlobFile = join(sourceBlobsDir, blobHash);
                if (!sourceBlobFile.exists()) {
                    continue;
                }
                if (!destBlobFile.exists()) {
                    Blob blob = readObject(sourceBlobFile, Blob.class);
                    writeObject(destBlobFile, blob);
                }
            }
        }
    }

    private static void remotesPathExist(String remotePath) {
        if (remotePath == null || !new File(remotePath).exists()) {
            message("Remote directory not found.");
            System.exit(0);
        }
    }

    public static void push(String remoteName, String remoteBranchName) {
        HashMap<String, String> remotes = readObject(REMOTES, HashMap.class);
        String remotePath = remotes.get(remoteName);
        remotesPathExist(remotePath);
        File remote = new File(remotePath);
        File givenBranch = join(remote, "refs", "heads", remoteBranchName);
        if (!givenBranch.exists()) {
            Set<String> parents = helpBuildParents();
            writeCommitsBlobs(parents, remote, false);
        } else {
            String branchHash = readContentsAsString(givenBranch);
            Set<String> pushCommits = buildPushCommits(branchHash);
            if (pushCommits == null) {
                message("Please pull down remote changes before pushing.");
                return;
            }
            writeCommitsBlobs(pushCommits, remote, false);
        }
        writeContents(givenBranch, getHeadHash());
    }

    private static Set<String> buildFetchCommits(File remote, String remoteHeadHash) {
        Set<String> fetchCommits = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(remoteHeadHash);
        File fetchCommitFile = join(remote, "objects", "commits");
        while (!queue.isEmpty()) {
            String currentHash = queue.remove();
            if (fetchCommits.contains(currentHash)) {
                continue;
            }
            File localFile = join(COMMITS, currentHash);
            if (localFile.exists()) {
                continue;
            }
            fetchCommits.add(currentHash);
            File fetchCommit = join(fetchCommitFile, currentHash);
            Commit current = readObject(fetchCommit, Commit.class);
            String parent = current.getParent();
            String anotherParent = current.getAnotherParent();
            if (parent != null) {
                queue.add(parent);
            }
            if (anotherParent != null) {
                queue.add(anotherParent);
            }
        }
        return fetchCommits;
    }

    public static void fetch(String remoteName, String remoteBranchName) {
        HashMap<String, String> remotes = readObject(REMOTES, HashMap.class);
        String remotePath = remotes.get(remoteName);
        remotesPathExist(remotePath);
        File remote = new File(remotePath);
        File givenBranch = join(remote, "refs", "heads", remoteBranchName);
        if (!givenBranch.exists()) {
            message("That remote does not have that branch.");
            return;
        }
        String remoteHeadHash = readContentsAsString(givenBranch);
        Set<String> fetchCommits = buildFetchCommits(remote, remoteHeadHash);
        writeCommitsBlobs(fetchCommits, remote, true);
        File currentRemote = join(HEADS, remoteName);
        if (!currentRemote.exists()) {
            currentRemote.mkdir();
        }
        File curRemoteHead = join(currentRemote, remoteBranchName);
        writeContents(curRemoteHead, remoteHeadHash);
    }

    public static void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        merge(remoteName + "/" + remoteBranchName);
    }
}
