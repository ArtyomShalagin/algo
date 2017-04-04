package veb;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {
    public static void run(InputStream input, boolean print) {
        Scanner in = new Scanner(input);
        PrintWriter out;
        if (print) {
            out = new PrintWriter(System.out);
        } else {
            try {
                out = new PrintWriter(new FileOutputStream(new File("/dev/null")));
            } catch (FileNotFoundException ignored) {
                System.err.println("Unable to write to /dev/null o_O\n If you are using windows please stop");
                return;
            }
        }
        out.print("max w: ");
        out.flush();
        int w = in.nextInt();
        VEBTree tree = new VEBTree(w);
        out.println("max value = " + ((1 << w) - 1));
        out.println("commands: add <number>, remove <number>, min, max, next <number>, prev <number>, dump, quit");
        out.flush();
        reading:
        while (in.hasNext()) {
            String q = in.next();
            switch (q) {
                case "add": {
                    long value = in.nextLong();
                    tree.add(value);
                    break;
                }
                case "remove": {
                    long value = in.nextLong();
                    tree.remove(value);
                    break;
                }
                case "min":
                    out.println(tree.getMin());
                    break;
                case "max":
                    out.println(tree.getMax());
                    break;
                case "next": {
                    long value = in.nextLong();
                    out.println(tree.next(value));
                    break;
                }
                case "prev": {
                    long value = in.nextLong();
                    System.out.println(tree.prev(value));
                    break;
                }
                case "dump":
                    List<Long> dump = new ArrayList<>();
                    for (long i = 0; i < 1 << w; i++) {
//                        if (tree.find(i)) {
//                            dump.add(i);
//                        }
                    }
                    out.println("dump: " + dump);
                    break;
                case "quit":
                    break reading;
                default:
                    out.println("unknown command " + q);
            }
            out.flush();
        }
        out.close();
    }
}
