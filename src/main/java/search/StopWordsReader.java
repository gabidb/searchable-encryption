package search;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class StopWordsReader {

    /**

     Reads the stop words file from the resources directory and returns a list of stop words. Stop words are determined by the
     stopWords.csv file, which is a list of common words that typically do not contribute to the meaning of a query.
     @return a list of stop words
     */
    protected static List<String> readStopWords() {
        List<String> stopWords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(StopWordsReader.class.getResourceAsStream("/stopWords.csv"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split(",");
                for (String word : words) {
                    stopWords.add(word.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read stop words file: " + e.getMessage());
        }
        return stopWords;
    }
}
