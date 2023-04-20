package search;

import java.io.*;
import java.util.*;

class Tokenizer {
    private static final List<String> stopWords = StopWordsReader.readStopWords();

    /**

     Tokenizes the given file by splitting it into words and removing the stop words. The resulting tokens are stored
     in a map along with their positions in the file.
     @param file The file to tokenize.
     @return A map containing the tokens and their positions.
     */
    protected static Map<String, Set<Integer>> tokenize(File file) {
        Map<String, Set<Integer>> tokens = new HashMap<>();
        int position = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\W+"); // split by non-word characters
                for (String word : words) {
                    String word_lower = word.toLowerCase();
                    if (!stopWords.contains(word_lower)) { // check whether the word is in the stopWords list
                        position++;
                        if(!tokens.containsKey(word_lower)){
                            tokens.put(word_lower, new HashSet<>());
                        }
                        tokens.get(word_lower).add(position);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }

    /**

     Tokenizes the given query by splitting it into words and removing any stop words. Stop words are determined by the
     stopWords list, which is a list of common words that typically do not contribute to the meaning of a query. The resulting
     list of tokens is returned.
     @param query the query string to tokenize
     @return a list of tokens extracted from the query string, with stop words removed
     */
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

