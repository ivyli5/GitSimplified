package gitlet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system. */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    static final File COMMITS = join(Repository.GITLET_DIR, "commits");

    static final File BLOBS = join(Repository.GITLET_DIR, "blobs");
    public static void main(String[] args) {
        String firstArg = initialStuff(args);
        switch (firstArg) {
            case "init":
                argsChecker(args, 1);
                createRepo();
                break;
            case "add":
                argsChecker(args, 2);
                add(args[1]);
                break;
            case "commit":
                argsChecker(args, 2);
                commit(args[1]);
                break;
            case "log":
                argsChecker(args, 1);
                log();
                break;
            case "global-log":
                argsChecker(args, 1);
                globalLog();
                break;
            case "rm":
                argsChecker(args, 2);
                remove(args[1]);
                break;
            case "find":
                argsChecker(args, 2);
                find(args[1]);
                break;
            case "status":
                argsChecker(args, 1);
                status();
                break;
            case "restore":
                if (args.length != 4 && args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                restore(args);
                break;
            case "branch":
                argsChecker(args, 2);
                branch(args[1]);
                break;
            case "switch":
                argsChecker(args, 2);
                switchBranch(args[1]);
                break;
            case "rm-branch":
                argsChecker(args, 2);
                removeBranch(args[1]);
                break;
            case "reset":
                argsChecker(args, 2);
                reset(args[1]);
                break;
            case "merge":
                argsChecker(args, 2);
                merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static String initialStuff(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        return firstArg;
    }

    public static void argsChecker(String[] args, int num) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void createRepo() {
        if (join(Repository.GITLET_DIR, "repo").exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        new Repository();
        COMMITS.mkdir();
        BLOBS.mkdir();
        Commit currCommit = new Commit("initial commit");
        new BranchTree(currCommit);
        Utils.writeObject(join(Repository.GITLET_DIR, "currCommit"), currCommit);
    }

    public static void add(String toAddName) {
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        HashMap<String, Blob> blobMap = new HashMap<String, Blob>();
        File toAdd = new File(toAddName);
        if (!toAdd.exists()) {
            if (currCommit.hasFile(toAddName)) {
                repo.removeAdd(toAddName);
                return;
            }
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob addBlob = new Blob(toAdd);
        if (currCommit.hasFile(toAddName)) {
            Blob currBlob = idToBlob(currCommit.getBlobID(toAddName), blobMap);
            if (Arrays.equals(currBlob.getContents(), (addBlob.getContents()))) {
                repo.removeBoth(toAddName);
            } else {
                repo.add(addBlob);
            }
        } else {
            repo.add(addBlob);
        }
    }

    public static void remove(String fileName) {
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        if (!repo.removeAdd(fileName)) {
            //File toRemove = new File(fileName);
            if (currCommit.hasFile(fileName)) {
                repo.addRemove(fileName);
            } else {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
            if (join(Repository.CWD, fileName).exists()) {
                Utils.restrictedDelete(fileName);
            }
        }
    }

    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        if (repo.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        currCommit = new Commit(message, currCommit);
        branchTree.setCurrHead(currCommit.getID());
        Utils.writeObject(join(Repository.GITLET_DIR, "currCommit"), currCommit);
    }

    //Work on second parent
    public static void commit(String message, Commit secondParent, String secondBranch) {
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        if (repo.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        currCommit = new Commit(message, currCommit, secondParent);
        branchTree.setCurrHead(currCommit.getID());
        branchTree.setSecondBranch(secondBranch, currCommit.getID());
        Utils.writeObject(join(Repository.GITLET_DIR, "currCommit"), currCommit);
    }

    public static void log() {
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        HashMap<String, Commit> commitMap = new HashMap<String, Commit>();
        Commit curr = currCommit;
        while (curr != null) {
            curr.print();
            if (curr.getParentID() == null) {
                curr = null;
            } else {
                curr = idToCommit(curr.getParentID(), commitMap);
            }
        }
    }

    public static void globalLog() {
        for (String fileName : Utils.plainFilenamesIn(COMMITS)) {
            if (fileName.length() > 6) {
                File file = join(COMMITS, fileName);
                Commit curr = Utils.readObject(file, Commit.class);
                curr.print();
            }
        }
    }

    public static void find(String message) {
        int cnt = 0;
        for (String fileName : Utils.plainFilenamesIn(COMMITS)) {
            if (fileName.length() > 6) {
                File file = join(COMMITS, fileName);
                Commit curr = Utils.readObject(file, Commit.class);
                if (curr.getMessage().equals(message)) {
                    cnt += 1;
                    System.out.println(curr.getID());
                }
            }
        }
        if (cnt == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        branchTree.print();
        System.out.println("");
        repo.print();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        System.out.println("");
    }

    public static void branch(String name) {
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        branchTree.createBranch(name, currCommit.getID());
    }

    public static void switchBranch(String branch) {
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        HashMap<String, Commit> commitMap = new HashMap<String, Commit>();
        HashMap<String, Blob> blobMap = new HashMap<String, Blob>();
        Commit newCommit = idToCommit(branchTree.getBranch(branch), commitMap);
        newCommit.makeHead(currCommit);
        for (String file : newCommit.getFiles().keySet()) {
            Utils.writeContents(join(Repository.CWD, file), idToBlob(newCommit.getFiles().get(file),
                    blobMap).getContents());
        }
        repo.clear();
        branchTree.setCurr(branch);
        currCommit = newCommit;
        Utils.writeObject(join(Repository.GITLET_DIR, "currCommit"), currCommit);
    }

    public static void restore(String[] args) {
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        HashMap<String, Commit> commitMap = new HashMap<String, Commit>();
        HashMap<String, Blob> blobMap = new HashMap<String, Blob>();
        String fileName;
        Commit restoreCommit;
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            fileName = args[3];
            restoreCommit = idToCommit(args[1], commitMap);
        } else {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            fileName = args[2];
            restoreCommit = currCommit;
        }
        if (!restoreCommit.hasFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File writeTo = new File(fileName);
        Utils.writeContents(writeTo, idToBlob(restoreCommit.getBlobID(fileName), blobMap).getContents());
    }

    public static void removeBranch(String branch) {
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        branchTree.deleteBranch(branch);
    }

    public static void reset(String id) {
        Repository repo = Utils.readObject(Utils.join(Repository.GITLET_DIR, "repo"), Repository.class);
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        HashMap<String, Commit> commitMap = new HashMap<String, Commit>();
        HashMap<String, Blob> blobMap = new HashMap<String, Blob>();
        Commit newCommit = idToCommit(id, commitMap);
        newCommit.makeHead(currCommit);
        for (String file : newCommit.getFiles().keySet()) {
            Utils.writeContents(join(Repository.CWD, file), idToBlob(newCommit.getFiles().get(file),
                    blobMap).getContents());
        }
        repo.clear();
        branchTree.setCurrHead(id);
        currCommit = newCommit;
        Utils.writeObject(join(Repository.GITLET_DIR, "currCommit"), currCommit);

    }


    public static void merge(String givenBranch) {
        Commit currCommit = Utils.readObject(Utils.join(Repository.GITLET_DIR, "currCommit"), Commit.class);
        BranchTree branchTree = Utils.readObject(Utils.join(Repository.GITLET_DIR, "tree"), BranchTree.class);
        HashMap<String, Commit> commitMap = new HashMap<String, Commit>();
        HashMap<String, Blob> blobMap = new HashMap<String, Blob>();
        if (Repository.TO_ADD.listFiles().length != 0 || Repository.TO_REMOVE.listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branchTree.hasBranch(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchTree.getCurrBranchName().equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit givenCommit = idToCommit(branchTree.getBranch(givenBranch), commitMap);
        HashSet<Commit> currAncestors = new HashSet<Commit>();
        Commit curr = idToCommit(currCommit.getParentID(), commitMap);
        while (curr != null) {
            if (curr.equals(givenCommit)) {
                System.out.println("Given branch is an ancestor of the current branch.");
                return;
            }
            currAncestors.add(curr);
            if (curr.getParentID() != null) {
                curr = idToCommit(curr.getParentID(), commitMap);
            } else {
                curr = null;
            }
        }
        if (currCommit.getSecondParentID() != null) {
            curr = idToCommit(currCommit.getSecondParentID(), commitMap);
            while (curr != null) {
                if (curr.equals(givenCommit)) {
                    System.out.println("Given branch is an ancestor of the current branch.");
                    return;
                }
                currAncestors.add(curr);
                if (curr.getParentID() != null) {
                    curr = idToCommit(curr.getParentID(), commitMap);
                } else {
                    curr = null;
                }
            }
        }
        Commit split = null;
        curr = idToCommit(givenCommit.getParentID(), commitMap);
        while (curr != null) {
            if (curr.getID().equals(currCommit.getID())) {
                System.out.println("Current branch fast-forwarded.");
                switchBranch(givenBranch);
                return;
            }
            if (split == null && currAncestors.contains(curr)) {
                split = curr;
            }
            if (curr.getParentID() != null) {
                curr = idToCommit(curr.getParentID(), commitMap);
            } else {
                curr = null;
            }
        }
        HashMap<String, String> splitFiles = split.getFiles();
        HashMap<String, String> currFiles = currCommit.getFiles();
        HashMap<String, String> givenFiles = givenCommit.getFiles();
        for (String fileName : givenFiles.keySet()) {
            if (!currCommit.hasFile(fileName) && !split.hasFile(fileName)) {
                if (join(Repository.CWD, fileName).exists()) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                Blob givenBlob = idToBlob(givenFiles.get(fileName), blobMap);
                Utils.writeContents(join(Repository.CWD, fileName), givenBlob.getContents());
                add(fileName);
            }
        }
        mergeSecondHalf(splitFiles, currFiles, givenFiles, currCommit, givenCommit, blobMap, givenBranch, branchTree);
    }

    public static void mergeSecondHalf(HashMap<String, String> splitFiles, HashMap<String, String> currFiles,
                                       HashMap<String, String> givenFiles, Commit currCommit,
                                       Commit givenCommit, HashMap<String, Blob> blobMap,
                                       String givenBranch, BranchTree branchTree) {
        boolean conflict = false;
        for (String fileName : splitFiles.keySet()) {
            if (currCommit.hasFile(fileName) && givenCommit.hasFile(fileName)) {
                Blob splitBlob = idToBlob(splitFiles.get(fileName), blobMap);
                Blob currBlob = idToBlob(currFiles.get(fileName), blobMap);
                Blob givenBlob = idToBlob(givenFiles.get(fileName), blobMap);
                if (!splitBlob.getContents().equals(givenBlob.getContents())
                        && splitBlob.getContents().equals(currBlob.getContents())) {
                    Utils.writeContents(join(Repository.CWD, fileName), givenBlob.getContents());
                    add(fileName);
                } else if (!splitBlob.getContents().equals(givenBlob.getContents())
                        && !splitBlob.getContents().equals(currBlob.getContents())) {
                    mergeWrite(fileName, currBlob.getContents(), givenBlob.getContents());
                    conflict = true;
                }
            } else if (currCommit.hasFile(fileName) && !givenCommit.hasFile(fileName)) {
                Blob splitBlob = idToBlob(splitFiles.get(fileName), blobMap);
                Blob currBlob = idToBlob(currFiles.get(fileName), blobMap);
                if (splitBlob.getContents().equals(currBlob.getContents())) {
                    Utils.restrictedDelete(fileName);
                    remove(fileName);
                } else {
                    mergeWrite(fileName, currBlob.getContents(), new byte[0]);
                    conflict = true;
                }
            } else if (givenCommit.hasFile(fileName)) {
                if (join(Repository.CWD, fileName).exists()) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                Blob splitBlob = idToBlob(splitFiles.get(fileName), blobMap);
                Blob givenBlob = idToBlob(givenFiles.get(fileName), blobMap);
                if (!splitBlob.getContents().equals(givenBlob.getContents())) {
                    mergeWrite(fileName, new byte[0], givenBlob.getContents());
                    conflict = true;
                }
            }
        }
        commit("Merged " + givenBranch + " into " + branchTree.getCurrBranchName() + ".", givenCommit, givenBranch);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void mergeWrite(String fileName, byte[] currContents, byte[] givenContents) {
        Utils.writeContents(join(Repository.CWD, fileName), "<<<<<<< HEAD\n",
                currContents, "=======\n", givenContents, ">>>>>>>\n");
    }





    /**public void setUp() {
     commitMap = new HashMap<String, Commit>();
     blobMap = new HashMap<String, Blob>();
     if (Repository.GITLET_DIR.exists()) {
     repo = readObject(join(Repository.GITLET_DIR, "repo"), Repository);
     currCommit = readObject(join(Repository.GITLET_DIR, "currCommit"), Commit);
     branchTree = readObject(join(Repository.GITLET_DIR, "branchTree"), Tree);
     }
     }**/

    public static Commit idToCommit(String id, HashMap<String, Commit> commitMap) {
        if (id.length() < "1234567891324235235".length()) {
            id = id.substring(0, 6);
        }
        if (commitMap.containsKey(id)) {
            return commitMap.get(id);
        }
        File commitFile = join(COMMITS, id);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit ans = Utils.readObject(commitFile, Commit.class);
        commitMap.put(id, ans);
        return ans;
    }

    public static Blob idToBlob(String id, HashMap<String, Blob> blobMap) {
        if (blobMap.containsKey(id)) {
            return blobMap.get(id);
        }
        File blobFile = join(BLOBS, id);
        Blob ans = Utils.readObject(blobFile, Blob.class);
        blobMap.put(id, ans);
        return ans;
    }
}
