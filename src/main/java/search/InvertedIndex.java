package search;

import encryption.AES;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class InvertedIndex {

    private AES aes;
    private Map<String, Map<String, Set<Integer>>> index;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public InvertedIndex(byte[] key) {
        aes = new AES(key);
        index = new HashMap<>();
    }

    public InvertedIndex(byte[] key, Map<String, Map<String, Set<Integer>>> indexMap) {
        aes = new AES(key);
        index = indexMap;
    }

    public Map<String, Map<String, Set<Integer>>> getIndex() {
        return index;
    }

    /**
     * Adds a new file to the inverted index.
     * The index is created by generating n-grams from the file content, and adding
     * each n-gram occurrence to the index for the corresponding document.
     *
     * @param documentID the ID of the document to add to the index
     * @param file the file containing the document's content
     * @param n the size of the n-grams to generate
     * @throws Exception if an error occurs while adding the file to the index
     */
    public void add(String documentID, File file, int n) throws Exception {
        if (file == null) {
            throw new IOException("Either file does not not exist, or the file cannot be processed!");
        }

        Map<String, Set<Integer>> tokens = Tokenizer.tokenize(file);
        if (tokens.isEmpty()) {
            throw new IllegalStateException("Invalid tokenization result. Tokens cannot be empty.");
        }

        Map<String, Set<Integer>> ngrams = NgramGenerator.generateNgrams(tokens, n);
        if (ngrams.isEmpty()) {
            throw new IllegalStateException("Invalid n-gram generation result. Ngrams cannot be empty.");
        }

        for (Map.Entry<String, Set<Integer>> ngram : ngrams.entrySet()) {

            String encrypted_token = aes.encrypt(ngram.getKey());
            String encrypted_documentID = aes.encrypt(documentID);

            if (!index.containsKey(encrypted_token)) {
                index.put(encrypted_token, new HashMap<>());
            }
            if (!index.get(encrypted_token).containsKey(encrypted_documentID)) {
                index.get(encrypted_token).put(encrypted_documentID, new HashSet<>());
            }
            index.get(encrypted_token).get(encrypted_documentID).addAll(ngram.getValue());
        }
        logger.info("Document with ID: " + documentID + " added to the index!");
    }

    /**
     * Removes all occurrences of the specified document ID from the index.
     * This method should be called when a document is deleted from the database.
     *
     * @param documentID the ID of the document to remove from the index
     * @throws Exception if an error occurs while deleting the file from the index
     */
    public void delete(String documentID) throws Exception {
        String encrypted_documentID = aes.encrypt(documentID);

        for (Map.Entry<String, Map<String, Set<Integer>>> term : index.entrySet()) {
            // remove the entry for the given documentID, if it exists
            term.getValue().remove(encrypted_documentID);
        }
    }

    /**
     * Updates the inverted index with new document content.
     * This method first deletes the old document information from the index
     * and then adds the new information for the same document.
     *
     * @param documentID the ID of the document to update in the index
     * @param file the new file content to add to the index
     * @param n the length of n-grams to use for tokenization
     * @throws Exception if an I/O error occurs while reading the file
     */
    public void update(String documentID, File file, int n) throws Exception{
        delete(documentID);
        add(documentID, file, n);
    }
}
