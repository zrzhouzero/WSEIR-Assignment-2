import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class index {

    private static Hashtable<String, Hashtable<Integer, Integer>> invertedList;
    private static int currentDocId;
    private static ExecutorService fixedThreadPool;

    static {
        fixedThreadPool = Executors.newFixedThreadPool(200);
        invertedList = new Hashtable<>();
        currentDocId = 1;
    }

    public static void indexing(String source, String stop, boolean print) throws IOException {
        // check if files exist
        if (!checkFileExists(source, stop)) return;
        // fetch terms stored in stoplist
        HashSet<String> stoplist = fetchStopList(stop);
        File map = new File("map");
        if (map.exists()) map.delete();
        map.createNewFile();
        parse(new File(source), map, stoplist, print);
        fixedThreadPool.shutdown();
        while(Thread.activeCount() != 1){
            System.out.print("Doing File split and parsing, active threads: " + Thread.activeCount() + "\r");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        writeInvlistToFile();
    }

    private static void parse(File file, File map, HashSet<String> stoplist, boolean print) {
        try {
            File docs = new File("docs");
            if (!docs.exists() || !docs.isDirectory()){
                docs.mkdir();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean record = false;
            int count = 0;
            BufferedWriter mapWriter = new BufferedWriter(new FileWriter(map, true));
            String str = "[.,\"/\\?!@#$%^&*--+=:'()<>;]";
            Pattern pattern = Pattern.compile(str);
            Matcher matcher;
            StringBuffer content = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                content.append(line + System.lineSeparator());
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
                    String finalFileName = currentDocId + "";
                    currentDocId ++;
                    StringBuffer finalContent = content;
                    content = new StringBuffer();
                    fixedThreadPool.execute(() -> {
                        try {
                            File splitFile = new File(docs.getAbsolutePath() +"//"+ finalFileName);
                            if (!splitFile.exists()) splitFile.createNewFile();
                            BufferedWriter writer = new BufferedWriter(new FileWriter(splitFile));
                            writer.write(finalContent.toString());
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
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
                        updateInvlists(currentDocId, word);
                    }
                }
            }
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
                String str = clock + "/" + totalSize;
                System.out.print(str + "\r");
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
        String usage = "index Usage:\n" +
                "    java index [-s <stoplist>] [-p] <sourcefile>.\n" +
                "    -s <stoplist> is an optional argument which indicate a stoplist.\n" +
                "    -p is an optional argument that determines whether print the filtered term to screen\n" +
                "    <sourcefile> is the path of the source file which should be always the last argument.\n" +
                "\n" +
                "    eg. java index -s stoplist latimes-100";
        System.out.println(usage);
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
