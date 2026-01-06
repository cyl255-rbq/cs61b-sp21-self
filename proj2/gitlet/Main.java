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
        initialized(firstArg);
        switch(firstArg) {
            case "init":
                validateArgs(args, 1);
                init();
                break;
            case "add":
                validateArgs(args, 2);
                add(args[1]);
                break;
            case "commit":
                validateArgs(args, 2);
                commit(args[1]);
                break;
            case "rm":
                validateArgs(args, 2);
                rm(args[1]);
                break;
            case "log":
                validateArgs(args, 1);
                log();
                break;
            case "global-log":
                validateArgs(args, 1);
                globalLog();
                break;
            case "find":
                validateArgs(args, 2);
                find(args[1]);
                break;
            case "status":
                validateArgs(args, 1);
                status();
                break;
            case "checkout":
                validateArgsCheckout(args);
                checkout(args, args.length);
                break;
            case "branch":
                validateArgs(args, 2);
                branch(args[1]);
                break;
            case "rm-branch":
                validateArgs(args, 2);
                rmBranch(args[1]);
                break;
            case "reset":
                validateArgs(args, 2);
                reset(args[1]);
                break;
            case "merge":
                validateArgs(args, 2);
                merge(args[1]);
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void validateArgsCheckout(String[] args) {
        int length = args.length;
        if (length == 3 && args[1].equals("--")) {
        } else if (length == 4 && args[2].equals("--")) {
        } else if (length == 2) {
        } else {
            message("Incorrect operands.");
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
    private static void initialized(String arg) {
        if (!arg.equals("init") && !GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

}
