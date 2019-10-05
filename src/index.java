import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class index {

    private static Hashtable<String, Hashtable<Integer, Integer>> invertedList;
    private static int currentDocId;

    static {
        invertedList = new Hashtable<>();
        currentDocId = 1;
    }

    public static void indexing(String sourceFilePath) {
        indexing(sourceFilePath, null);
    }

    public static void indexing(String sourceFilePath, String stoplistPath) {
        // check if files exist
        if (!checkFileExists(sourceFilePath, stoplistPath)) return;
        // fetch terms stored in stoplist
        HashSet<String> stoplist = fetchStopList(stoplistPath);
        // parse source file
        parse(new File(sourceFilePath), stoplist);
    }

    private static void parse(File sourceFile, HashSet<String> stoplist) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
            String line;
            boolean requireRecord = false;

            // output map
            File map = new File("map");
            map.delete();
            map.createNewFile();
            BufferedWriter mapWriter = new BufferedWriter(new FileWriter(map));

            String str = "[.,\"/\\?!@#$%^&*--+=:'()<>;]";
            Pattern pattern = Pattern.compile(str);
            Matcher matcher;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<DOCNO>")) {
                    line = line.replace("<DOCNO>", "");
                    line = line.replace("</DOCNO>", "");
                    line = line.trim();
                    mapWriter.append(String.valueOf(currentDocId)).append(",").append(line).append(System.lineSeparator());
                    continue;
                }

                if (line.contains("</DOC>")) {
                    currentDocId++;
                    continue;
                }

                if (line.contains("<HEADLINE>") || line.contains("<TEXT>")) {
                    requireRecord = true;
                    continue;
                }

                if (line.contains("</HEADLINE>") || line.contains("</TEXT>")) {
                    requireRecord = false;
                    continue;
                }

                if (line.contains("<P>") || line.contains("</P>")) {
                    continue;
                }

                if (requireRecord) {
                    String[] words = line.split(" ");
                    for (String word : words) {
                        matcher = pattern.matcher(word);
                        word = matcher.replaceAll("").toLowerCase().trim();
                        if(word.equals("")) continue;
                        if (stoplist.contains(word)) continue;
                        System.out.println(word);
                        updateInvlists(currentDocId, word);
                    }
                }
            }
            new Thread(new Runnable() {

				@Override
				public void run() {
		            try {
						writeInvlistToFile();
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
            	
            }).start();
            mapWriter.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeInvlistToFile() throws IOException {
        int startPointer = 0;
        File lexicon = new File("lexicon");
        File invlists = new File("invlists");
        lexicon.delete();
        lexicon.createNewFile();
        invlists.delete();
        invlists.createNewFile();

        int clock = 0;
        try (BufferedWriter lexiconWriter = new BufferedWriter(new FileWriter(lexicon));
             DataOutputStream outputInvList = new DataOutputStream(new FileOutputStream(invlists))) {

            int totalSize = invertedList.size();

            for (String s : invertedList.keySet()) {
                Hashtable<Integer, Integer> tempTable = invertedList.get(s);
                outputInvList.writeInt(tempTable.size());
                for (Integer i : tempTable.keySet()) {
                    outputInvList.writeInt(i);
                    outputInvList.writeInt(tempTable.get(i));
                }
                outputInvList.flush();
                lexiconWriter.write(s + "," + startPointer + System.lineSeparator());
                lexiconWriter.flush();

                startPointer = outputInvList.size();

                System.out.println(clock + "/" + totalSize);
                clock++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static boolean checkFileExists(String... filePath) {
        for (String path : filePath) {
            if (path == null) continue;
            File file = new File(path);
            if (!file.exists()) {
                System.out.println(path + " does not exist.");
                return false;
            }
        }
        return true;
    }

    private static HashSet<String> fetchStopList(String stoplistPath) {
        if (stoplistPath == null) return new HashSet<>();
        HashSet<String> list = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(stoplistPath))) {
            String term;
            while ((term = br.readLine()) != null)
                list.add(term);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void updateInvlists(Integer documentId, String str) {
        if (invertedList.containsKey(str)) {
            Hashtable<Integer, Integer> tempTable = invertedList.get(str);
            if (tempTable.containsKey(documentId)) {
                Integer tempFrequency = tempTable.get(documentId);
                tempFrequency += 1;
                tempTable.put(documentId, tempFrequency);
                invertedList.put(str, tempTable);
            } else {
                tempTable.put(documentId, 1);
                invertedList.put(str, tempTable);
            }
        } else {
            Hashtable<Integer, Integer> tempTable = new Hashtable<>();
            tempTable.put(documentId, 1);
            invertedList.put(str, tempTable);
        }
    }

    private static void printUsage() {
        System.out.println("invalid input.");
        System.out.println("Usage: index [-p] <sourceFile> or index [-s <stoplist>] [-p] <sourcefile>.");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            // for test
            //indexing("./src/latimes-100");
            return;
        }
        if (args.length == 2 && args[0].equals("-p")) {
            //index [-p] <sourceFile>
            indexing(args[1]);
        } else if (args.length == 4 && args[0].equals("-s") && args[2].equals("-p")) {
            //index [-s <stoplist>] [-p] <sourcefile>
            indexing(args[3], args[1]);
        } else printUsage();

    }
}
