package Model;

import java.net.URI;
import java.net.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class AssistantIA {
    // ⚠️ REMPLACE PAR TA CLÉ RÉELLE ICI
    private static final String API_KEY = "TA_CLE_API_GEMINI";
    private static final String URL_API = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static String optimiserContenu(String texteBrut) {
        try {
            // Nettoyage du texte pour éviter les problèmes de JSON
            String texteSecurise = texteBrut.replace("\"", "\\\"").replace("\n", " ");

            // Construction propre du JSON avec l'objet JSONObject
            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject parts = new JSONObject();
            JSONArray partsArray = new JSONArray();
            
            parts.put("text", "Tu es un assistant à l'ESITEC. Reformule ce cours pour un cahier de texte de manière pro et courte : " + texteSecurise);
            partsArray.put(parts);
            
            JSONObject contentObj = new JSONObject();
            contentObj.put("parts", partsArray);
            contents.put(contentObj);
            jsonBody.put("contents", contents);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Affichage console pour débugger en cas de souci
            System.out.println("Réponse API : " + response.body());

            JSONObject jsonResponse = new JSONObject(response.body());

            // Vérification si la réponse contient bien le texte attendu
            if (jsonResponse.has("candidates")) {
                return jsonResponse.getJSONArray("candidates")
                                   .getJSONObject(0)
                                   .getJSONObject("content")
                                   .getJSONArray("parts")
                                   .getJSONObject(0)
                                   .getString("text");
            } else if (jsonResponse.has("error")) {
                return "Erreur API : " + jsonResponse.getJSONObject("error").getString("message");
            } else {
                return "L'IA n'a pas pu répondre. Vérifiez la console.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur technique : " + e.getMessage();
        }
    }
}