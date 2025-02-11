package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.join;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Ivy Li
 */

public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    private Date timestamp;

    private String parentID;
    private String secondParentID;

    private HashMap<String, String> files;

    private String id;

    public Commit(String m) {
        message = m;
        parentID = null;
        secondParentID = null;
        timestamp = new Date(0);
        files = new HashMap<String, String>();
        id = Utils.sha1(Utils.serialize(this));
        File commitSave = join(Main.COMMITS, id);
        File commitSaveShort = join(Main.COMMITS, id.substring(0, 6));
        Utils.writeObject(commitSave, this);
        Utils.writeObject(commitSaveShort, this);
    }

    public Commit(String m, Commit parent) {
        message = m;
        parentID = parent.getID();
        secondParentID = null;
        timestamp = new Date();
        files = (HashMap<String, String>) parent.getFiles().clone();
        for (File toAdd : Repository.TO_ADD.listFiles()) {
            String blobID = Utils.readContentsAsString(toAdd);
            files.put(toAdd.getName(), blobID);
            toAdd.delete();
            //File toAddSaved = join(Repository.GITLET_DIR, sha1);
            //Utils.writeContents(toAddSaved, Utils.readContents(toAdd));
            //files.put(toAdd.getName(), BlobID);
            //Utils.restrictedDelete(toAdd);
        }
        for (File toRemove : Repository.TO_REMOVE.listFiles()) {
            files.remove(toRemove.getName());
            toRemove.delete();
        }
        id = Utils.sha1(Utils.serialize(this));
        File commitSave = join(Main.COMMITS, id);
        File commitSaveShort = join(Main.COMMITS, id.substring(0, 6));
        Utils.writeObject(commitSave, this);
        Utils.writeObject(commitSaveShort, this);
    }

    public Commit(String m, Commit parent, Commit secondParent) {
        message = m;
        parentID = parent.getID();
        secondParentID = secondParent.getID();
        timestamp = new Date();
        files = (HashMap<String, String>) parent.getFiles().clone();
        for (File toAdd : Repository.TO_ADD.listFiles()) {
            String blobID = Utils.readContentsAsString(toAdd);
            files.put(toAdd.getName(), blobID);
            toAdd.delete();
            //File toAddSaved = join(Repository.GITLET_DIR, sha1);
            //Utils.writeContents(toAddSaved, Utils.readContents(toAdd));
            //files.put(toAdd.getName(), BlobID);
            //Utils.restrictedDelete(toAdd);
        }
        for (File toRemove : Repository.TO_REMOVE.listFiles()) {
            files.remove(toRemove.getName());
            toRemove.delete();
        }
        id = Utils.sha1(Utils.serialize(this));
        File commitSave = join(Main.COMMITS, id);
        File commitSaveShort = join(Main.COMMITS, id.substring(0, 6));
        Utils.writeObject(commitSave, this);
        Utils.writeObject(commitSaveShort, this);
    }

    public void print() {
        System.out.println("===");
        System.out.println("commit " + id);
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        System.out.println("Date: " + formatter.format(timestamp));
        System.out.println(message);
        System.out.println("");
    }

    public HashMap<String, String> getFiles() {
        return files;
    }

    public void clearFiles(Commit newCommit) {
        for (String file : files.keySet()) {
            if (!newCommit.hasFile(file)) {
                Utils.restrictedDelete(join(Repository.CWD, file));
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public void makeHead(Commit currCommit) {
        for (String file : files.keySet()) {
            if (!currCommit.hasFile(file) && join(Repository.CWD, file).exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        currCommit.clearFiles(this);
    }

    public String getID() {
        return id;
    }

    public String getParentID() {
        return parentID;
    }

    public String getSecondParentID() {
        return secondParentID;
    }

    /**public Commit getParent() {
     if (parent == null) {
     return null;
     }
     return Utils.readObject(join(Main.COMMITS, parentID), Commit);
     }**/

    public boolean hasFile(String name) {
        return files.containsKey(name);
    }

    public String getBlobID(String name) {
        return files.get(name);
    }
}
