import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class NlpClassifierService {
    private String apiKey;
    private Path dirPath;
    NlpClassifierService(String apiKey, Path dirPath){
        this.apiKey = apiKey;
        this.dirPath = dirPath;
    }
    private String readFiles(){
        Path dirPath = this.dirPath;
        File folder = dirPath.toFile();
        File[] files = folder.listFiles();
        StringBuilder sb = new StringBuilder();

        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".md")) {
                String preview="";
                
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    // The read method returns the number of characters read, or -1 if the end of the stream is reached. till 200 characters
                    charsRead = reader.read(buffer, 0, 200);
        
                    if (charsRead != -1) {
                        // Convert the character array (or the part of it that was read) to a String
                        preview = new String(buffer, 0, charsRead);
                    } 
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                
                sb.append("Filename: ").append(file.getName()).append(" | Preview: ").append(preview).append("\n");
            }
        }
        return sb.toString();
    }
    
    public CompletableFuture<Map<String, List<String>>> categorizeFiles(){
        String previews = readFiles();
        String prompt = "Analyze the provided file previews and group the files into logical categories based on their content or purpose.
        
        Rules:
        - Respond ONLY with a valid raw JSON object.
        - Do NOT include any explanations, comments, or markdown.
        - Each key must be a category name (string).
        - Each value must be an array of exact filenames (strings), preserving original spelling, case, and extensions.
        - Each filename must appear in exactly one category.
        - Do not create empty categories.
        - If a file does not clearly belong anywhere, place it under \"Uncategorized\".
        - Use short, human-readable category names (1–3 words)
        - Minimize number of categories, limit to a maximum of 15;
        
        Output format example:
        {
          \"Category A\": [\"file1.md\", \"file2.md\"],
          \"Category B\": [\"image1.md\"]
        }"
        
        GeminiHelper helper = new GeminiHelper(apiKey);
        
        // asynchronous helper call uses Java's built-in ForkJoin pool.
        helper.getResponse(prompt)
              .thenAccept(resultVal -> {
                  // This block automatically runs when the network call finishes
                  Platform.runLater(() -> input.setText(resultVal));
              })
              .exceptionally(ex -> {
                  // This catches any network/JSON errors seamlessly
                  Platform.runLater(() -> {
                      Alert alert = new Alert(Alert.AlertType.ERROR, "API Error: " + ex.getMessage());
                      alert.showAndWait();
                      sideBar.setDisable(false);
                      appBar.setDisable(false);
                  });
                  return null;
            });
    }
    
    
}
