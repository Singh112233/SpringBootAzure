package com.springboot.azureapp.service;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.*;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import com.azure.core.credential.AzureKeyCredential;
import com.springboot.azureapp.exception.DemoModelNotFoundException;
import com.springboot.azureapp.model.DemoModel;
import com.springboot.azureapp.repository.DemoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
@Service
public class DemoService {

    @Autowired
    private DemoRepository demoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public DemoModel addEntries(DemoModel demoModel) {
        return demoRepository.save(demoModel);
    }

    public DemoModel getEntries(Long id) throws DemoModelNotFoundException {
        Optional<DemoModel> demoModel = demoRepository.findById(id);
        if (!demoModel.isPresent()) {
            throw new DemoModelNotFoundException("Demo Model Not Found ");
        } else
            return demoModel.get();
    }


    public List<DemoModel> getAllEntries() {
        return demoRepository.findAll();
    }


    public DemoModel getEntriesByName(String name) {
        return demoRepository.findByTitle(name);
    }

    @Value("${azure.formrecognizer.endpoint}")
    private String formRecognizerEndpoint;
    public String extractDataFromFile(MultipartFile file) throws Exception {

        InputStream inputStream = file.getInputStream();
        long length = file.getSize();
        String contentType = file.getContentType();

       // ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();
        TokenCredential credential = new ManagedIdentityCredentialBuilder()
                .build();

        // Initialize the DocumentAnalysisClient
        DocumentIntelligenceClient documentIntelligenceClient = new DocumentIntelligenceClientBuilder()
                .endpoint(formRecognizerEndpoint)
                .credential(credential)
                .buildClient();

        // sample document
        String modelId = "prebuilt-layout";
        BinaryData binaryData = BinaryData.fromStream(inputStream, length);
        SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeLayoutPoller =
                documentIntelligenceClient.beginAnalyzeDocument(modelId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new AnalyzeDocumentRequest().setBase64Source(binaryData.toBytes()));

        AnalyzeResult result = analyzeLayoutPoller.getFinalResult().getAnalyzeResult();

        String extractedText;
        extractedText = result.getContent();
      //  extractedText = extractedText.replace("\n", " ").replace("\r", "").trim();

        return extractedText;
    }
//    public String getChatCompletion(String extractedData) throws JsonProcessingException {
//        RestTemplate restTemplate = new RestTemplate();
//        String prompt = "You are a Purchase order analysis expert. Extract the following information from the provided purchase order document. Ensure that the information is completely accurate and that you do not infer or guess missing values. If a specific detail is not present in the document, leave it as an empty string in the response. Do not include any extra text or additional information.\n" +
//                "The information should be returned in the following JSON structure:\n" +
//                "{\n" +
//                "    \"Purchase Order Number\": \"\",\n" +
//                "    \"Purchase Order Date\": \"\",\n" +
//                "    \"Items\": [\n" +
//                "        {\n" +
//                "            \"Item Description\": \"\",\n" +
//                "            \"Quantity\": \"\",\n" +
//                "            \"Unit Price\": \"\"\n" +
//                "            \"Delivery Date\": \"\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"Item Description\": \"\",\n" +
//                "            \"Quantity\": \"\",\n" +
//                "            \"Unit Price\": \"\"\n" +
//                "            \"Delivery Date\": \"\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"Item Description\": \"\",\n" +
//                "            \"Quantity\": \"\",\n" +
//                "            \"Unit Price\": \"\"\n" +
//                "            \"Delivery Date\": \"\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"Item Description\": \"\",\n" +
//                "            \"Quantity\": \"\",\n" +
//                "            \"Unit Price\": \"\"\n" +
//                "            \"Delivery Date\": \"\"\n" +
//                "        }\n" +
//                "    ],\n" +
//                "    \"Currency\": \"\"\n" +
//                "    \"Payment Term\": \"\"\n" +
//                "    \"Incoterm\": \"\"\n" +
//                "    \"Bill To Address\": \"\"\n" +
//                "    \"Ship to Address\": \"\"\n" +
//                "    \"Delivery to Address\": \"\"\n" +
//                "}\n" +
//                "Notes:\n" +
//                "\n" +
//                "Extract All the line items Don't miss anything, there can be scenario where only one item description is present but quantity delivery date changes so make a note of this scenario and extract all line items which defers in quantity and delivery date."+
//                "Extract the Purchase Order Number, Purchase Order Date, and Currency from their respective sections in the document.\n" +
//                "Extract Item Description, Quantity, and Unit Price for all listed items in the document and structure them under the Items array. If the document contains fewer items, adjust the array size accordingly.\n" +
//                "For the Release Date, if present, extract it under \"Purchase Order Date\" or include it as part of item-level information if item-specific.\n" +
//                "Maintain precision, especially for numerical values like Quantity, Unit Price, and Currency.\n" +
//                "Analyze that text is in different language convert that to english, for eg. if payment term is in different language then english then convert it to english. Similarly for other fields."+
//                "IMPORTANT: Only extract the exact information present in the document and return it in the requested format. Do not include additional remarks or interpretations."+extractedData;
//
//
//        System.out.println(prompt);
//        // Headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", "application/json");
//        headers.set("api-key", apiKey);
//
//        // Request Body
//        Map<String, Object> requestBody = Map.of(
//                "messages", new Object[] {
//                        Map.of("role", "user", "content", prompt)
//                }
//
//        );
//
//        // Create HTTP Entity
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        // Make REST Call
//        ResponseEntity<String> response = restTemplate.exchange(
//                endpoint,
//                HttpMethod.POST,
//                entity,
//                String.class
//        );
//        JsonNode rootNode = objectMapper.readTree(response.getBody());
//        String content = rootNode.path("choices").get(0).path("message").path("content").asText();
//        content = content.replaceAll("```(json)?", "").replaceAll("```", "").trim();
//        return content;
//    }
}

