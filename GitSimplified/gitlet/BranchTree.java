package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class BranchTree implements Serializable {
    private String currBranch;
    private HashMap<String, String> branches;

    public BranchTree(Commit init) {
        currBranch = "main";
        branches = new HashMap<String, String>();
        branches.put("main", init.getID());
        Utils.writeObject(Utils.join(Repository.GITLET_DIR, "tree"), this);
    }

    public void createBranch(String name, String id) {
        if (branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        branches.put(name, id);
        Utils.writeObject(Utils.join(Repository.GITLET_DIR, "tree"), this);
    }

    public String getBranch(String name) {
        if (!branches.containsKey(name)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (currBranch.equals(name)) {
            System.out.println("No need to switch to the current branch.");
            System.exit(0);
        }
        return branches.get(name);
    }

    public void deleteBranch(String name) {
        if (!branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currBranch.equals(name)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branches.remove(name);
        Utils.writeObject(Utils.join(Repository.GITLET_DIR, "tree"), this);
    }

    public boolean hasBranch(String name) {
        if (branches.containsKey(name)) {
            return true;
        }
        return false;
    }

    public String getCurrBranchName() {
        return currBranch;
    }

    public void setCurr(String name) {
        currBranch = name;
        Utils.writeObject(Utils.join(Repository.GITLET_DIR, "tree"), this);
    }

    public void setSecondBranch(String name, String id) {
        branches.put(name, id);
        Utils.writeObject(Utils.join(Repository.GITLET_DIR, "tree"), this);
    }

    public void setCurrHead(String id) {
        branches.put(currBranch, id);
        Utils.writeObject(Utils.join(Repository.GITLET_DIR, "tree"), this);
    }

    public String getCurrCommit() {
        return branches.get(currBranch);
    }

    public void print() {
        System.out.println("=== Branches ===");
        System.out.println("*" + currBranch);
        for (String branch : branches.keySet()) {
            if (!branch.equals(currBranch)) {
                System.out.println(branch);
            }
        }
    }
}
