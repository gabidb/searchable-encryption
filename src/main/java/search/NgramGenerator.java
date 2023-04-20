package search;

import java.util.*;
import java.util.logging.Logger;

class NgramGenerator {

    private final static Logger logger = Logger.getLogger(NgramGenerator.class.getName());

    /**
     * Generates n-grams for a given string.
     * @param tokens the tokenized file
     * @param n the length of each n-gram
     * @return a map of n-grams
     */
    protected static Map<String, Set<Integer>> generateNgrams(Map<String, Set<Integer>> tokens, int n) {
        Map<String, Set<Integer>> ngrams = new HashMap<>();

        if (n == 1 || n == 2) {
            logger.warning("N must be either 0 or an integer at least 3!");
            logger.info("Setting n = 0");
            n = 0;
        }

        for (Map.Entry<String, Set<Integer>> token : tokens.entrySet()) {
            int ngram_length = Math.min(n, token.getKey().length());
            // The regex ?<=\\G{n number of dots} splits every string into multiple strings of length n.
            String[] token_parts = token.getKey()
                    .split("(?<=\\G" + String.join("", Collections.nCopies(ngram_length, ".")) + ")");
            for (String s: token_parts) {
                if (s.length() > 1) {
                    ngrams.put(s, token.getValue());
                }
            }
        }
        return ngrams;
    }

    /**
     * Generates n-grams for a given string.
     * @param tokens the tokenized file
     * @param n the length of each n-gram
     * @return a list of n-grams
     */
    protected static List<String> generateNgramsQuery(List<String> tokens, int n) {
        List<String> ngrams = new ArrayList<>();

        if (n == 0) {
            return tokens;
        } else if (n == 1 || n == 2) {
            logger.warning("N must be either 0 or an integer at least 3!");
            logger.info("Setting n = 0");
            return tokens;
        }

        for (String token: tokens) {
            int ngram_length = Math.min(n, token.length());
            // The regex ?<=\\G{n number of dots} splits every string into multiple strings of length n.
            String[] token_parts = token
                    .split("(?<=\\G" + String.join("", Collections.nCopies(ngram_length, ".")) + ")");
            for (String s: token_parts) {
                if (s.length() > 1) {
                    ngrams.add(s);
                }
            }
        }

        return ngrams;
    }
}
