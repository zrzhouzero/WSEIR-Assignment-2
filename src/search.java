import util.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class search {

    private static Hashtable<String, Integer> lexiconTable;
    private static Hashtable<Integer, DocumentInfo> documentMappingTable = new Hashtable<>();
    private static File lexicon;

    // file structure: document id, document title, document length
    private static File map;
    private static File invlists;

    private static String documentDirectoryPath = "docs/";
    private static String stopListPath = null;


    /**
     * Print instruction when the parameters are invalid
     */
    private static void printInstruction() {
        System.out.println("Invalid Input.");
        System.out.println("Usage:\n\tjava search <lexicon> <invlists> <map> <queryterm 1> [... <queryterm N>]");
    }


    /**
     * Load the lexicon file to system
     *
     * @param lexicon the lexicon file
     * @throws NumberFormatException when the lexicon file is invalid,
     *                               i.e. the line is not (lexicon, starting point) structure
     * @throws IOException           when the file cannot be found
     */
    private static void loadLexicon(File lexicon) throws NumberFormatException, IOException {
        lexiconTable = new Hashtable<>();
        BufferedReader lexiconReader = new BufferedReader(new FileReader(lexicon));
        String lexiconLine;
        while ((lexiconLine = lexiconReader.readLine()) != null) {
            String[] term = lexiconLine.split(",");
            lexiconTable.put(term[0], Integer.parseInt(term[1].trim()));
        }
        lexiconReader.close();
    }


    /**
     * This class is to record the document id and document length as the attribute of a document
     */
    public static class DocumentInfo {
        String documentTitle;
        double documentLength;

        DocumentInfo(String documentTitle, double documentLength) {
            this.documentTitle = documentTitle;
            this.documentLength = documentLength;
        }
    }


    /**
     * load the map file to system
     *
     * @param map the map file
     */
    private static void loadMap(File map) {
        String mapLine;
        try (BufferedReader mapReader = new BufferedReader(new FileReader(map))) {
            while ((mapLine = mapReader.readLine()) != null) {
                String[] temp = mapLine.split(",");
                documentMappingTable.put(Integer.parseInt(temp[0].trim()), new DocumentInfo(temp[1], Double.parseDouble(temp[2])));
            }
        } catch (NumberFormatException e) {
            System.out.println("The file [map] is not valid.");
            printInstruction();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    /**
     * query the keywords and print the result on screen
     *
     * @param label query label
     * @param terms all the terms in query
     * @throws IOException when an I/O error occurs
     */
    private static void query(int label, int numOfResults, boolean isRankOn, String... terms) throws IOException {
        // search the target word in lexicon table
        // 1. target the query term in the lexicon table
        // 2. get the pointer of the term
        // 3. read the invlist file starting at the pointer
        // 4. retrieve the part of the term
        // 5. store the integer list for later interpretation
        for (String term : terms) {
            // no relevant result for this term
            if (!lexiconTable.containsKey(term)) {
                System.out.println(term);
                System.out.println("There is no result for [" + term + "]." + System.lineSeparator());
                continue;
            }
            int startingPoint = lexiconTable.get(term);

            // if ranking mode is off
            if (!isRankOn) {
                System.out.println(queryResult(readInvList(startingPoint), label));
            } else {
                // if ranking mode is on
                System.out.println(queryResult(rankingMap(readInvList(startingPoint), numOfResults), label, numOfResults, term, false));
            }
        }
    }

    protected static void advancedQuery(int label, int numResult, boolean isRankOn, String... terms) throws IOException {
        // search the target word in lexicon table
        // 1. target the query term in the lexicon table
        // 2. get the pointer of the term
        // 3. read the invlist file starting at the pointer
        // 4. retrieve the part of the term
        // 5. store the integer list for later interpretation
        for (String term : terms) {
            // no relevant result for this term
            if (!lexiconTable.containsKey(term)) {
                System.out.println(term);
                System.out.println("There is no result for [" + term + "]." + System.lineSeparator());
                continue;
            }
            int startingPoint = lexiconTable.get(term);
            System.out.println(queryResult(rankingMap(readInvList(startingPoint), numResult), label, numResult, term, true));
        }
    }



    /**
     * read file invList and get the target inverted list
     *
     * @param startPos start position in the inverted list
     * @return query result as a hash map
     * @throws IOException if an I/O error occurs
     */
    private static HashMap<Integer, Integer> readInvList(int startPos) throws IOException {
        DataInputStream input = new DataInputStream(new FileInputStream(invlists));
        input.skipBytes(startPos);

        int arrayLength = input.readInt();
        Integer[] intArray = new Integer[arrayLength * 2];
        for (int i = 0; i < arrayLength * 2; i++) {
            intArray[i] = input.readInt();
        }

        HashMap<Integer, Integer> resultMap = new HashMap<>();
        int i = 0;
        while (i < intArray.length - 1) {
            int id = intArray[i];
            i++;
            int fdt = intArray[i];
            i++;
            resultMap.put(id, fdt);
        }

        input.close();
        return resultMap;
    }


    /**
     * rank the top X documents according to the search result
     *
     * @param map  the search result map generated by function @readInvList
     * @param topX the number of top X result
     * @return a list of ranked documents and the respective ranking point
     */
    private static ArrayList<DocumentRankingPoints> rankingMap(HashMap<Integer, Integer> map, int topX) {
        double totalLength = 0;
        HashMap<Integer, Double> countedMap = new HashMap<>();

        double ft = map.size();

        for (Integer i : map.keySet()) {
            double count = getDocumentCharacterCount(i);
            countedMap.put(i, count);
            totalLength += count;
        }

        double AL = totalLength / ft;
        double N = documentMappingTable.size();

        MinHeap rank = new MinHeapArray(topX);
        for (Integer i : countedMap.keySet()) {
            double fdt = map.get(i);
            double Ld = countedMap.get(i);
            double point = RankingCalculator.BM25(N, ft, fdt, Ld, AL);
            rank.addElement(new DocumentRankingPoints(i, point));
        }
        return rank.outputRank();
    }


    /**
     * @param documentId the id of the document being counting
     * @return the total characters of the document
     */
    private static double getDocumentCharacterCount(int documentId) {
        DocumentInfo info = documentMappingTable.get(documentId);
        return info.documentLength;
    }


    /**
     * this function is to convert a hash map into a list of human readable string
     *
     * @param map   the input hash map indicate did (document id) and fdt (the term frequency within the document) key,
     *              value pair
     * @param label query label
     * @return the interpretation of the map as a readable string
     */
    private static String queryResult(HashMap<Integer, Integer> map, int label) {
        StringBuilder builder = new StringBuilder();
        builder.append(map.size()).append(System.lineSeparator());

        for (Integer i : map.keySet()) {
            builder.append(label).append(" ");
            builder.append(documentMappingTable.get(i).documentTitle).append(" ");
            builder.append(map.get(i)).append(System.lineSeparator());
        }

        return builder.toString();
    }


    /**
     * this function is to convert a ranked list into human readable string
     *
     * @param rankList the ranked result list which contains the document id and the document ranking point
     * @param label    query label
     * @param topX     the number of top X documents the query asked for
     * @return the interpretation of the list as a readable string
     */
    private static String queryResult(ArrayList<DocumentRankingPoints> rankList, int label, int topX, String term, boolean summaryMode) {
        StringBuilder builder = new StringBuilder();
        builder.append("Top ").append(topX).append(" result for \"").append(term).append("\": ").append(System.lineSeparator());

        for (int i = 0; i < rankList.size(); i++) {
            builder.append(label).append(" ");
            builder.append(documentMappingTable.get(rankList.get(i).getDocumentId()).documentTitle).append(" ");
            int rank = i + 1;
            builder.append(rank).append(" ");
            DecimalFormat df = new DecimalFormat("#.###");
            df.setRoundingMode(RoundingMode.CEILING);
            builder.append(df.format(rankList.get(i).getDocumentPoint())).append(System.lineSeparator());

            // if summary mode is on
            if (summaryMode) {
                Summary summary = new Summary(rankList.get(i).getDocumentId(), term, documentDirectoryPath, stopListPath);
                builder.append("[Query-based Summary]").append(System.lineSeparator());
                builder.append(summary.generateDynamicSummary()).append(System.lineSeparator());
                builder.append("[Document-based Summary]").append(System.lineSeparator());
                builder.append(summary.generateStaticSummary()).append(System.lineSeparator());
                builder.append(System.lineSeparator());
            }
        }

        return builder.toString();
    }


    /**
     * load lexicon, invList, and map [stoplist] to system
     *
     * @param filePath the paths of the above three files
     * @throws NumberFormatException see above @loadLexicon
     * @throws IOException           when I/O error occurs
     */
    protected static void loadResources(String... filePath) throws NumberFormatException, IOException {
        if (!checkFileExists(filePath)) System.exit(0);
        lexicon = new File(filePath[0]);
        invlists = new File(filePath[1]);
        map = new File(filePath[2]);
        if (filePath[3] != null) stopListPath = filePath[3];
        loadLexicon(lexicon);
        loadMap(map);
    }


    /**
     * check the files existence
     *
     * @param filePath the path of the files
     * @return if all the files exist
     */
    private static boolean checkFileExists(String... filePath) {
        for (String path : filePath) {
            if (path == null) continue;
            File file = new File(path);
            if (!file.exists()) {
                System.out.println(path + " does not exist." + System.lineSeparator());
                return false;
            }
        }
        return true;
    }


    // TODO: add more main line arguments, see assignment 2 page 4-5
    // search [-S [-s stop]]
    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        if (args.length == 0) {
            printInstruction();
            return;
        }

        List<String> arguments = new ArrayList<>(Arrays.asList(args));

        int index = 0;
        boolean BM25 = false;
        int queryLabel = 0;
        int numResults = 1;
        String lexicon = null, invLists = null, map = null, stop = null;
        String[] queryItems;
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

        loadResources(lexicon, invLists, map, stop);
        query(queryLabel, numResults, BM25, queryItems);

        long endTime = System.nanoTime();
        long time = (endTime - startTime) / 1000000;
        System.out.println("Running time: " + time + " ms");
    }

}