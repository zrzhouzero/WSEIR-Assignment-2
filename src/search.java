import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;

public class search {

    private static Hashtable<String, Pair> lexiconTable;
    private static Hashtable<Integer, String> documentMappingTable = new Hashtable<>();
    private static File lexicon;
    private static File map;
    private static File invlists;

    private static void printInstruction() {
        System.out.println("Invalid Input.");
        System.out.println("Usage:\n\tjava search <lexicon> <invlists> <map> <queryterm 1> [... <queryterm N>]");
    }

    private static void loadLexicon(File lexicon) throws NumberFormatException, IOException {
        lexiconTable = new Hashtable<>();
        BufferedReader lexiconReader = new BufferedReader(new FileReader(lexicon));
        String lexiconLine;
        int startingPoint = 0;
        while ((lexiconLine = lexiconReader.readLine()) != null) {
            String[] term = lexiconLine.split(",");
            int length = Integer.parseInt(term[1].trim()) - startingPoint;
            Pair p = new Pair(startingPoint, length);
            lexiconTable.put(term[0], p);
            startingPoint = Integer.parseInt(term[1].trim());
        }
        lexiconReader.close();
    }

    private static void loadMap(File map) {
        String mapLine;
        try (BufferedReader mapReader = new BufferedReader(new FileReader(map))){
			while ((mapLine = mapReader.readLine()) != null) {
			    String[] temp = mapLine.split(",");
			    documentMappingTable.put(Integer.parseInt(temp[0].trim()), temp[1]);
			}
	        mapReader.close();
		} catch (NumberFormatException e) {
			System.out.println("The file [map] is not invalid.");
			printInstruction();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private static class Pair {
        private int startingPoint;
        private int length;

        private Pair(int startingPoint, int length) {
            this.startingPoint = startingPoint;
            this.length = length;
        }
    }

    private static void query(String... terms) throws IOException {
        // search the target word in lexicon table
        // 1. target the query term in the lexicon table
        // 2. get the pointer of the term
        // 3. read the invlist file starting at the pointer
        // 4. retrieve the part of the term
        // 5. store the integer list for later interpretation
        for (String term : terms) {
            // no relevant result for this term
            if (!lexiconTable.containsKey(term)) {
                System.out.println("There is no result for [" + term + "]." + System.lineSeparator());
                continue;
            }
            Pair pair = lexiconTable.get(term);
            System.out.println(term);
            System.out.println(readInvList(pair.startingPoint, pair.length));
        }
    }

    /**
     * read file invList and get the target inverted list
     *
     * @param startPos start position in the inverted list
     * @param length   reading length
     * @return query result in format
     * @throws IOException if an I/O error occurs
     */
    private static String readInvList(int startPos, int length) throws IOException {
        String queryResult;
        DataInputStream input = new DataInputStream(new FileInputStream(invlists));
        byte[] bytes = new byte[100000];
        input.skipBytes(startPos);
        input.read(bytes, 0, length);
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, length);
        
        StringBuilder builder = new StringBuilder();
        builder.append(buffer.getInt()).append(System.lineSeparator());

        // the buffer.getInt() above indicate the term frequency
        
        while(buffer.remaining() > 0) {
            builder.append(documentMappingTable.get(buffer.getInt())).append(" ");
            builder.append((int)buffer.get()).append(System.lineSeparator());
        }

        // the buffer.getInt() above indicate the term frequency within a document

        queryResult = builder.toString();
        input.close();
        return queryResult;
    }

    private static void loadResources(String... filePath) throws NumberFormatException, IOException {
        if (!checkFileExists(filePath)) System.exit(0);
        lexicon = new File(filePath[0]);
        invlists = new File(filePath[1]);
        map = new File(filePath[2]);
        loadLexicon(lexicon);
        loadMap(map);
    }

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


    public static void main(String[] args) throws IOException {
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
            query(terms);
        } else {
            printInstruction();
        }
    }

}