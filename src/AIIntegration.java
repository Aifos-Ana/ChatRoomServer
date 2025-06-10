import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIIntegration {
    private static final String MODEL_NAME = "llama3";

    public static String getAIResponse(String input) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection connection = null;
        
        try {
            // Enhanced JSON escaping
            String jsonPayload = String.format(
                "{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":false}",
                MODEL_NAME,
                input.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\t", "\\t")
            );

            URL url = new URL("http://localhost:11434/api/generate");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes("UTF-8"));
            }

            // Get appropriate stream (success/error)
            InputStream responseStream = connection.getResponseCode() < 400 
                ? connection.getInputStream() 
                : connection.getErrorStream();

            // Read response
            StringBuilder jsonResponse = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseStream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line.trim());
                }
            }

             //Debug output
            System.out.println("HTTP Status: " + connection.getResponseCode());
            System.out.println("Raw Response: " + jsonResponse);
            
            
            // Parse response
            if (connection.getResponseCode() == 200) {
                int start = jsonResponse.indexOf("\"response\":\"");
                if (start != -1) {
                    start += "\"response\":\"".length();
                    int end = jsonResponse.indexOf("\"", start);
                    if (end != -1) {
                        return jsonResponse.substring(start, end)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"");
                    }
                }
                return "Error: Could not parse AI response";
            } else {
                return "AI Error: " + jsonResponse.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorDetails = "Error: " + e.getMessage();
            if (connection != null) {
                try {
                    errorDetails += " [HTTP " + connection.getResponseCode() + "]";
                } catch (Exception ex) { /* Ignore secondary errors */ }
            }
            return errorDetails;
        }
    }
}