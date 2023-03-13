package queryprocessing;

import encryption.AES;
import indexing.InvertedIndex;
import indexing.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class QueryProcessor {
    private final InvertedIndex index;
    private final AES aes;

    public QueryProcessor(InvertedIndex index, AES aes) {
        this.index = index;
        this.aes = aes;
    }

//    public List<String> processQuery(String query) {
//        List<String> tokens = Tokenizer.tokenize(query);
//        List<String> documentIds = new ArrayList<>();
//        for (String token : tokens) {
//            String encrypted_token = aes.encrypt(token);
//            List<String> documents = index.getDocumentsByTerm(encrypted_token, aes);
//            documentIds.addAll(documents);
//        }
//        return documentIds;
//    }
}
