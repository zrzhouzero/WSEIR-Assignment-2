import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Summary {

    private int documentId;
    private String query;
    private String stoplistPath;
    private String directoryPath;

    /**
     * to instantiate an object to store the relevant document
     *
     * @param documentId the id of the document being summarised
     */
    public Summary(int documentId, String query, String directoryPath, String stoplistPath) {
        this.documentId = documentId;
        this.query = query;
        this.stoplistPath = stoplistPath;
        this.directoryPath = directoryPath;
    }


    /**
     * to generate a "static" or abstract summary based on the document
     * as not all the documents in library have abstracts
     * so in this project, we consider the first paragraph as the summary for convenience
     *
     * @return the generated summary
     */
    public String generateStaticSummary() {
        return "";
    }


    /**
     * to generate a "dynamic" or extract summary based on both the documents and the query
     * return the three consecutive sentences with the largest ranking value
     *
     * @return the generated summary
     */
    public String generateDynamicSummary() {
        StringBuilder builder = new StringBuilder();
        File file = new File(directoryPath + documentId);
        try (Scanner sc = new Scanner(new FileReader(file))) {

            boolean isStart = false;
            while (sc.hasNextLine()) {
                String temp = sc.nextLine();
                if (!isStart) {
                    if (temp.contains("<TEXT>")) {
                        isStart = true;
                    }
                } else {
                    if (temp.contains("</P>") || temp.contains("<P>")) {
                        continue;
                    }
                    if (temp.contains("</TEXT>")) {
                        isStart = false;
                        continue;
                    }
                    builder.append(temp);
                }
            }

            HashMap<Integer, SentenceStructure> sentenceMap = new HashMap<>();
            int sentenceId = 0;
            HashSet<String> stoplist = new HashSet<>();
            try (Scanner scanner = new Scanner(new FileReader(new File(stoplistPath)))) {
                while (scanner.hasNextLine()) {
                    stoplist.add(scanner.nextLine());
                }
            }

            BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.UK);
            String source = builder.toString();
            iterator.setText(source);
            int start = iterator.first();

            for (int end = iterator.next();
                 end != BreakIterator.DONE;
                 start = end, end = iterator.next()) {
                sentenceId++;
                String origin = source.substring(start, end);

                String str = "[.,\"/?!@#$%^&*--+=:'()<>;]";
                Pattern pattern = Pattern.compile(str);
                Matcher matcher;
                matcher = pattern.matcher(origin);
                String temp = matcher.replaceAll("").toLowerCase().trim();
                String[] split = temp.split(" ");

                int length = 0;
                int termOccurrence = 0;
                for (String s : split) {
                    if (s.equals(this.query)) {
                        termOccurrence++;
                    }
                    if (!stoplist.contains(s)) {
                        length += s.length();
                    }
                }
                sentenceMap.put(sentenceId, new SentenceStructure(origin, length, termOccurrence));
            }

            return findBestSentence(sentenceMap);

        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }


    /**
     * find the three consecutive sentences which have the greatest value for summary
     * the value is (term occurrence) ^ 2 / length
     *
     * @param map the input map with sentence ID, and its content, length without stop words, and query term occurrence
     * @return the string with the best value
     */
    private String findBestSentence(HashMap<Integer, SentenceStructure> map) {
        if (map.size() == 0) {
            return "The article has no suitable summary.";
        } else if (map.size() == 1) {
            return map.get(1).source;
        } else if (map.size() == 2) {
            return map.get(1).source + map.get(2).source;
        } else {
            int index = 1;
            double value = 0;
            for (int i = 1; i < map.size() - 1; i++) {
                double temp = (double) map.get(i).termOccurrence * map.get(i).termOccurrence / map.get(i).length;
                temp += (double) map.get(i + 1).termOccurrence * map.get(i + 1).termOccurrence / map.get(i + 1).length;
                temp += (double) map.get(i + 2).termOccurrence * map.get(i + 2).termOccurrence / map.get(i + 2).length;
                if (temp > value) {
                    index = i;
                    value = temp;
                }
            }
            return map.get(index).source + map.get(index + 1).source + map.get(index + 2).source;
        }
    }


    /**
     * a structure to store the information of a sentence while processing
     */
    private static class SentenceStructure {
        String source;
        int length;
        int termOccurrence;

        SentenceStructure(String source, int length, int termOccurrence) {
            this.source = source;
            this.length = length;
            this.termOccurrence = termOccurrence;
        }
    }


    public static void main(String[] args) {
        Summary s = new Summary(1, "society","src/docs/", "src/stoplist");
        String dynamic = s.generateDynamicSummary();
        System.out.println(dynamic);
    }

}
