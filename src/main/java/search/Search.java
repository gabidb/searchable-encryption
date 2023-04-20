package search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import encryption.AES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Search {
    private final AES aes;
    private Map<String, Map<String, Set<Integer>>> invertedIndex;

    public Search(String index, byte[] key) {
        aes = new AES(key);
        convertIndex(index);
    }

    /**
     * Perform query search on an index.
     * @param query the query
     * @param n n must be the same n that the index was generated with.
     * @return list of document IDs that match the query
     */
    public List<String> search(String query, int n) {
        BM25Proximity bm25Proximity = new BM25Proximity(invertedIndex, aes);
        List<String> queryList = QueryProcessor.processQuery(query,n);
        Map<String, Double> rating = bm25Proximity.search(queryList.toArray(new String[0]));

        // Filter out entries with a value of 0 and sort the ratings by descending order of relevance score
        List<Map.Entry<String, Double>> sortedRating = rating.entrySet().stream()
                .filter(e -> e.getValue() != 0)
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());

        List<String> sortedDocuments = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedRating) {
            sortedDocuments.add(entry.getKey());
        }
        return sortedDocuments;
    }

    // Convert index from string to map
    private void convertIndex(String index) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            TypeReference<Map<String, Map<String, Set<Integer>>>> typeRef = new TypeReference<>() {};
            invertedIndex = mapper.readValue(index, typeRef);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
