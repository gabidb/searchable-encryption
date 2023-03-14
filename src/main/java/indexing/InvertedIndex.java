package indexing;

import encryption.AES;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedIndex {

    protected AES aes;
    private Map<String, List<String>> index;

    public InvertedIndex(byte[] key) {
        aes = new AES(key);
        index = new HashMap<>();
    }

    public InvertedIndex(byte[] key, HashMap<String, List<String>> indexList) {
        aes = new AES(key);
        index = indexList;
    }

    public Map<String, List<String>> getIndex() {
        return index;
    }

    public void addToIndex(String documentID, File file, int n) {
        List<String> tokens = Tokenizer.tokenize(file);
        List<String> ngrams = NgramGenerator.generateNgrams(tokens, n);
        for (String ngram : ngrams) {
            String encrypted_token = aes.encrypt(ngram);
            if (!index.containsKey(encrypted_token)) {
                index.put(encrypted_token, new ArrayList<>());
            }
            index.get(encrypted_token).add(aes.encrypt(documentID));
        }
    }

    public List<String> getDocumentsByTerm(String term) {
        List<String> documentId_list = new ArrayList<>();
        if (index.containsKey(term)) {
            for (String documentID: index.get(term)) {
                documentId_list.add(aes.decrypt(documentID));
            }
        }
        return documentId_list;
    }
}
