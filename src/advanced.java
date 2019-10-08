import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class advanced {

    public static void main(String[] args) throws IOException {
//        if (args.length == 0) {
//            search.printInstruction();
//            return;
//        }

        List<String> arguments = new ArrayList<>(Arrays.asList(args));

        int index = 0;
        boolean BM25 = true;
        int queryLabel = 0;
        int numResults = 1;
        String lexicon = null, invLists = null, map = null, stop = null;
        String[] queryItems;

        long startTime = System.nanoTime();
        
        if (arguments.contains("-q")) {
            index = arguments.indexOf("-q") + 1;
            queryLabel = Integer.parseInt(arguments.get(index));
        } else {
            System.out.println("Do not detect the argument, -q <query-label>");
            printUsage();
            return;
        }

        if (arguments.contains("-n")) {
            index = arguments.indexOf("-n") + 1;
            numResults = Integer.parseInt(arguments.get(index));
        }

        if (arguments.contains("-l")) {
            index = arguments.indexOf("-l") + 1;
            lexicon = arguments.get(index);
        } else {
            System.out.println("Do not detect the argument, -l <lexicon>");
            printUsage();
            return;
        }

        if (arguments.contains("-i")) {
            index = arguments.indexOf("-i") + 1;
            invLists = arguments.get(index);
        } else {
            System.out.println("Do not detect the argument, -i <invlists>");
            printUsage();
            return;
        }

        if (arguments.contains("-m")) {
            index = arguments.indexOf("-m") + 1;
            map = arguments.get(index);
        } else {
            System.out.println("Do not detect the argument, -m <map>");
            printUsage();
            return;
        }

        if (arguments.contains("-s")) {
            index = arguments.indexOf("-s") + 1;
            stop = arguments.get(index);
        } else {
            System.out.println("Do not detect the argument, -s <stoplist>");
            printUsage();
            return;
        }
        index += 1;
        queryItems = Arrays.copyOfRange(args, index, args.length);

        search.loadResources(lexicon, invLists, map, stop);
        search.advancedQuery(queryLabel, numResults, BM25, queryItems);

        long endTime = System.nanoTime();
        long time = (endTime - startTime) / 1000000;
        System.out.println("Running time: " + time + " ms");
    }

    private static void printUsage(){
        String usage = "advanced Usage:\n" +
                "    java advanced -q <query-label> -n <num-results> -l <lexicon> -i <invlists> -m map \n" +
                "    -s <stoplist> <queryterm-1> [<queryterm-2> ... <queryterm-N>]\n" +
                "\n" +
                "    e.g. java advanced -q 401 -n 5 -l lexicon -i invlists -m map -s stoplist volcano america active.";
        System.out.println("Invalid Input.");
        System.out.println(usage);
    }
}
