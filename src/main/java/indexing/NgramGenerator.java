package indexing;

import java.util.ArrayList;
import java.util.List;

public class NgramGenerator {
    /**
     * Generates n-grams for a given string.
     * @param tokens the tokenized file
     * @param n the length of each n-gram
     * @return a list of n-grams
     */
    protected static List<String> generateNgrams(List<String> tokens, int n) {
        List<String> ngrams = new ArrayList<>();

        for (int j = n; j > 1 ; j--) {
            StringBuilder sb = new StringBuilder(); // use a StringBuilder to concatenate n-grams
            for (int i = 0; i < tokens.size(); i++) {
                if (i < j) {
                    sb.append(tokens.get(i)).append(" ");
                } else {
                    ngrams.add(sb.toString().trim()); // add the previous n-gram to the list
                    sb.delete(0, sb.indexOf(" ") + 1); // remove the first token from the StringBuilder
                    sb.append(tokens.get(i)).append(" "); // add the current token to the StringBuilder
                }
            }
            ngrams.add(sb.toString().trim()); // add the last n-gram to the list
        }
        return ngrams;
    }

}
