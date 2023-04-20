package search;

import java.util.*;
class QueryProcessor {

    /*
        Tokenize query and generate n-grams if n > 0
     */
    protected static List<String> processQuery(String query, int n) {
        List<String> tokens = Tokenizer.tokenizeQuery(query);

        if (tokens.isEmpty()) {
            throw new IllegalStateException("Invalid tokenization result. Tokens cannot be empty.");
        }
        List<String> ngrams = new ArrayList<>();
        if (n > 0) {
            ngrams = NgramGenerator.generateNgramsQuery(tokens, n);
            if (ngrams.isEmpty()) {
                throw new IllegalStateException("Invalid n-gram generation result. Ngrams cannot be empty.");
            }
        } else {
            ngrams = tokens;
        }

        return ngrams;
    }
}
