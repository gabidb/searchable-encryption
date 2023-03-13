package indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StopWordsReader {
    public static List<String> readStopWords(String filePath) {
        List<String> stopWords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split(",");
                for (String word : words) {
                    stopWords.add(word.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read stop words file: " + e.getMessage());
        }
        return stopWords;
    }
}
