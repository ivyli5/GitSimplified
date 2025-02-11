package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Ivy Li
 */

public class Repository implements Serializable {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File STAGING_AREA = join(GITLET_DIR, "stagingarea");

    public static final File TO_ADD = join(STAGING_AREA, "toadd");

    public static final File TO_REMOVE = join(STAGING_AREA, "toremove");

    public Repository() {
        GITLET_DIR.mkdir();
        STAGING_AREA.mkdir();
        TO_ADD.mkdir();
        TO_REMOVE.mkdir();
        Utils.writeObject(Utils.join(GITLET_DIR, "repo"), this);
    }

    public void add(Blob toAdd) {
        if (join(TO_REMOVE, toAdd.getName()).exists()) {
            join(TO_REMOVE, toAdd.getName()).delete();
            return;
        }
        File added = join(TO_ADD, toAdd.getName());
        Utils.writeContents(added, toAdd.getID());
    }

    public boolean removeAdd(String fileName) {
        File toRemove = Utils.join(TO_ADD, fileName);
        if (toRemove.exists()) {
            toRemove.delete();
            return true;
        }
        return false;
    }

    public void removeBoth(String fileName) {
        removeAdd(fileName);
        File toRemove = Utils.join(TO_REMOVE, fileName);
        if (toRemove.exists()) {
            toRemove.delete();
        }
    }

    public void addRemove(String fileName) {
        File toAdd = Utils.join(TO_REMOVE, fileName);
        Utils.writeContents(toAdd, "");
    }

    public void clear() {
        for (File file : TO_ADD.listFiles()) {
            file.delete();
        }
        for (File file : TO_REMOVE.listFiles()) {
            file.delete();
        }
    }

    public boolean isEmpty() {
        return (TO_ADD.listFiles().length == 0 && TO_REMOVE.listFiles().length == 0);
    }

    public void print() {
        System.out.println("=== Staged Files ===");
        File[] toAList = TO_ADD.listFiles();
        Arrays.sort(toAList);
        File[] toRList = TO_REMOVE.listFiles();
        Arrays.sort(toRList);
        for (File file : toAList) {
            System.out.println(file.getName());
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        for (File file : toRList) {
            System.out.println(file.getName());
        }
        System.out.println("");
    }
}
