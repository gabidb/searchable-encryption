package indexing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private static final List<String> stopWords = StopWordsReader.readStopWords("src/main/resources/stopWords.csv");

    protected static List<String> tokenize(File file) {
        List<String> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\W+"); // split by non-word characters
                for (String word : words) {
                    String word_lower = word.toLowerCase();
                    if (!stopWords.contains(word_lower)) { // check whether the word is in the stopWords list
                        tokens.add(word_lower);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }

    protected  static List<String> tokenizeQuery(String query) {
        List<String> tokens = new ArrayList<>();
        String[] words = query.split("\\W+"); // split by non-word characters
        for (String word : words) {
            String word_lower = word.toLowerCase();
            if (!stopWords.contains(word_lower)) { // check whether the word is in the stopWords list
                tokens.add(word_lower);
            }
        }
        return tokens;
    }
}
