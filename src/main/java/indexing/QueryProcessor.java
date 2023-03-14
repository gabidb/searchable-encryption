package indexing;

import encryption.AES;

import java.util.*;
import java.util.stream.Collectors;

public class QueryProcessor {
    public static Set<String> processQuery(String query, InvertedIndex index, int n) {
        List<String> tokens = Tokenizer.tokenizeQuery(query);
        n = Math.min(tokens.size(), n);
        for (int i = n; i > 0; i--) {
            List<String> ngrams = NgramGenerator.generateNgrams(tokens, i)
                    .stream()
                    .map(ngram -> index.aes.encrypt(ngram))
                    .collect(Collectors.toList());;
            Set<String> documentIds = new HashSet<>();
            for (String ngram : ngrams) {
                List<String> documents = index.getDocumentsByTerm(ngram);
                documentIds.addAll(documents);
            }
            if (!documentIds.isEmpty()) {
                return documentIds;
            }
        }
        return Collections.emptySet();
    }
}
