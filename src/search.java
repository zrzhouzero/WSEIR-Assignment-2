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
    private static Hashtable<Integer, String> documentMappingTable = new Hashtable<>();
    private static File lexicon;
    private static File map;
    private static File invlists;


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
     * @throws IOException when the file cannot be found
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
     * load the map file to system
     *
     * @param map the map file
     */
    private static void loadMap(File map) {
        String mapLine;
        try (BufferedReader mapReader = new BufferedReader(new FileReader(map))){
			while ((mapLine = mapReader.readLine()) != null) {
			    String[] temp = mapLine.split(",");
			    documentMappingTable.put(Integer.parseInt(temp[0].trim()), temp[1]);
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
    private static void query(int label, String... terms) throws IOException {
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
            System.out.println(term);

            // if ranking mode is off
            System.out.println(queryResult(readInvList(startingPoint), label));
            // if ranking mode is on
            System.out.println(queryResult(rankingMap(readInvList(startingPoint), 10), label, 10, term));
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
     * This class is to record the information of a document to avoid reading the source document again
     * when count the document length
     */
    private static class DocumentInfo {
        private int fdt;
        private double Ld;

        private DocumentInfo(int fdt, double Ld) {
            this.fdt = fdt;
            this.Ld = Ld;
        }
    }


    /**
     * rank the top X documents according to the search result
     *
     * @param map the search result map generated by function @readInvList
     * @param topX the number of top X result
     * @return a list of ranked documents and the respective ranking point
     */
    private static ArrayList<DocumentRankingPoints> rankingMap(HashMap<Integer, Integer> map, int topX) {
        double totalLength = 0;
        HashMap<Integer, DocumentInfo> countedMap = new HashMap<>();

        double ft = map.size();

        for (Integer i : map.keySet()) {
            double count = getDocumentCharacterCount(i);
            DocumentInfo info = new DocumentInfo(map.get(i), count);
            countedMap.put(i, info);
            totalLength += count;
        }

        double AL = totalLength / ft;
        double N = documentMappingTable.size();

        MinHeap rank = new MinHeapArray(topX);
        for (Integer i : countedMap.keySet()) {
            double fdt = countedMap.get(i).fdt;
            double Ld = countedMap.get(i).Ld;
            double point = RankingPointCalculator.BM25(N, ft, fdt, Ld, AL);
            rank.addElement(new DocumentRankingPoints(i, point));
        }
        return rank.outputRank();
    }


    /**
     *
     * @param documentId the id of the document being counting
     * @return the total characters of the document
     */
    // TODO: function to count the characters in a document
    private static double getDocumentCharacterCount(int documentId) {
        return 10000;
    }


    /**
     * this function is to convert a hash map into a list of human readable string
     *
     * @param map the input hash map indicate did (document id) and fdt (the term frequency within the document) key,
     *            value pair
     * @param label query label
     * @return the interpretation of the map as a readable string
     */
    private static String queryResult(HashMap<Integer, Integer> map, int label) {
        StringBuilder builder = new StringBuilder();
        builder.append(map.size()).append(System.lineSeparator());

        for (Integer i : map.keySet()) {
            builder.append(label).append(" ");
            builder.append(documentMappingTable.get(i)).append(" ");
            builder.append(map.get(i)).append(System.lineSeparator());
        }

        return builder.toString();
    }


    /**
     * this function is to convert a ranked list into human readable string
     *
     * @param rankList the ranked result list which contains the document id and the document ranking point
     * @param label query label
     * @param topX the number of top X documents the query asked for
     * @return the interpretation of the list as a readable string
     */
    private static String queryResult(ArrayList<DocumentRankingPoints> rankList, int label, int topX, String term) {
        StringBuilder builder = new StringBuilder();
        builder.append("Top ").append(topX).append(" result for \"").append(term).append("\": ").append(System.lineSeparator());

        for (int i = 0; i < rankList.size(); i++) {
            builder.append(label).append(" ");
            builder.append(documentMappingTable.get(rankList.get(i).getDocumentId())).append(" ");
            int rank = i + 1;
            builder.append(rank).append(" ");
            DecimalFormat df = new DecimalFormat("#.###");
            df.setRoundingMode(RoundingMode.CEILING);
            builder.append(df.format(rankList.get(i).getDocumentPoint())).append(System.lineSeparator());
        }

        return builder.toString();
    }


    /**
     * load lexicon, invList, and map to system
     *
     * @param filePath the paths of the above three files
     * @throws NumberFormatException see above @loadLexicon
     * @throws IOException when I/O error occurs
     */
    private static void loadResources(String... filePath) throws NumberFormatException, IOException {
        if (!checkFileExists(filePath)) System.exit(0);
        lexicon = new File(filePath[0]);
        invlists = new File(filePath[1]);
        map = new File(filePath[2]);
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
    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        if (args.length == 0) {
			// for test in ide
//            loadResources("./lexicon", "./invlists", "./map");
//			query("hit", "flat", "meeting");
			printInstruction();
            return;
        }
        if (args.length >= 4) {
            loadResources(args[0], args[1], args[2]);
            String[] terms = Arrays.copyOfRange(args, 3, args.length);

            // TODO: add a function to label the query, currently hard coded the label as "401"
            query(401, terms);
        } else {
            printInstruction();
        }
        long endTime = System.nanoTime();
        long time = (endTime - startTime) / 1000000;
        System.out.println("Running time: " + time + " ms");
    }

}