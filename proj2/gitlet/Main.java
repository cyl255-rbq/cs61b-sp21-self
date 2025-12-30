package gitlet;

import java.io.File;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author cyl
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateArgs(args, 1);
                init();
                break;
            case "add":
                initialized();
                validateArgs(args, 2);
                add(args[1]);
                break;
            case "commit":
                initialized();
                validateArgs(args, 2);
                commit(args[1]);
                break;
            case "rm":
                initialized();
                validateArgs(args, 2);
                rm(args[1]);
                break;
            case "log":
                break;
            case "global-log":
                break;
            case "find":
                break;
            case "status":
                break;
            case "checkout":
                break;
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * @param args arguments.
     * @param n nums of the operator should follow.
     */

    private static void validateArgs(String[] args, int n) {
        if (args.length != n) {
            if (args[0].equals("commit")) {
                message("Please enter a commit message.");
            } else {
                message("Incorrect operands.");
            }
            System.exit(0);
        }
    }

    /**
     * test whether initialized that have ".gitlet" file.
     */
    private static void initialized() {
        if (!GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

    }

}
