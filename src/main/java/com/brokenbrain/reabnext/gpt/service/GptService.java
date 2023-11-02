package com.brokenbrain.reabnext.gpt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@Data
public class GptService {
    private static final String KEY = "";
    private String PROMPT = "";
    private long MAX_TOKENS = 1000;
    private float TEMPERATURE = 1;
    private String MODEL = "text-davinci-003";
    private Map<String, Object> outputGptMap;

    public Map<String, Object> getOutputGptMap() {
        return outputGptMap;
    }

    public Map<String, Object> gerarTreino() {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://api.openai.com/v1/completions");

            String jsonData = "{" +
                    "\"model\" : \"" + MODEL + "\"," +
                    "\"prompt\" : \"" + this.getPROMPT() + "\"," +
                    "\"max_tokens\" : " + MAX_TOKENS + "," +
                    "\"temperature\" : " + TEMPERATURE +
                    "}";

            StringEntity inputGptEntity = new StringEntity(jsonData);
            inputGptEntity.setContentType("application/json");
            post.setEntity(inputGptEntity);
            post.setHeader("Authorization", "Bearer " + KEY);

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("HTTP Status Code: " + response.getStatusLine().getStatusCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            // Read the API response into a StringBuilder
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line);
            }

            // Parse the JSON response into a Map
            ObjectMapper mapper = new ObjectMapper();
            outputGptMap = mapper.readValue(outputBuilder.toString(), new TypeReference<Map<String, Object>>() {
            });

            client.close();

            return outputGptMap;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }
}
