import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import org.markdwn.GeminiHelper; 

public class NlpClassifierService {
    private final String apiKey;
    private final Path dirPath;

    public NlpClassifierService(String apiKey, Path dirPath) {
        this.apiKey = apiKey;
        this.dirPath = dirPath;
    }

    private String readFiles() {
        File folder = dirPath.toFile();
        File[] files = folder.listFiles();
        StringBuilder sb = new StringBuilder();

        if (files == null) return "";

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".md")) {
                String preview = "";
                char[] buffer = new char[200];

                try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                    int charsRead = reader.read(buffer, 0, 200);
                    if (charsRead != -1) {
                        preview = new String(buffer, 0, charsRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sb.append("Filename: ")
                  .append(file.getName())
                  .append(" | Preview: ")
                .append(preview)
                  .append("\n");
            }
        }
        return sb.toString();
    }

    public CompletableFuture<Map<String, List<String>>> categorizeFiles() {
        String previews = readFiles();
        String prompt = """
            Analyze the provided file previews and group the files into logical categories based on their content or purpose.
    
            Rules:
            - Respond ONLY with a valid raw JSON object.
            - Do NOT include any explanations, comments, or markdown.
            - Each key must be a category name (string).
            - Each value must be an array of exact filenames (strings), preserving original spelling, case, and extensions.
            - Each filename must appear in exactly one category.
            - Do not create empty categories.
            - If a file does not clearly belong anywhere, place it under "Uncategorized".
            - Use short, human-readable category names (1–3 words)
            - Minimize number of categories, limit to a maximum of 15;
    
            File previews:
            %s
    
            Output format example:
            {
              "Category A": ["file1.md", "file2.md"],
              "Category B": ["image1.md"]
            }
            """.formatted(previews);
    
        GeminiHelper helper = new GeminiHelper(apiKey);
    
        CompletableFuture<Map<String, List<String>>> future =
            helper.getResponse(prompt)
                .thenApply((String jsonRes) -> {
                    // Strip markdown formatting if the LLM hallucinated it
                    String cleanJson = jsonRes.replace("```json", "").replace("```", "").trim();
                    Gson gs = new Gson();
                    Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
                    return gs.fromJson(cleanJson, mapType);
                });
    
        return future.exceptionally(ex -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "API Error: " + ex.getMessage());
                alert.showAndWait();
            });
            return java.util.Collections.<String, List<String>>emptyMap();
        });
    }
}