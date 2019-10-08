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
        boolean BM25 = false;
        int queryLabel = 0;
        int numResults = 1;
        String lexicon = null, invLists = null, map = null, stop = null;
        String[] queryItems;

        long startTime = System.nanoTime();

        if (arguments.contains("-BM25")){
            BM25 = true;
        }
        if (arguments.contains("-q")) {
            index = arguments.indexOf("-q") + 1;
            queryLabel = Integer.parseInt(arguments.get(index));
        }

        if (arguments.contains("-n")) {
            index = arguments.indexOf("-n") + 1;
            numResults = Integer.parseInt(arguments.get(index));
        }

        if (arguments.contains("-l")) {
            index = arguments.indexOf("-l") + 1;
            lexicon = arguments.get(index);
        }

        if (arguments.contains("-i")) {
            index = arguments.indexOf("-i") + 1;
            invLists = arguments.get(index);
        }

        if (arguments.contains("-m")) {
            index = arguments.indexOf("-m") + 1;
            map = arguments.get(index);
        }

        if (arguments.contains("-s")) {
            index = arguments.indexOf("-s") + 1;
            stop = arguments.get(index);
        }
        index += 1;
        queryItems = Arrays.copyOfRange(args, index, args.length);

        search.loadResources(lexicon, invLists, map, stop);
        search.advancedQuery(queryLabel, numResults, BM25, queryItems);

        long endTime = System.nanoTime();
        long time = (endTime - startTime) / 1000000;
        System.out.println("Running time: " + time + " ms");
    }
}
