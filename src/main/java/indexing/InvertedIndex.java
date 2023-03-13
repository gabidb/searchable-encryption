package indexing;

import encryption.AES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedIndex {
    private Map<String, List<String>> index;

    public InvertedIndex() {
        index = new HashMap<>();
    }

    public InvertedIndex(HashMap<String, List<String>> indexList) {
        index = indexList;
    }

    public Map<String, List<String>> getIndex() {
        return index;
    }

    public void addToIndex(String documentID, List<String> tokens, AES aes) {
        for (String token : tokens) {
            String encrypted_token = aes.encrypt(token);
            if (!index.containsKey(encrypted_token)) {
                index.put(encrypted_token, new ArrayList<>());
            }
            index.get(encrypted_token).add(aes.encrypt(documentID));
        }
    }

    public List<String> getDocumentsByTerm(String term, AES aes) {
        List<String> documentId_list = new ArrayList<>();
        if (index.containsKey(term)) {
            for (String documentID: index.get(term)) {
                documentId_list.add(aes.decrypt(documentID));
            }
        }
        return documentId_list;
    }
}
