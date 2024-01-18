package search;

import encryption.AES;

import java.util.*;

public class BM25Proximity {

    private final AES aes;
    private final Map<String, Integer> doc_lengths; // Stores document lengths
    private final Map<String, Map<String, Set<Integer>>> inverted_index;
    private int num_docs; // Number of documents
    private final double k1 = 1.2; // BM25 parameter k1. Using default value
    private final double b = 0.75; // BM25 parameter b. Using default value
    private final double k3 = 2.0; // BM25 parameter k3. Using default value

    public BM25Proximity(Map<String, Map<String, Set<Integer>>> index, AES aes) {
        this.aes = aes;
        this.inverted_index = index;
        this.doc_lengths = new HashMap<>();
        getNumberOfDocuments(index);
    }

    // Compute the number of documents in the index and store document lengths
    private void getNumberOfDocuments(Map<String, Map<String, Set<Integer>>> index) {
        int num_docs = 0;

        for (Map.Entry<String, Map<String, Set<Integer>>> term : index.entrySet()) {
            Map<String, Set<Integer>> doc_index = term.getValue();
            for (Map.Entry<String, Set<Integer>> document : doc_index.entrySet()) {
                String documentID = aes.decrypt(document.getKey()).trim();
                Set<Integer> positions = document.getValue();

                // Increment the number of documents only if it's a new document
                if (!doc_lengths.containsKey(documentID)) {
                    num_docs++;
                }

                // Find the maximum position of the current term in the document
                int max_position = Collections.max(positions);

                // Update the document length with the highest position.
                // The term with the highest position is a good approximation of the document length
                doc_lengths.put(documentID, Math.max(doc_lengths.getOrDefault(documentID, 0), max_position));

            }
        }
        this.num_docs = num_docs;
    }

    /**
     * Searches the document collection for the given query terms and calculates a relevance score for each document.
     * The relevance score is based on the BM25 scoring model, which takes into account term frequency, inverse document frequency,
     * document length normalization, and term proximity.
     *
     * @param query_terms An array of query terms to search for in the document collection.
     * @return A map of document IDs to their corresponding relevance scores, where a higher score indicates higher relevance.
     */
    public Map<String, Double> getBM25score(String[] query_terms) {
        Map<String, Double> document_scores = new HashMap<>();

        for (Map.Entry<String, Integer> entry : doc_lengths.entrySet()) {
            String documentID = entry.getKey();
            double doc_score = 0.0;

            for (String term : query_terms) {
                Map<String, Set<Integer>> doc_index = inverted_index.get(aes.encrypt(term));
                if (doc_index == null) {
                    continue; // the term does not appear in the index
                }

                Set<Integer> positions = doc_index.get(aes.encrypt(documentID));
                if (positions == null) {
                    continue; // the term does not appear in the current document
                }

                int tf = positions.size();
                int doc_length = doc_lengths.get(documentID);
                int doc_freq = doc_index.size();

                double idf = getIDF(doc_freq);
                double avg_doc_len = getAvgDocLength();
                double query_term_weight = getQueryTermWeight(term, query_terms);
                double proximity_score = getProximityScore(query_terms, documentID);
                double term_freq = tf * (k1 + 1) / (tf + k1 * (1 - b + b * doc_length / avg_doc_len));
                double term_weight = term_freq * idf * query_term_weight;
                doc_score += term_weight * proximity_score;
            }
            System.out.println(documentID + "    " + doc_score);
            document_scores.put(documentID, doc_score);
        }
        return document_scores;
    }

    // Calculate the IDF (Inverse Document Frequency) for a given term
    private double getIDF(int doc_freq) {
        /* The IDF formula used in BM25 has a drawback:
         When used for terms appearing in more than half of the corpus,
         the value would come out as negative value,
         resulting in the overall score to become negative.
         Fix: add 1 to ensure always positive value
        */
        return Math.log(1 + (num_docs - doc_freq + 0.5) / (doc_freq + 0.5));
    }


    // Calculate the average document length
    private double getAvgDocLength() {
        int total_doc_length = 0;
        for (int doc_length : doc_lengths.values()) {
            total_doc_length += doc_length;
        }
        return (double) total_doc_length / doc_lengths.size();
    }

    // Calculate the query term weight for a given term in the query
    private double getQueryTermWeight(String query_term, String[] query_terms) {
        int freq = 0;
        for (String term : query_terms) {
            if (term.equals(query_term)) {
                freq++;
            }
        }
        return (k3 + 1) * freq / (k3 + freq);
    }

    // Compute the proximity score between the query terms in a given document
    private double getProximityScore(String[] query_terms, String documentID) {
        double score = 0.0;
        Map<String, Set<Integer>> map = new HashMap<>();

        for (String query : query_terms) {
            Map<String, Set<Integer>> map1 = inverted_index.get(aes.encrypt(query));

            if (map1 != null) {
                map.put(query, inverted_index.get(aes.encrypt(query)).getOrDefault(aes.encrypt(documentID), Collections.emptySet()));
            } else {
                continue;
            }
        }
        score += getProximityScoreForDocID(map, query_terms);
        return score;
    }

    /*
        This method calculates a proximity score that reflects how close the query terms appear together in the document.
        The closer the query terms appear, the higher the proximity score,
        which may suggest a higher relevance of the document for the given query.
     */
    double getProximityScoreForDocID(Map<String, Set<Integer>> term_index, String[] query_terms) {
        double score = 1.0;
        for (int i = 0; i < query_terms.length - 1; i++) {
            String term1 = query_terms[i];
            String term2 = query_terms[i + 1];
            List<Integer> term1_positions = new ArrayList<>(term_index.getOrDefault(term1, Collections.emptySet())); // sorted in ascending order
            List<Integer> term2_positions = new ArrayList<>(term_index.getOrDefault(term2, Collections.emptySet())); // sorted in ascending order

            if (term1_positions.isEmpty() || term2_positions.isEmpty()) {
                continue;
            }

            double proximity = Double.MAX_VALUE;

            for (int j = 0; j < term1_positions.size(); j++) {
                int pos1 = term1_positions.get(j);
                int closestPos2 = Integer.MAX_VALUE;

                for (int k = 0; k < term2_positions.size(); k++) {
                    int pos2 = term2_positions.get(k);

                    // the first occurrence in term2_positions that is bigger than the current position is the closest one
                    if (pos2 >= pos1) {
                        closestPos2 = pos2;
                        break;
                    }
                }

                if (closestPos2 != Integer.MAX_VALUE) {
                    proximity = Math.min(proximity, closestPos2 - pos1);
                }
            }

            if (proximity != Double.MAX_VALUE) {
                score *= 1.0 + Math.exp(-proximity); // the smaller the proximity, the higher the score
            }
        }
        return score;
    }

}
