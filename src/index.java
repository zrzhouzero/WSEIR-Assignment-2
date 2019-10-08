import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class index {

    private static Hashtable<String, Hashtable<Integer, Integer>> invertedList;
    private static int currentDocId;

    static {
        invertedList = new Hashtable<>();
        currentDocId = 1;
    }

    public static void indexing(String source, String stop, boolean print) throws IOException {
        // check if files exist
        if (!checkFileExists(source, stop)) return;
        // fetch terms stored in stoplist
        HashSet<String> stoplist = fetchStopList(stop);
        split(new File(source));
        File docs = new File("docs/");
        File map = new File("map");
        map.delete();
        map.createNewFile();
        for (File file : docs.listFiles()) {
            parse(file, map,stoplist, print);
        }
        writeInvlistToFile();
    }

    private static void split(File source) {
        try {
            File docs = new File("docs");
            if (!docs.exists()){
                docs.mkdir();
            }
            BufferedReader reader = new BufferedReader(new FileReader(source));
            String line;
            StringBuffer content = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                content.append(line + System.lineSeparator());
                if (line.contains("</DOC>")){
                    String finalFileName = currentDocId + "";
                    currentDocId ++;
                    StringBuffer finalContent = content;
                    content = new StringBuffer();
                    new Thread(() -> {
                        try {
                            File splitFile = new File("docs/" + finalFileName);
                            if (splitFile.exists()) splitFile.delete();
                            splitFile.createNewFile();
                            BufferedWriter writer = new BufferedWriter(new FileWriter(splitFile));
                            writer.write(finalContent.toString());
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentDocId = 1;
    }

    private static void parse(File file, File map, HashSet<String> stoplist, boolean print) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean record = false;
            int count = 0;
            BufferedWriter mapWriter = new BufferedWriter(new FileWriter(map, true));
            String str = "[.,\"/\\?!@#$%^&*--+=:'()<>;]";
            Pattern pattern = Pattern.compile(str);
            Matcher matcher;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<DOCNO>")) {
                    line = line.replace("<DOCNO>", "");
                    line = line.replace("</DOCNO>", "");
                    line = line.trim();
                    mapWriter.append(file.getName()).append(",").append(line);
                    continue;
                }

                if (line.contains("</DOC>")) {
                    mapWriter.append(",").append(String.valueOf(count)).append(System.lineSeparator());
                    mapWriter.flush();
                    count = 0;
                    continue;
                }

                if (line.contains("<HEADLINE>") || line.contains("<TEXT>")) {
                    record = true;
                    continue;
                }

                if (line.contains("</HEADLINE>") || line.contains("</TEXT>")) {
                    record = false;
                    continue;
                }

                if (line.contains("<P>") || line.contains("</P>")) {
                    continue;
                }

                if (record) {
                    String[] words = line.split(" ");
                    for (String word : words) {
                        matcher = pattern.matcher(word);
                        word = matcher.replaceAll("").toLowerCase().trim();
                        if (word.equals("")) continue;
                        if (stoplist.contains(word)) continue;
                        if(print) System.out.println(word);
                        count += word.length();
                        updateInvlists(Integer.parseInt(file.getName()), word);
                    }
                }
            }
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

                String str = clock + "/" + totalSize;
                System.out.print(str);
                for (int i = 0; i < str.length(); i++) {
                    System.out.print("\b");
                }
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
            printUsage();
            return;
        }

        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        int index = 0;
        boolean print = false;
        String source, stop = null;
        if (arguments.contains("-p")) {
            print = true;
            arguments.remove("-p");
        }
        if (arguments.contains("-s")) {
            index = arguments.indexOf("-s") + 1;
            stop = arguments.get(index);
        }
        source = arguments.get(arguments.size() - 1);
        try {
            indexing(source, stop, print);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
